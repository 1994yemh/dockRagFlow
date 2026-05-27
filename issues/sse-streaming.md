# RAGFlow 全栈 SSE 流式打字机系统升级

## 上下文描述
为了彻底解决同步阻塞等待导致的用户体验滞后问题，需将聊天助手调试沙盒重构为 SSE (Server-Sent Events) 流式字符输出模式。这需要后端将多轮对话升级为异步 `SseEmitter` 监听机制，且前端升级为流式字节解码与打字机动态渲染。

## 计划步骤
- [x] 1. 业务接口与 REST 控制器扩充：[RagFlowChatService.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/service/RagFlowChatService.java) & [RagFlowChatController.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/controller/RagFlowChatController.java)
- [x] 2. 注入流式依赖并实现核心异步流服务：[RagFlowChatServiceImpl.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/service/impl/RagFlowChatServiceImpl.java)
- [x] 3. 重构前端流式打字机渲染机制：[index.html](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/resources/static/index.html)
- [x] 4. 运行编译与全栈联调验证

## 2026-05-27 流式打字与排版高保真优化 (方案 1)
- [x] 1. 后端 Service 层流式大模型数据转为 JSON 封装，避免特殊字符在流式切行中遗失。
- [x] 2. 控制器中为 `/send-chat-flow` 路由接口物理注入禁用代理缓存响应头 (`X-Accel-Buffering: no` 等)。
- [x] 3. 前端 UI 重构 SSE 字节流解码，淘汰 `line.trim()` 逻辑，并采用 `JSON.parse` 完美保真还原 Markdown 排版格式；升级 System Prompt 使得大模型输出更加精炼归纳；集成 Marked.js 和 Highlight.js 实现高规格富文本和多色彩代码块渲染；完成防抖式极速滚动及流结束后一次性高亮重构，保障极致无阻卡视觉流畅度；全线打通 sessionId 的后端传递，实现会话在 RAGFlow 的自动持久化保存。

## 2026-05-27 会话聊天记录持久化修复 (核心Bug修复)
**问题原因**：流式对话 `sendChatFlow` 走的是 LangChain4j 的 OpenAI 兼容协议（`/api/v1/openai/{chat_id}`），该协议不支持 `session_id` 参数，导致 RAGFlow 无法将对话消息关联到已存在的 Session，聊天记录无法保存。
**修复方案**：将流式对话接口从 LangChain4j OpenAI 兼容协议切换为 RAGFlow 原生 `POST /api/v1/chat/completions`，传递 `chat_id` + `session_id` + `stream=true`。
- [x] 1. `RagFlowChatWithHistoryReqVO.java` 新增 `sessionId` 可选字段
- [x] 2. `RagFlowChatServiceImpl.sendChatFlow()` 重写：弃用 LangChain4j StreamingChatModel，改用 Hutool HttpRequest 直连 RAGFlow 原生 `/api/v1/chat/completions`，传递 `session_id`；增加累积式 answer 的差量提取逻辑（RAGFlow 每次返回完整文本，需与上次对比取增量），保证前端打字机效果不变
- [x] 3. 前端 `index.html` 的 `sendMessage()` payload 中增加 `session_id: activeSessionId.value`
- [x] 4. Maven 编译验证通过

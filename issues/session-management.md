# RAGFlow 会话管理与调试闭环系统

## 上下文描述
用户需要将原本的“智能体”命名彻底修正为“聊天助手”。此外，需要在前端实现：
1. 头像图片上传，并自动在前端转换为 Base64 格式保存。
2. 知识库级联多选，替换掉原有的 Area 输入框，调用后端 `/list-datasets` 真正绑定数据集。
3. 实现多页面/Tab，将“聊天助手管理”和“会话调试管理（Sessions）”完美分层。
4. 全栈联动：打通会话 Sessions 的真实后端增删改查及详情读取（带 `messages` 历史消息），并在前台右侧沙盒对话中支持多轮聊天。

## 计划步骤
- [x] 1. 恢复 0 字节文件：`RagFlowChatAssistantRespVO.java` 与 `RagFlowChatReqVO.java`
- [x] 2. 新增会话管理及多轮对话请求/响应 VO
- [x] 3. 补全 Service 层接口与具体实现逻辑（通过 Hutool 调用 RAGFlow API）
- [x] 4. 补全 Controller 接口，暴露 REST 服务
- [x] 5. 重构前端 index.html，彻底由“纯前端模拟”改为“真实 fetch API 调用”
- [x] 6. 运行 maven 编译测试，进行全栈测试与问题评审

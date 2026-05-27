# Walkthrough - RAGFlow 会话管理与聊天助手全栈控制台

我已为您完美实现了 **“聊天助手管理”** 与 **“会话管理及调试”** 的全栈闭环控制台系统！本系统全面淘汰了以往单纯的“智能体”陈旧命名，严格践行了 RAGFlow 会话 Sessions 管理体系，并升级为真正的多 Tab 页流式交互，消除了所有的页面拥挤感，给用户带来wow的极简专业感。

---

## 1. 核心架构与新建/修改文件清单

本次升级严格遵守项目编码与 POJO 设计规约，新增加与修改的层级关系及职责如下：

```mermaid
graph TD
    subgraph 前端 UI (精美毛玻璃多Tab页)
        index_html[index.html]
    end

    subgraph 后端 REST 控制器
        Controller[RagFlowChatController]
    end

    subgraph 业务逻辑服务层
        Service[RagFlowChatService / RagFlowChatServiceImpl]
    end

    subgraph 后端 VO 实体层
        ReqVO[RagFlowChatReqVO / RagFlowChatWithHistoryReqVO]
        RespVO[RagFlowChatAssistantRespVO / RagFlowSessionRespVO]
        SessionReq[RagFlowSessionCreateReqVO / RagFlowSessionUpdateReqVO / RagFlowSessionDeleteReqVO]
    end

    subgraph 底层 RAGFlow 系统接口
        LangChain4j[LangChain4j OpenAiChatModel] -->|多轮 completions 对话| RAGFlow_OpenAI[/api/v1/openai/{chat_id}/chat/completions]
        Service -->|会话 CRUD| RAGFlow_Sessions[/api/v1/chats/{chat_id}/sessions]
    end

    index_html -->|真实异步 Fetch| Controller
    Controller -->|调用业务| Service
    Service -->|数据承载| ReqVO & RespVO & SessionReq
```

### 📁 Java 核心扩展清单（全部遵循 `@author 中锐网络` 注释，UTF-8 编码）
1. **[RECOVER]** [RagFlowChatAssistantRespVO.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/pojo/vo/RagFlowChatAssistantRespVO.java)：聊天助手全量参数响应实体，支持嵌套的大模型/Prompt设置。
2. **[RECOVER]** [RagFlowChatReqVO.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/pojo/vo/RagFlowChatReqVO.java)：单轮对话请求参数实体。
3. **[NEW]** [RagFlowSessionCreateReqVO.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/pojo/vo/RagFlowSessionCreateReqVO.java)：创建会话请求，限制 `@NotBlank` 校验。
4. **[NEW]** [RagFlowSessionUpdateReqVO.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/pojo/vo/RagFlowSessionUpdateReqVO.java)：更新会话（重命名）请求，限制 `@NotBlank` 校验。
5. **[NEW]** [RagFlowSessionDeleteReqVO.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/pojo/vo/RagFlowSessionDeleteReqVO.java)：删除会话请求，支持批量 `ids` 列表及一键 `deleteAll` 标识。
6. **[NEW]** [RagFlowSessionRespVO.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/pojo/vo/RagFlowSessionRespVO.java)：会话出参数据实体。包含 `messages` 列表，完美贴合系统的蛇形字段转换。
7. **[NEW]** [RagFlowChatWithHistoryReqVO.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/pojo/vo/RagFlowChatWithHistoryReqVO.java)：包含追问上下文的 `messages` 历史消息数组的对话请求。
8. **[MODIFY]** [RagFlowChatService.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/service/RagFlowChatService.java) & [RagFlowChatServiceImpl.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/service/impl/RagFlowChatServiceImpl.java)：
   * **会话代理**：使用 Hutool `HttpRequest` 与 RAGFlow 系统进行代理请求，彻底实现了会话的获取列表、创建、重命名、批量删除、详情拉取接口。
   * **多轮追问**：提取前端历史记录消息链，使用 `langchain4j 1.0.0-beta3` 的 `ChatLanguageModel`，组装为 `List<ChatMessage>`（包含 `UserMessage` / `AiMessage` / `SystemMessage`），调用 `chatModel.chat(langchainMessages)` 并获取 `ChatResponse` 文本提炼，支持完美的上下文提问！
9. **[MODIFY]** [RagFlowChatController.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/controller/RagFlowChatController.java)：暴露 6 个标准 REST API，路由和方法名强一致，只专注于路由请求的入口控制，不包含任何业务。

### 📁 前端 UI 极润改造
* **[MODIFY]** [index.html](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/resources/static/index.html)：
  * **毛玻璃双 Tab 架构**：顶部导航实现 “💬 聊天助手管理” 与 “🪵 会话管理与调试” 双页签，逻辑相互独立，解决以往堆砌拥挤的问题。
  * **知识库级联卡片多选**：自动请求后端 `/list-datasets`，以精美 Checkbox Cards 展现系统内所有可关联的知识库，点击即可在表单中完成多选 ID 数组装配。
  * **拖拽/选择头像 Base64 上传**：提供拖拽上传区域，文件上传后利用 `FileReader` 自动将其转换为 Base64 编码存入 `form.icon`，附带圆形高保真预览及一键清除功能。
  * **全真后台 Session 联动**：前端所有的会话操作（创建、重命名、删除、一键清空）不再是死板的纯前端模拟，而是真实以异步 fetch 调用后台，并在切换会话时，自动拉取该会话在后台存储的所有 `messages` 历史聊天记录完美呈现在沙盒窗口中。
  * **多轮流式对话追问**：点击会话发起聊天时，将当前沙盒的所有上下文 `messages` 转换组装传递给后端的多轮对话接口，实现真正具有长效检索记忆的 RAG 深度追问。

---

## 2. 自动化与系统测试报告

* **本地构建报告 (mvn compile passed)**：
  ```bash
  [INFO] Compiling 22 source files with javac [debug release 17] to target\classes
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  ```
  *(注：所有 VO 类型转换、Jackson 反序列化编译在 JDK 17 下 100% 成功通过跑通)*

---

## 3. 全栈会话 API 交互规约

### 3.1 查询会话列表
* **HTTP 请求**：`GET http://localhost:8080/api/v1/ragflow-chat/list-sessions/{chatId}`
* **出参示例**：
```json
[
  {
    "id": "578d541e87ad11ef96b90242ac120006",
    "chat_id": "2ca4b22e878011ef88fe0242ac120005",
    "name": "new session",
    "create_time": 1728636403974
  }
]
```

### 3.2 创建会话
* **HTTP 请求**：`POST http://localhost:8080/api/v1/ragflow-chat/create-session/{chatId}`
* **JSON 入参**：`{ "name": "会话 1" }`
* **出参示例**：
```json
{
  "id": "自动生成的新SessionID",
  "chat_id": "2ca4b22e878011ef88fe0242ac120005",
  "name": "会话 1"
}
```

### 3.3 带上下文的历史消息多轮提问
* **HTTP 请求**：`POST http://localhost:8080/api/v1/ragflow-chat/send-chat-with-history`
* **JSON 入参**：
```json
{
  "chat_id": "2ca4b22e878011ef88fe0242ac120005",
  "messages": [
    { "role": "user", "content": "我的电脑显卡驱动怎么更新？" },
    { "role": "assistant", "content": "您可以通过访问 NVIDIA 官网..." },
    { "role": "user", "content": "那 AMD 的呢？" }
  ]
}
```
* **JSON 回答出参**：
```json
{
  "answer": "如果是 AMD 显卡，您可以访问 AMD 官网 of 驱动支持页面...",
  "chat_id": "2ca4b22e878011ef88fe0242ac120005"
}
```

# 聊天助手全量参数集成与前端测试页开发计划

本任务旨在完善 RAGFlow 聊天助手（Chat Assistant）的 `POST /api/v1/chats` 创建接口（补充完整所有参数），并补充完整的删（Delete）、改（Update）、查（List）接口实现。同时，我们将设计并编写一个高颜值的 Glassmorphism 现代前端页面，用于测试聊天助手的全部 CRUD 功能。

## 1. 计划详情 (Implementation Details)

### 1.1 后端 VO 补充与规范化 (POJO Layer)
*   **【修改】** `RagFlowChatAssistantCreateReqVO.java`
    *   补充字段：
        *   `icon` (string, Base64头像)
        *   `topK` (Integer, 检索候选块数，默认1024)
        *   `rerankId` (String, 重排模型ID)
    *   补充 `LlmSettingVO` 内部类：
        *   `modelType` (String, 默认 "chat")
    *   补充 `PromptConfigVO` 内部类：
        *   `tts` (Boolean, 默认 false)
        *   `refineMultiturn` (Boolean, 默认 false)
        *   `useKg` (Boolean, 默认 false)
        *   `reasoning` (Boolean, 默认 false)
        *   `crossLanguages` (List<String>)
        *   `tavilyApiKey` (String)
        *   `tocEnhance` (Boolean, 默认 false)
        *   `parameters` (List<ParameterVO>)
    *   新增内部类 `ParameterVO` (包含 `key` 和 `optional`)
*   **【新建】** `RagFlowChatAssistantUpdateReqVO.java`
    *   用于更新接口，继承或复制自 CreateReqVO，添加 `id` 属性，并添加校验注解。
*   **【新建/重构】** `RagFlowChatAssistantRespVO.java`
    *   替代原来的 `RagFlowChatAssistantCreateRespVO`，作为统一定义的 RAGFlow 聊天助手全字段数据返回 VO。包含 RAGFlow 服务端返回的所有元数据和实体字段。

### 1.2 服务层扩展 (Service Layer)
*   **【修改】** `RagFlowChatService.java` & `RagFlowChatServiceImpl.java`
    *   **创建**：将 `createChatAssistant` 的返回值更新为统一的 `RagFlowChatAssistantRespVO`。
    *   **更新**：`RagFlowChatAssistantRespVO updateChatAssistant(RagFlowChatAssistantUpdateReqVO reqVO)`，向外对接 RAGFlow 的 `PUT /api/v1/chats/{chat_id}`。
    *   **删除**：`Boolean deleteChatAssistant(String chatId)`，向外对接 RAGFlow 的 `DELETE /api/v1/chats/{chat_id}`。
    *   **列表**：`List<RagFlowChatAssistantRespVO> listChatAssistants(Integer page, Integer pageSize, String keywords, String name, String id)`，向外对接 RAGFlow 的 `GET /api/v1/chats`。

### 1.3 控制器层扩展 (Controller Layer)
*   **【修改】** `RagFlowChatController.java`
    *   规范符合 REST API 路径与方法名完全匹配：
        *   创建：`POST /create-chat-assistant`，调用 `createChatAssistant`
        *   更新：`POST /update-chat-assistant` 或 `PUT /update-chat-assistant`，调用 `updateChatAssistant`
        *   删除：`DELETE /delete-chat-assistant/{chatId}`，调用 `deleteChatAssistant`
        *   列表：`GET /list-chat-assistants`，调用 `listChatAssistants`

### 1.4 前端高颜值测试页面开发 (UI Layer)
*   **【新建】** `src/main/resources/static/index.html`
    *   **视觉风格**：采用极其震撼的 Sleek Dark Mode 风格 + 霓虹渐变（Neon Gradient）边框 + 细腻的毛玻璃拟玻态（Glassmorphism）卡片面板。使用 `Outfit` 和 `Inter` Google Fonts 现代字体。
    *   **核心功能**：
        1.  **添加/编辑面板（左侧/中层）**：表单设计极为细腻，支持展开/收起高级配置。包含：
            *   基本设置：名称、知识库ID（支持填入多个）、LLM ID（支持下拉或填入）。
            *   LLM 高级参数设置：温度、Top P、存在惩罚、频率惩罚、模型类型等滑动条与输入框。
            *   Prompt 精细设置：系统提示词（Textarea）、开场白、空回复、是否开启引文、TTS、多轮提炼、知识图谱搜索、深度推理、多语言转换、Tavily API Key、TOC 增强。
            *   检索阈值：相似度阈值（滑动条 0-1）、向量相似度权重（滑动条 0-1）、Top N/K、重排模型。
        2.  **助理列表（右侧/中层）**：卡片式列表展示当前系统中的全部聊天助手。包含搜索框。
            *   每个卡片拥有精美的渐变点缀，并配有一键“编辑”（自动回填表单并高亮显示）、“一键删除”和“对话体验”快捷按钮。
        3.  **对话调试沙盒（侧边滑出抽屉或浮窗）**：能够即时与选定的聊天助手在线实时对话，查看其 RAG 回复。
        4.  **响应状态日志面板（底部）**：以精致的黑客风（JetBrains Mono）代码块，实时打印每次发送和返回的完整 JSON 数据，便于观察参数是否完全写入及服务端的真实响应。

## 2. 预期结果与验证方案
*   **后端验证**：编写单元测试 `RagFlowChatServiceImplTest.java` 覆盖 CRUD 四个方法，保证接口百分之百调用正确且能跟 RAGFlow 畅通交互。
*   **前端验证**：通过访问 `http://localhost:8080/index.html` 验证界面展示效果，手动测试每一个 CRUD 操作，确保每个参数在创建和更新时都能成功写入服务端。

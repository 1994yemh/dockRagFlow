# 聊天助手全量进化、知识库多选与会话多Tab架构设计计划

根据用户的反馈，我们对控制台的命名定位进行了精准校正（由“智能体”更正为“聊天助手”，以切合 RAGFlow 实际架构），并为控制台实施以下高能级升级：实现知识库 API 级联多选、头像本地上传 Base64 编码、以及高内聚的多 Tab 分页架构（预留聊天会话 Session 增删改查）。

## 1. 详细实施步骤 (Implementation Steps)

### 1.1 精准重命名与术语规范化
*   **【清理】** 将后端控制器 [RagFlowChatController.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/controller/RagFlowChatController.java)、服务层 [RagFlowChatService.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/service/RagFlowChatService.java)、单元测试类 [RagFlowChatServiceImplTest.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/test/java/com/ruinet/ragflow/service/RagFlowChatServiceImplTest.java) 以及前端页面中的所有“智能体”称呼，一律重构重命名为**“聊天助手”**，保持前后端及界面描述的高度统一。

### 1.2 知识库列表级联拉取与集成 (Dataset Integration)
*   **【新建】** [RagFlowDatasetRespVO.java](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/java/com/ruinet/ragflow/pojo/vo/RagFlowDatasetRespVO.java)：作为拉取 RAGFlow 知识库列表的数据接收出参。
*   **【修改】** `RagFlowChatService.java` & `RagFlowChatServiceImpl.java`：
    *   新增方法：`List<RagFlowDatasetRespVO> listDatasets()`。
    *   底层发送：`GET /api/v1/datasets?page_size=100` 并解析返回的 JSON 数组。
*   **【修改】** `RagFlowChatController.java`：
    *   新增端点：`GET /api/v1/ragflow-chat/list-datasets` 映射到 `listDatasets`。
*   **【修改】** 前端 `index.html`：
    *   页面初始化时自动请求 `/list-datasets` 并存入 `datasets` 状态。
    *   **重构“关联知识库”UI**：抛弃原先干瘪的手动填入 TextArea，设计为**极富科技感的复选框网格卡片 (Checkbox Cards Grid)**。用户只需在列表卡片上轻点勾选，即可实现知识库的多选与取消多选，后台自动将选中的 ID 组合为 `dataset_ids` 数组。

### 1.3 高颜值的头像本地上传转 Base64 动效
*   **【修改】** 前端 `index.html`：
    *   在头像输入框左边或下方设计一个毛玻璃风格的 **图片拖拽上传与选择区 (Image Upload Zone)**。
    *   内置 `FileReader` 监听图片选择事件，自动实现 `File` 到 `DataURL (Base64)` 的无缝转换，自动回写给 `form.icon`。
    *   **无缝预览**：当 `form.icon` 含有 Base64 编码时，页面提供精致的圆形微缩图进行实时预览，并提供高颜值的“一键清除”特效。

### 1.4 双页页签架构 (Multi-Tab Navigation & Sessions Prep)
*   **【修改】** 前端 `index.html`：
    *   在顶部设计一排科技感十足的毛玻璃 Tab 页签导航：
        1.  **💬 聊天助手管理 (Assistant Management)**：承载现有的聊天助手 CRUD 配置和卡片展示。
        2.  **🪵 会话调试与管理 (Session Sandbox & CRUD)**：
            *   为了“分多个页面，不要挤在一起”的极佳体验，将原先侧边的对话沙盒彻底升级为此 Tab 独立页。
            *   当选中左侧的任意一个聊天助手时，右侧将呈现专门的 **“会话列表面板”**（预留会话 Session 的增删改查 UI 控制）。
            *   可以在此页创建、选择、删除属于该助手的多个不同的独立会话。

## 2. 预期验证方案
*   **编译验证**：控制台运行 `mvn clean compile` 以验证新增的 Dataset 接口无任何类型或编译问题。
*   **功能测试**：启动后端服务后，访问 `http://localhost:8080/` 检验：
    1.  知识库多选卡片是否成功渲染并拉取了数据库中真实的知识库列表。
    2.  上传头像能否成功实现图片到 Base64 字符的转换并正常创建/修改助手。
    3.  点击 Tab 选项卡是否能在“助手管理”与“会话管理”之间流畅切换。

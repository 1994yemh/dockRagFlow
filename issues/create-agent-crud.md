# 创建智能体 (Agent) CRUD 接口计划

## 1. 上下文
用户需要使用 Java 实现对 RAGFlow 智能体 (Agent) 的增删改查 (CRUD) 接口，并提供一个能跑的、最简的 `dsl`，同时需要包含相关的单元测试。

## 2. 计划步骤

### 2.1 创建 VO 类
- **[NEW]** `RagFlowAgentCreateReqVO.java`
  - 属性: `title` (String, 必填), `description` (String), `dsl` (Object, 必填)
- **[NEW]** `RagFlowAgentUpdateReqVO.java`
  - 属性: `title` (String), `description` (String), `dsl` (Object)
- **[NEW]** `RagFlowAgentRespVO.java`
  - 属性: `id`, `title`, `description`, `dsl`, `create_time`, `update_time`, 等。

### 2.2 创建 Service 接口与实现类
- **[NEW]** `RagFlowAgentService.java`
  - 方法: `createAgent`, `updateAgent`, `deleteAgent`, `listAgents`
- **[NEW]** `RagFlowAgentServiceImpl.java`
  - 注入 `RagFlowConfig` 获取 `baseUrl` 和 `apiKey`。
  - 使用 `Hutool` 的 `HttpRequest` 调用 RAGFlow 的 `/api/v1/agents` 系列接口。

### 2.3 编写最简 DSL 与单元测试
- **[NEW]** `RagFlowAgentServiceImplTest.java`
  - 构建一个最简的 `dsl`（仅包含一个 Begin 节点）。
  - 使用 `@Disabled` 注解忽略真实的外部接口请求（或者在本地测试时可手动放开）。
  - 编写 `testCreateAgent`, `testUpdateAgent`, `testDeleteAgent`, `testListAgents`。

## 3. 预期结果
完成上述文件创建后，可以通过注入 `RagFlowAgentService` 在业务中实现对智能体的增删改查操作。单元测试能够演示完整的调用流程和默认 DSL 的结构。

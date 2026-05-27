# Task: Create Chat Assistant API Integration

## Context
The user wants to implement the "Create Chat Assistant" (新建知识库智能体) HTTP API in the Spring Boot project.
This API interacts with RAGFlow's POST `/api/v1/chats` endpoint.
It must follow project POJO conventions, REST API naming match rules, UTF-8 coding, and use Hutool for object/HTTP manipulations.

## Plan
1. Create request VO: `RagFlowChatAssistantCreateReqVO.java`
2. Create response VO: `RagFlowChatAssistantCreateRespVO.java`
3. Expose method in `RagFlowChatService.java`
4. Implement integration with RAGFlow backend using Hutool HttpRequest in `RagFlowChatServiceImpl.java`
5. Map REST endpoint in `RagFlowChatController.java`: `POST /create-chat-assistant` matching `createChatAssistant`
6. Add unit tests and integration test setup in `RagFlowChatServiceImplTest.java`

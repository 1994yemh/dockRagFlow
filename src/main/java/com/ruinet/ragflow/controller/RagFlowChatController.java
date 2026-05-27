package com.ruinet.ragflow.controller;

import com.ruinet.ragflow.pojo.vo.*;
import com.ruinet.ragflow.service.RagFlowChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RAGFlow 智能体对话控制器。
 *
 * @author 中锐网络
 */
@RestController
@RequestMapping("/api/v1/ragflow-chat")
@Tag(name = "RAGFlow Chat API", description = "用于与 RAGFlow 智能体进行对话交互的接口")
public class RagFlowChatController {

    @Autowired
    private RagFlowChatService ragFlowChatService;

    /**
     * 发送对话请求，并返回智能体大模型整合知识库后的回答。
     * 遵循规约：REST API 路径与方法名完全匹配（方法：sendChat，路径：send-chat），且控制器内无任何业务逻辑。
     *
     * @param reqVO 请求视图对象
     * @return 响应视图对象
     */
    @PostMapping("/send-chat")
    @Operation(summary = "发送对话问题", description = "接收用户的提问，通过 LangChain4j 调度 RAGFlow 的知识库检索大模型并返回总结回答")
    public RagFlowChatRespVO sendChat(@Valid @RequestBody RagFlowChatReqVO reqVO) {
        return ragFlowChatService.sendChat(reqVO);
    }

    /**
     * 动态创建一个专属的知识库智能体。
     * 遵循规约：REST API 路径与方法名完全匹配（方法：createChatAssistant，路径：create-chat-assistant），且控制器内无任何业务逻辑。
     *
     * @param reqVO 智能体助理创建参数
     * @return 创建成功的智能体助理详情
     */
    @PostMapping("/create-chat-assistant")
    @Operation(summary = "创建智能体助理", description = "用于在 RAGFlow 中通过 API 动态新建一个专属的知识库智能体助理")
    public RagFlowChatAssistantRespVO createChatAssistant(@Valid @RequestBody RagFlowChatAssistantCreateReqVO reqVO) {
        return ragFlowChatService.createChatAssistant(reqVO);
    }

    /**
     * 修改专属的知识库智能体配置。
     * 遵循规约：REST API 路径与方法名完全匹配（方法：updateChatAssistant，路径：update-chat-assistant），且控制器内无任何业务逻辑。
     *
     * @param reqVO 智能体助理修改参数
     * @return 修改成功后的智能体助理详情
     */
    @PostMapping("/update-chat-assistant")
    @Operation(summary = "更新智能体助理配置", description = "用于更新已存在专属的知识库智能体助理配置")
    public RagFlowChatAssistantRespVO updateChatAssistant(@Valid @RequestBody RagFlowChatAssistantUpdateReqVO reqVO) {
        return ragFlowChatService.updateChatAssistant(reqVO);
    }

    /**
     * 根据主键 ID 删除指定的知识库智能体助理。
     * 遵循规约：REST API 路径与方法名完全匹配（方法：deleteChatAssistant，路径：delete-chat-assistant/{chatId}），且控制器内无任何业务逻辑。
     *
     * @param chatId 聊天助手 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/delete-chat-assistant/{chatId}")
    @Operation(summary = "删除智能体助理", description = "通过 ID 删除指定的知识库智能体助理")
    public Boolean deleteChatAssistant(@PathVariable("chatId") String chatId) {
        return ragFlowChatService.deleteChatAssistant(chatId);
    }

    /**
     * 分页条件过滤获取知识库智能体助理列表。
     * 遵循规约：REST API 路径与方法名完全匹配（方法：listChatAssistants，路径：list-chat-assistants），且控制器内无任何业务逻辑。
     *
     * @param page 页码
     * @param pageSize 每页条数
     * @param keywords 模糊搜索关键词
     * @param name 智能体名字精确匹配
     * @param id 智能体 ID 精确匹配
     * @return 智能体助理列表
     */
    @GetMapping("/list-chat-assistants")
    @Operation(summary = "获取聊天助手列表", description = "分页条件过滤查询当前的知识库聊天助手列表")
    public List<RagFlowChatAssistantRespVO> listChatAssistants(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page_size", required = false) Integer pageSize,
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "id", required = false) String id) {
        return ragFlowChatService.listChatAssistants(page, pageSize, keywords, name, id);
    }

    /**
     * 获取当前租户所有的知识库数据集列表。
     * 遵循规约：REST API 路径与方法名完全匹配（方法：listDatasets，路径：list-datasets），且控制器内无任何业务逻辑。
     *
     * @return 知识库列表
     */
    @GetMapping("/list-datasets")
    @Operation(summary = "获取知识库列表", description = "获取当前系统所有的知识库数据集列表")
    public List<RagFlowDatasetRespVO> listDatasets() {
        return ragFlowChatService.listDatasets();
    }

    /**
     * 获取指定聊天助手关联的所有会话列表。
     *
     * @param chatId 聊天助手 ID
     * @return 会话列表
     */
    @GetMapping("/list-sessions/{chatId}")
    @Operation(summary = "获取会话列表", description = "查询指定聊天助手关联的会话列表")
    public List<RagFlowSessionRespVO> listSessions(@PathVariable("chatId") String chatId) {
        return ragFlowChatService.listSessions(chatId);
    }

    /**
     * 为指定的聊天助手创建一个新会话。
     *
     * @param chatId 聊天助手 ID
     * @param reqVO  会话创建参数
     * @return 创建成功的会话详情
     */
    @PostMapping("/create-session/{chatId}")
    @Operation(summary = "创建会话", description = "为指定的聊天助手创建一个全新会话")
    public RagFlowSessionRespVO createSession(@PathVariable("chatId") String chatId, @Valid @RequestBody RagFlowSessionCreateReqVO reqVO) {
        return ragFlowChatService.createSession(chatId, reqVO);
    }

    /**
     * 更新指定聊天助手的指定会话属性（如重命名）。
     *
     * @param chatId    聊天助手 ID
     * @param sessionId 会话 ID
     * @param reqVO     会话更新参数
     * @return 更新后的会话详情
     */
    @PatchMapping("/update-session/{chatId}/{sessionId}")
    @Operation(summary = "更新会话名称", description = "修改指定会话的名称")
    public RagFlowSessionRespVO updateSession(@PathVariable("chatId") String chatId, @PathVariable("sessionId") String sessionId, @Valid @RequestBody RagFlowSessionUpdateReqVO reqVO) {
        return ragFlowChatService.updateSession(chatId, sessionId, reqVO);
    }

    /**
     * 批量或一键清空指定聊天助手的会话。
     *
     * @param chatId 聊天助手 ID
     * @param reqVO  会话删除参数
     * @return 是否成功删除
     */
    @DeleteMapping("/delete-sessions/{chatId}")
    @Operation(summary = "删除会话", description = "批量删除指定的会话或一键清空该聊天助手的会话")
    public Boolean deleteSessions(@PathVariable("chatId") String chatId, @RequestBody RagFlowSessionDeleteReqVO reqVO) {
        return ragFlowChatService.deleteSessions(chatId, reqVO);
    }

    /**
     * 获取指定会话的详细信息（包含历史聊天记录）。
     *
     * @param chatId    聊天助手 ID
     * @param sessionId 会话 ID
     * @return 会话详情（含消息记录列表）
     */
    @GetMapping("/get-session/{chatId}/{sessionId}")
    @Operation(summary = "获取会话详情", description = "获取单个会话的信息，包含其历史聊天记录")
    public RagFlowSessionRespVO getSession(@PathVariable("chatId") String chatId, @PathVariable("sessionId") String sessionId) {
        return ragFlowChatService.getSessionDetails(chatId, sessionId);
    }

    /**
     * 带有历史消息上下文的多轮追问对话端点。
     *
     * @param reqVO 多轮对话请求参数
     * @return 回答结果
     */
    @PostMapping("/send-chat-with-history")
    @Operation(summary = "多轮追问对话", description = "传递完整的历史聊天记录以实现有记忆的知识库深度问答")
    public RagFlowChatRespVO sendChatWithHistory(@Valid @RequestBody RagFlowChatWithHistoryReqVO reqVO) {
        return ragFlowChatService.sendChatWithHistory(reqVO);
    }

    /**
     * 流式多轮追问对话端点，通过 Server-Sent Events (SSE) 协议逐字返回大模型回答。
     * 遵循规约：REST API 路径与方法名完全匹配（方法：sendChatFlow，路径：send-chat-flow），且控制器内无业务逻辑。
     *
     * @param reqVO    多轮对话请求参数
     * @param response 响应对象，用于动态拦截并强灌禁用响应缓冲和缓存的 Headers，保卫极致流式吐字
     * @return SSE 流发射器
     */
    @PostMapping(value = "/send-chat-flow", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式多轮追问对话", description = "利用 Server-Sent Events 流式通道，实时逐字下发大模型有检索记忆的回答")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter sendChatFlow(
            @Valid @RequestBody RagFlowChatWithHistoryReqVO reqVO,
            jakarta.servlet.http.HttpServletResponse response) {
        
        // 1. 强力且深度地禁用反向代理（如 Nginx）以及 CDN/网关的 Response Buffering 响应积压行为
        response.setHeader("X-Accel-Buffering", "no");
        
        // 2. 注入全面无死角的清除缓存 Headers，确保流通道的高实时畅通
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        return ragFlowChatService.sendChatFlow(reqVO);
    }
}


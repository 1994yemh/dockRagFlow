package com.ruinet.ragflow.service;

import com.ruinet.ragflow.pojo.vo.*;

import java.util.List;

/**
 * RAGFlow 聊天助手对话业务逻辑接口。
 *
 * @author 中锐网络
 */
public interface RagFlowChatService {

    /**
     * 发送问题给指定的 RAGFlow 聊天助手，并返回其基于知识库总结出的回答。
     *
     * @param reqVO 请求参数（包含提问文本和可选的聊天助手ID）
     * @return 聊天助手响应（包含生成的回答文本）
     */
    RagFlowChatRespVO sendChat(RagFlowChatReqVO reqVO);

    /**
     * 动态创建一个专属的知识库聊天助手。
     *
     * @param reqVO 聊天助手创建参数
     * @return 创建成功的聊天助手详情
     */
    RagFlowChatAssistantRespVO createChatAssistant(RagFlowChatAssistantCreateReqVO reqVO);

    /**
     * 更新指定聊天助手的配置。
     *
     * @param reqVO 聊天助手更新参数
     * @return 更新后的聊天助手详情
     */
    RagFlowChatAssistantRespVO updateChatAssistant(RagFlowChatAssistantUpdateReqVO reqVO);

    /**
     * 根据主键 ID 删除聊天助手。
     *
     * @param id 聊天助手 ID
     * @return 是否删除成功
     */
    Boolean deleteChatAssistant(String id);

    /**
     * 分页过滤查询当前租户拥有的聊天助手列表。
     *
     * @param page 页码
     * @param pageSize 每页条数
     * @param keywords 关键字
     * @param name 聊天助手名称
     * @param id 聊天助手 ID
     * @return 聊天助手列表
     */
    List<RagFlowChatAssistantRespVO> listChatAssistants(Integer page, Integer pageSize, String keywords, String name, String id);

    /**
     * 获取当前租户下所有的知识库数据集列表。
     *
     * @return 知识库列表数据
     */
    List<RagFlowDatasetRespVO> listDatasets();

    /**
     * 分页过滤查询当前聊天助手关联的会话列表。
     *
     * @param chatId 聊天助手 ID
     * @return 会话列表
     */
    List<RagFlowSessionRespVO> listSessions(String chatId);

    /**
     * 为聊天助手创建一个新会话。
     *
     * @param chatId 聊天助手 ID
     * @param reqVO  会话创建参数
     * @return 创建成功的会话详情
     */
    RagFlowSessionRespVO createSession(String chatId, RagFlowSessionCreateReqVO reqVO);

    /**
     * 更新指定会话的属性（如重命名）。
     *
     * @param chatId    聊天助手 ID
     * @param sessionId 会话 ID
     * @param reqVO     会话修改参数
     * @return 更新后的会话详情
     */
    RagFlowSessionRespVO updateSession(String chatId, String sessionId, RagFlowSessionUpdateReqVO reqVO);

    /**
     * 批量或一键删除指定聊天助手的会话。
     *
     * @param chatId 聊天助手 ID
     * @param reqVO  会话删除参数
     * @return 是否删除成功
     */
    Boolean deleteSessions(String chatId, RagFlowSessionDeleteReqVO reqVO);

    /**
     * 获取指定会话的详情（包含历史聊天记录）。
     *
     * @param chatId    聊天助手 ID
     * @param sessionId 会话 ID
     * @return 会话详情（包含消息列表）
     */
    RagFlowSessionRespVO getSessionDetails(String chatId, String sessionId);

    /**
     * 发送带有历史记忆的多轮提问对话，并返回大模型的回答。
     *
     * @param reqVO 多轮对话请求参数（含历史消息列表和助手ID）
     * @return 对话回复结果
     */
    RagFlowChatRespVO sendChatWithHistory(RagFlowChatWithHistoryReqVO reqVO);
}



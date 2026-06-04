package com.ruinet.ragflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ruinet.ragflow.exception.ServiceException;
import com.ruinet.ragflow.pojo.vo.*;
import com.ruinet.ragflow.service.RagFlowChatService;
import java.util.List;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * RAGFlow 聊天助手对话业务逻辑实现类。
 *
 * @author 中锐网络
 */
@Service
public class RagFlowChatServiceImpl implements RagFlowChatService {

    @Value("${ragflow.base-url}")
    private String baseUrl;

    @Value("${ragflow.api-key}")
    private String apiKey;

    @Value("${ragflow.default-chat-id}")
    private String defaultChatId;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public RagFlowChatRespVO sendChat(RagFlowChatReqVO reqVO) {
        // 1. 优先使用 Hutool 进行空值提前检查，满足提前 return / 提前抛出异常规约
        if (reqVO == null) {
            throw new ServiceException("请求体不能为空");
        }
        if (StrUtil.isBlank(reqVO.getMessage())) {
            throw new ServiceException("提问内容不能为空");
        }

        // 2. 确定目标聊天助手 ID，不传则使用系统默认配置
        String targetChatId = StrUtil.isNotBlank(reqVO.getChatId()) ? reqVO.getChatId() : defaultChatId;
        if (StrUtil.isBlank(targetChatId) || "your-chat-id-here".equals(targetChatId)) {
            throw new ServiceException("聊天助手 ID 未配置或无效，请检查参数");
        }

        if (StrUtil.isBlank(apiKey) || "your-api-key-here".equals(apiKey)) {
            throw new ServiceException("RAGFlow API Key 未正确配置");
        }

        // 3. 构建大模型底层对接端点
        // 注意：LangChain4j 的 OpenAiChatModel 在发送请求时，会自动在 baseUrl 尾部追加 "/chat/completions"。
        // 因此我们提供的 baseUrl 必须形式为：http://{host-address}/api/v1/openai/{chat_id}
        String openaiCompatibleUrl = String.format("%s/api/v1/openai/%s", 
                StrUtil.removeSuffix(baseUrl, "/"), targetChatId);

        // 4. 使用指定的 langchain4j 1.0.0-beta3 构建 OpenAI 兼容客户端
        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .baseUrl(openaiCompatibleUrl)
                .apiKey(apiKey)
                .modelName("qwen3-32b@Tongyi-Qianwen") // 采用通义千问默认模型
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        // 5. 调用大模型生成文本（由 RAGFlow 内部进行 RAG 知识库检索和总结）
        String answer;
        try {
            answer = chatModel.chat(reqVO.getMessage());
        } catch (Exception e) {
            // 系统异常转译为业务异常抛出，提供关键上下文
            throw new ServiceException("调用 RAGFlow 聊天助手接口失败：%s", e.getMessage());
        }

        if (StrUtil.isBlank(answer)) {
            throw new ServiceException("聊天助手未能生成任何有效回答");
        }

        // 6. 构造返回视图，使用 Hutool 的 BeanUtil 进行属性复制，符合强规
        RagFlowChatRespVO respVO = new RagFlowChatRespVO();
        respVO.setAnswer(answer);
        respVO.setChatId(targetChatId);

        return respVO;
    }

    @Override
    public RagFlowChatAssistantRespVO createChatAssistant(RagFlowChatAssistantCreateReqVO reqVO) {
        // 1. 值检查与前置断言
        if (ObjUtil.isNull(reqVO)) {
            throw new ServiceException("请求入参不能为空");
        }
        if (StrUtil.isBlank(reqVO.getName())) {
            throw new ServiceException("聊天助手名称不能为空");
        }
        if (StrUtil.isBlank(reqVO.getLlmId())) {
            throw new ServiceException("指定的底层大模型 ID 不能为空");
        }
        if (StrUtil.isBlank(apiKey) || "your-api-key-here".equals(apiKey)) {
            throw new ServiceException("RAGFlow API Key 未正确配置");
        }

        // 2. 补全大模型参数 (llm_setting) 默认值以防前端遗漏传入
        RagFlowChatAssistantCreateReqVO.LlmSettingVO setting = reqVO.getLlmSetting();
        if (setting == null) {
            setting = new RagFlowChatAssistantCreateReqVO.LlmSettingVO();
            reqVO.setLlmSetting(setting);
        }
        if (setting.getModelType() == null) {
            setting.setModelType("chat");
        }
        if (setting.getTemperature() == null) {
            setting.setTemperature(0.1);
        }
        if (setting.getTopP() == null) {
            setting.setTopP(0.3);
        }
        if (setting.getPresencePenalty() == null) {
            setting.setPresencePenalty(0.4);
        }
        if (setting.getFrequencyPenalty() == null) {
            setting.setFrequencyPenalty(0.7);
        }

        // 3. 将入参对象序列化为蛇形命名的 JSON 字符串
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(reqVO);
        } catch (Exception e) {
            throw new ServiceException("序列化聊天助手配置参数失败：%s", e.getMessage());
        }
        System.out.println("requestJson" + requestJson);
        // 3. 拼接 RAGFlow 后端 API 路由，发送 POST 请求进行创建
        String url = String.format("%s/api/v1/chats", StrUtil.removeSuffix(baseUrl, "/"));
        String responseBody;
        try {
            responseBody = HttpRequest.post(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestJson)
                    .timeout(30000)
                    .execute()
                    .body();
        } catch (Exception e) {
            throw new ServiceException("向 RAGFlow 发送创建聊天助手请求失败：%s", e.getMessage());
        }

        if (StrUtil.isBlank(responseBody)) {
            throw new ServiceException("RAGFlow 服务返回内容为空");
        }

        // 4. 解析响应 JSON
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            int code = rootNode.path("code").asInt(-1);
            String message = rootNode.path("message").asText("");

            if (code != 0) {
                throw new ServiceException("RAGFlow 接口创建聊天助手失败：%s", message);
            }

            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                throw new ServiceException("RAGFlow 创建成功但未返回聊天助手详细数据");
            }

            // 反序列化为 Java VO 出参
            return objectMapper.treeToValue(dataNode, RagFlowChatAssistantRespVO.class);
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("解析 RAGFlow 响应数据失败：%s", e.getMessage());
        }
    }

    @Override
    public RagFlowChatAssistantRespVO updateChatAssistant(RagFlowChatAssistantUpdateReqVO reqVO) {
        // 1. 值检查与前置断言
        if (ObjUtil.isNull(reqVO)) {
            throw new ServiceException("请求入参不能为空");
        }
        if (StrUtil.isBlank(reqVO.getId())) {
            throw new ServiceException("聊天助手 ID 不能为空");
        }
        if (StrUtil.isBlank(reqVO.getName())) {
            throw new ServiceException("聊天助手名称不能为空");
        }
        if (StrUtil.isBlank(reqVO.getLlmId())) {
            throw new ServiceException("指定的底层大模型 ID 不能为空");
        }
        if (StrUtil.isBlank(apiKey) || "your-api-key-here".equals(apiKey)) {
            throw new ServiceException("RAGFlow API Key 未正确配置");
        }

        // 2. 补全大模型参数 (llm_setting) 默认值以防前端遗漏传入
        RagFlowChatAssistantCreateReqVO.LlmSettingVO setting = reqVO.getLlmSetting();
        if (setting == null) {
            setting = new RagFlowChatAssistantCreateReqVO.LlmSettingVO();
            reqVO.setLlmSetting(setting);
        }
        if (setting.getModelType() == null) {
            setting.setModelType("chat");
        }
        if (setting.getTemperature() == null) {
            setting.setTemperature(0.1);
        }
        if (setting.getTopP() == null) {
            setting.setTopP(0.3);
        }
        if (setting.getPresencePenalty() == null) {
            setting.setPresencePenalty(0.4);
        }
        if (setting.getFrequencyPenalty() == null) {
            setting.setFrequencyPenalty(0.7);
        }

        // 3. 将入参对象序列化为蛇形命名的 JSON 字符串
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(reqVO);
        } catch (Exception e) {
            throw new ServiceException("序列化聊天助手更新配置参数失败：%s", e.getMessage());
        }


        // 3. 拼接 RAGFlow 后端 API 路由，发送 PUT 请求进行覆盖更新
        String url = String.format("%s/api/v1/chats/%s", 
                StrUtil.removeSuffix(baseUrl, "/"), reqVO.getId());
        String responseBody;
        try {
            responseBody = HttpRequest.put(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestJson)
                    .timeout(30000)
                    .execute()
                    .body();
        } catch (Exception e) {
            throw new ServiceException("向 RAGFlow 发送更新聊天助手请求失败：%s", e.getMessage());
        }

        if (StrUtil.isBlank(responseBody)) {
            throw new ServiceException("RAGFlow 服务返回内容为空");
        }

        // 4. 解析响应 JSON
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            int code = rootNode.path("code").asInt(-1);
            String message = rootNode.path("message").asText("");

            if (code != 0) {
                throw new ServiceException("RAGFlow 接口更新聊天助手失败：%s", message);
            }

            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                throw new ServiceException("RAGFlow 更新成功但未返回聊天助手详细数据");
            }

            return objectMapper.treeToValue(dataNode, RagFlowChatAssistantRespVO.class);
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("解析 RAGFlow 响应数据失败：%s", e.getMessage());
        }
    }

    @Override
    public Boolean deleteChatAssistant(String id) {
        // 1. 值检查与前置断言
        if (StrUtil.isBlank(id)) {
            throw new ServiceException("待删除的聊天助手 ID 不能为空");
        }
        if (StrUtil.isBlank(apiKey) || "your-api-key-here".equals(apiKey)) {
            throw new ServiceException("RAGFlow API Key 未正确配置");
        }

        // 2. 发送 DELETE 请求
        String url = String.format("%s/api/v1/chats/%s", 
                StrUtil.removeSuffix(baseUrl, "/"), id);
        String responseBody;
        try {
            responseBody = HttpRequest.delete(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(30000)
                    .execute()
                    .body();
        } catch (Exception e) {
            throw new ServiceException("向 RAGFlow 发送删除聊天助手请求失败：%s", e.getMessage());
        }

        if (StrUtil.isBlank(responseBody)) {
            throw new ServiceException("RAGFlow 服务返回内容为空");
        }

        // 3. 解析响应 JSON
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            int code = rootNode.path("code").asInt(-1);
            String message = rootNode.path("message").asText("");

            if (code != 0) {
                throw new ServiceException("RAGFlow 接口删除聊天助手失败：%s", message);
            }

            return true;
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("解析 RAGFlow 响应数据失败：%s", e.getMessage());
        }
    }

    @Override
    public List<RagFlowChatAssistantRespVO> listChatAssistants(Integer page, Integer pageSize, String keywords, String name, String id) {
        if (StrUtil.isBlank(apiKey) || "your-api-key-here".equals(apiKey)) {
            throw new ServiceException("RAGFlow API Key 未正确配置");
        }

        // 1. 构建包含查询参数的请求，采用 Hutool 的 form 机制，GET 请求会自动拼接到 URL 尾部作为 QueryString 传参
        String url = String.format("%s/api/v1/chats", StrUtil.removeSuffix(baseUrl, "/"));
        HttpRequest request = HttpRequest.get(url)
                .header("Authorization", "Bearer " + apiKey)
                .timeout(30000);
        if (page != null) {
            request.form("page", page);
        }
        if (pageSize != null) {
            request.form("page_size", pageSize);
        }
        if (StrUtil.isNotBlank(keywords)) {
            request.form("keywords", keywords);
        }
        if (StrUtil.isNotBlank(name)) {
            request.form("name", name);
        }
        if (StrUtil.isNotBlank(id)) {
            request.form("id", id);
        }

        // 2. 发送 GET 请求
        String responseBody;
        try {
            responseBody = request.execute().body();
        } catch (Exception e) {
            throw new ServiceException("向 RAGFlow 发送查询聊天助手列表请求失败：%s", e.getMessage());
        }

        if (StrUtil.isBlank(responseBody)) {
            throw new ServiceException("RAGFlow 服务返回内容为空");
        }

        // 3. 解析响应 JSON 并获取 chats 数组
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            int code = rootNode.path("code").asInt(-1);
            String message = rootNode.path("message").asText("");

            if (code != 0) {
                throw new ServiceException("RAGFlow 接口查询聊天助手列表失败：%s", message);
            }

            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                return new java.util.ArrayList<>();
            }

            JsonNode chatsNode = dataNode.path("chats");
            if (chatsNode.isMissingNode() || chatsNode.isNull() || !chatsNode.isArray()) {
                return new java.util.ArrayList<>();
            }

            // 4. 使用 Jackson CollectionType 进行安全的集合反序列化，防范泛型推断编译报错
            com.fasterxml.jackson.databind.type.CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, RagFlowChatAssistantRespVO.class);
            return objectMapper.readValue(chatsNode.toString(), listType);
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("解析 RAGFlow 列表数据失败：%s", e.getMessage());
        }
    }

    @Override
    public List<RagFlowDatasetRespVO> listDatasets() {
        if (StrUtil.isBlank(apiKey) || "your-api-key-here".equals(apiKey)) {
            throw new ServiceException("RAGFlow API Key 未正确配置");
        }

        // 1. 构建包含分页与大小的请求，因为获取所有知识库，page_size 设为 100 即可覆盖普通用户规模
        String url = String.format("%s/api/v1/datasets", StrUtil.removeSuffix(baseUrl, "/"));
        HttpRequest request = HttpRequest.get(url)
                .header("Authorization", "Bearer " + apiKey)
                .timeout(30000)
                .form("page_size", 100);

        // 2. 发送 GET 请求
        String responseBody;
        try {
            responseBody = request.execute().body();
        } catch (Exception e) {
            throw new ServiceException("向 RAGFlow 发送查询知识库列表请求失败：%s", e.getMessage());
        }

        if (StrUtil.isBlank(responseBody)) {
            throw new ServiceException("RAGFlow 服务返回内容为空");
        }

        // 3. 解析响应 JSON，其 data 节点直接为数据集 JSON 数据数组
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            int code = rootNode.path("code").asInt(-1);
            String message = rootNode.path("message").asText("");

            if (code != 0) {
                throw new ServiceException("RAGFlow 接口查询知识库列表失败：%s", message);
            }

            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isMissingNode() || dataNode.isNull() || !dataNode.isArray()) {
                return new java.util.ArrayList<>();
            }

            // 4. 使用 Jackson CollectionType 安全反序列化为 Dataset 列表，防范泛型推断编译报错
            com.fasterxml.jackson.databind.type.CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, RagFlowDatasetRespVO.class);
            return objectMapper.readValue(dataNode.toString(), listType);
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("解析 RAGFlow 知识库数据失败：%s", e.getMessage());
        }
    }

    @Override
    public List<RagFlowSessionRespVO> listSessions(String chatId) {
        if (StrUtil.isBlank(chatId)) {
            throw new ServiceException("聊天助手 ID 不能为空");
        }
        if (StrUtil.isBlank(apiKey) || "your-api-key-here".equals(apiKey)) {
            throw new ServiceException("RAGFlow API Key 未正确配置");
        }

        String url = String.format("%s/api/v1/chats/%s/sessions", StrUtil.removeSuffix(baseUrl, "/"), chatId);
        String responseBody;
        try {
            responseBody = HttpRequest.get(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(30000)
                    .form("page_size", 100)
                    .execute()
                    .body();
        } catch (Exception e) {
            throw new ServiceException("向 RAGFlow 发送查询会话列表请求失败：%s", e.getMessage());
        }

        if (StrUtil.isBlank(responseBody)) {
            throw new ServiceException("RAGFlow 服务返回内容为空");
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            int code = rootNode.path("code").asInt(-1);
            String message = rootNode.path("message").asText("");

            if (code != 0) {
                throw new ServiceException("RAGFlow 接口查询会话列表失败：%s", message);
            }

            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isMissingNode() || dataNode.isNull() || !dataNode.isArray()) {
                return new java.util.ArrayList<>();
            }

            com.fasterxml.jackson.databind.type.CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, RagFlowSessionRespVO.class);
            return objectMapper.readValue(dataNode.toString(), listType);
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("解析 RAGFlow 会话列表数据失败：%s", e.getMessage());
        }
    }

    @Override
    public RagFlowSessionRespVO createSession(String chatId, RagFlowSessionCreateReqVO reqVO) {
        if (StrUtil.isBlank(chatId)) {
            throw new ServiceException("聊天助手 ID 不能为空");
        }
        if (reqVO == null || StrUtil.isBlank(reqVO.getName())) {
            throw new ServiceException("会话名称不能为空");
        }
        if (StrUtil.isBlank(apiKey) || "your-api-key-here".equals(apiKey)) {
            throw new ServiceException("RAGFlow API Key 未正确配置");
        }

        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(reqVO);
        } catch (Exception e) {
            throw new ServiceException("序列化创建会话参数失败：%s", e.getMessage());
        }

        String url = String.format("%s/api/v1/chats/%s/sessions", StrUtil.removeSuffix(baseUrl, "/"), chatId);
        String responseBody;
        try {
            responseBody = HttpRequest.post(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestJson)
                    .timeout(30000)
                    .execute()
                    .body();
        } catch (Exception e) {
            throw new ServiceException("向 RAGFlow 发送创建会话请求失败：%s", e.getMessage());
        }

        if (StrUtil.isBlank(responseBody)) {
            throw new ServiceException("RAGFlow 服务返回内容为空");
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            int code = rootNode.path("code").asInt(-1);
            String message = rootNode.path("message").asText("");

            if (code != 0) {
                throw new ServiceException("RAGFlow 接口创建会话失败：%s", message);
            }

            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                throw new ServiceException("RAGFlow 创建成功但未返回会话详细数据");
            }

            return objectMapper.treeToValue(dataNode, RagFlowSessionRespVO.class);
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("解析 RAGFlow 创建会话响应数据失败：%s", e.getMessage());
        }
    }

    @Override
    public RagFlowSessionRespVO updateSession(String chatId, String sessionId, RagFlowSessionUpdateReqVO reqVO) {
        if (StrUtil.isBlank(chatId)) {
            throw new ServiceException("聊天助手 ID 不能为空");
        }
        if (StrUtil.isBlank(sessionId)) {
            throw new ServiceException("会话 ID 不能为空");
        }
        if (reqVO == null || StrUtil.isBlank(reqVO.getName())) {
            throw new ServiceException("更新会话名称不能为空");
        }
        if (StrUtil.isBlank(apiKey) || "your-api-key-here".equals(apiKey)) {
            throw new ServiceException("RAGFlow API Key 未正确配置");
        }

        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(reqVO);
        } catch (Exception e) {
            throw new ServiceException("序列化更新会话参数失败：%s", e.getMessage());
        }

        String url = String.format("%s/api/v1/chats/%s/sessions/%s", 
                StrUtil.removeSuffix(baseUrl, "/"), chatId, sessionId);
        String responseBody;
        try {
            responseBody = HttpRequest.patch(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestJson)
                    .timeout(30000)
                    .execute()
                    .body();
        } catch (Exception e) {
            throw new ServiceException("向 RAGFlow 发送更新会话请求失败：%s", e.getMessage());
        }

        if (StrUtil.isBlank(responseBody)) {
            throw new ServiceException("RAGFlow 服务返回内容为空");
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            int code = rootNode.path("code").asInt(-1);
            String message = rootNode.path("message").asText("");

            if (code != 0) {
                throw new ServiceException("RAGFlow 接口更新会话失败：%s", message);
            }

            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                throw new ServiceException("RAGFlow 更新成功但未返回会话详细数据");
            }

            return objectMapper.treeToValue(dataNode, RagFlowSessionRespVO.class);
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("解析 RAGFlow 更新会话响应数据失败：%s", e.getMessage());
        }
    }

    @Override
    public Boolean deleteSessions(String chatId, RagFlowSessionDeleteReqVO reqVO) {
        if (StrUtil.isBlank(chatId)) {
            throw new ServiceException("聊天助手 ID 不能为空");
        }
        if (reqVO == null) {
            throw new ServiceException("删除参数不能为空");
        }
        if (StrUtil.isBlank(apiKey) || "your-api-key-here".equals(apiKey)) {
            throw new ServiceException("RAGFlow API Key 未正确配置");
        }

        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(reqVO);
        } catch (Exception e) {
            throw new ServiceException("序列化删除会话参数失败：%s", e.getMessage());
        }

        String url = String.format("%s/api/v1/chats/%s/sessions", StrUtil.removeSuffix(baseUrl, "/"), chatId);
        String responseBody;
        try {
            responseBody = HttpRequest.delete(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestJson)
                    .timeout(30000)
                    .execute()
                    .body();
        } catch (Exception e) {
            throw new ServiceException("向 RAGFlow 发送删除会话请求失败：%s", e.getMessage());
        }

        if (StrUtil.isBlank(responseBody)) {
            throw new ServiceException("RAGFlow 服务返回内容为空");
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            int code = rootNode.path("code").asInt(-1);
            String message = rootNode.path("message").asText("");

            if (code != 0) {
                throw new ServiceException("RAGFlow 接口删除会话失败：%s", message);
            }

            return true;
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("解析 RAGFlow 删除会话响应失败：%s", e.getMessage());
        }
    }

    @Override
    public RagFlowSessionRespVO getSessionDetails(String chatId, String sessionId) {
        if (StrUtil.isBlank(chatId)) {
            throw new ServiceException("聊天助手 ID 不能为空");
        }
        if (StrUtil.isBlank(sessionId)) {
            throw new ServiceException("会话 ID 不能为空");
        }
        if (StrUtil.isBlank(apiKey) || "your-api-key-here".equals(apiKey)) {
            throw new ServiceException("RAGFlow API Key 未正确配置");
        }

        String url = String.format("%s/api/v1/chats/%s/sessions/%s", 
                StrUtil.removeSuffix(baseUrl, "/"), chatId, sessionId);
        String responseBody;
        try {
            responseBody = HttpRequest.get(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(30000)
                    .execute()
                    .body();
        } catch (Exception e) {
            throw new ServiceException("向 RAGFlow 发送查询会话详情请求失败：%s", e.getMessage());
        }

        if (StrUtil.isBlank(responseBody)) {
            throw new ServiceException("RAGFlow 服务返回内容为空");
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            int code = rootNode.path("code").asInt(-1);
            String message = rootNode.path("message").asText("");

            if (code != 0) {
                throw new ServiceException("RAGFlow 接口查询会话详情失败：%s", message);
            }

            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                throw new ServiceException("RAGFlow 未返回会话详细数据");
            }

            return objectMapper.treeToValue(dataNode, RagFlowSessionRespVO.class);
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("解析 RAGFlow 会话详情响应数据失败：%s", e.getMessage());
        }
    }

    @Override
    public RagFlowChatRespVO sendChatWithHistory(RagFlowChatWithHistoryReqVO reqVO) {
        if (reqVO == null) {
            throw new ServiceException("请求体不能为空");
        }
        if (StrUtil.isBlank(reqVO.getChatId())) {
            throw new ServiceException("聊天助手 ID 不能为空");
        }
        if (reqVO.getMessages() == null || reqVO.getMessages().isEmpty()) {
            throw new ServiceException("对话消息历史不能为空");
        }

        if (StrUtil.isBlank(apiKey) || "your-api-key-here".equals(apiKey)) {
            throw new ServiceException("RAGFlow API Key 未正确配置");
        }

        String openaiCompatibleUrl = String.format("%s/api/v1/openai/%s", 
                StrUtil.removeSuffix(baseUrl, "/"), reqVO.getChatId());

        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .baseUrl(openaiCompatibleUrl)
                .apiKey(apiKey)
                .modelName("qwen3-32b@Tongyi-Qianwen")
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        List<ChatMessage> langchainMessages = new java.util.ArrayList<>();
        for (RagFlowSessionRespVO.MessageVO msg : reqVO.getMessages()) {
            if ("user".equalsIgnoreCase(msg.getRole())) {
                langchainMessages.add(UserMessage.from(msg.getContent()));
            } else if ("assistant".equalsIgnoreCase(msg.getRole())) {
                langchainMessages.add(AiMessage.from(msg.getContent()));
            } else if ("system".equalsIgnoreCase(msg.getRole())) {
                langchainMessages.add(SystemMessage.from(msg.getContent()));
            }
        }

        ChatResponse chatResponse;
        try {
            chatResponse = chatModel.chat(langchainMessages);
        } catch (Exception e) {
            throw new ServiceException("调用 RAGFlow 多轮对话接口失败：%s", e.getMessage());
        }

        if (chatResponse == null || chatResponse.aiMessage() == null || StrUtil.isBlank(chatResponse.aiMessage().text())) {
            throw new ServiceException("聊天助手未能生成任何有效回答");
        }

        RagFlowChatRespVO respVO = new RagFlowChatRespVO();
        respVO.setAnswer(chatResponse.aiMessage().text());
        respVO.setChatId(reqVO.getChatId());

        return respVO;
    }

    @Override
    public SseEmitter sendChatFlow(RagFlowChatWithHistoryReqVO reqVO) {
        if (reqVO == null) {
            throw new ServiceException("请求体不能为空");
        }
        if (StrUtil.isBlank(reqVO.getChatId())) {
            throw new ServiceException("聊天助手 ID 不能为空");
        }
        if (reqVO.getMessages() == null || reqVO.getMessages().isEmpty()) {
            throw new ServiceException("对话消息历史不能为空");
        }

        if (StrUtil.isBlank(apiKey) || "your-api-key-here".equals(apiKey)) {
            throw new ServiceException("RAGFlow API Key 未正确配置");
        }

        // 1. 构建 RAGFlow 原生对话端点 URL（支持 session_id 自动持久化对话记录）
        String url = String.format("%s/api/v1/chat/completions", StrUtil.removeSuffix(baseUrl, "/"));

        // 2. 构建请求 Body：chat_id + session_id + stream + messages（仅取最后一条 user 消息作为 question）
        java.util.Map<String, Object> bodyMap = new java.util.LinkedHashMap<>();
        bodyMap.put("chat_id", reqVO.getChatId());
        if (StrUtil.isNotBlank(reqVO.getSessionId())) {
            bodyMap.put("session_id", reqVO.getSessionId());
        }
        bodyMap.put("stream", true);

        // 将完整历史消息列表传递给 RAGFlow，让 RAGFlow 自行处理上下文
        java.util.List<java.util.Map<String, String>> messageList = new java.util.ArrayList<>();
        for (RagFlowSessionRespVO.MessageVO msg : reqVO.getMessages()) {
            java.util.Map<String, String> msgMap = new java.util.LinkedHashMap<>();
            msgMap.put("role", msg.getRole());
            msgMap.put("content", msg.getContent());
            messageList.add(msgMap);
        }
        bodyMap.put("messages", messageList);

        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(bodyMap);
        } catch (Exception e) {
            throw new ServiceException("序列化流式对话请求参数失败：%s", e.getMessage());
        }

        // 3. 创建 Spring SSE 发射器，超时 120 秒
        SseEmitter emitter = new SseEmitter(120000L);

        // 4. 异步线程中发起 HTTP 流式请求并逐行推送 SSE 事件
        new Thread(() -> {
            try {
                java.io.InputStream inputStream = HttpRequest.post(url)
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .body(requestJson)
                        .timeout(120000)
                        .executeAsync()
                        .bodyStream();

                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8));

                // RAGFlow 原生 SSE 的 answer 是累积式的，需做差量提取，只推送新增字符给前端打字机
                String[] previousAnswer = {""};

                String line;
                while ((line = reader.readLine()) != null) {
                    // RAGFlow 原生 SSE 帧格式：data:{JSON}
                    if (line.startsWith("data:")) {
                        String jsonStr = line.substring(5).trim();

                        // 结束标志
                        if ("[DONE]".equals(jsonStr)) {
                            break;
                        }

                        if (StrUtil.isNotBlank(jsonStr)) {
                            try {
                                JsonNode rootNode = objectMapper.readTree(jsonStr);
                                int code = rootNode.path("code").asInt(0);
                                if (code != 0) {
                                    String errMsg = rootNode.path("message").asText("未知错误");
                                    System.err.println("[RAGFlow 流式对话错误]: " + errMsg);
                                    continue;
                                }

                                JsonNode dataNode = rootNode.path("data");

                                // 终止帧：data 为 true 表示流结束
                                if (dataNode.isBoolean() && dataNode.asBoolean()) {
                                    break;
                                }

                                // 提取累积文本 answer 字段，并进行健壮的增量/重置差量提取，防范大模型思考过程与正式回答内容重置时造成的交织乱序与严重卡顿
                                String fullAnswer = dataNode.path("answer").asText("");
                                if (StrUtil.isNotBlank(fullAnswer)) {
                                    String delta = "";
                                    if (fullAnswer.startsWith(previousAnswer[0])) {
                                        // 1. 正常单调追加状态，安全截取差量
                                        delta = fullAnswer.substring(previousAnswer[0].length());
                                        previousAnswer[0] = fullAnswer;
                                    } else {
                                        // 2. 内容发生非单调跳变（如从思考过程切换到最终解答，或发生了清空重置）
                                        // 此时重置历史基准，将当前内容重新全量视为增量发出，杜绝截断卡死与乱码
                                        delta = fullAnswer;
                                        previousAnswer[0] = fullAnswer;
                                    }

                                    if (StrUtil.isNotBlank(delta)) {
                                        // 将增量文本封装为 JSON 推送（与原有前端解析格式保持兼容）
                                        java.util.Map<String, Object> dataMap = new java.util.HashMap<>();
                                        dataMap.put("text", delta);
                                        
                                        // 提取大模型返回的引用（reference）信息原样下发，用以渲染问答引用溯源面板
                                        JsonNode referenceNode = dataNode.path("reference");
                                        if (referenceNode != null && !referenceNode.isMissingNode() && !referenceNode.isNull()) {
                                            dataMap.put("reference", referenceNode);
                                        }
                                        
                                        String json = objectMapper.writeValueAsString(dataMap);

                                        System.out.print(delta);
                                        System.out.flush();
                                        emitter.send(SseEmitter.event().data(json));
                                    }
                                }
                            } catch (Exception e) {
                                // 静默处理解析异常，继续读取下一行
                            }
                        }
                    }
                }

                reader.close();
                System.out.println("\n[RAGFlow 流式对话生成完成]");
                emitter.complete();
            } catch (Exception e) {
                try {
                    System.err.println("\n[RAGFlow 流式对话异常]: " + e.getMessage());
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                    // 静默关闭通道
                }
            }
        }, "ragflow-sse-stream-thread").start();

        return emitter;
    }
}


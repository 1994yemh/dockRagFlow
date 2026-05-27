package com.ruinet.ragflow.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruinet.ragflow.exception.ServiceException;
import com.ruinet.ragflow.pojo.vo.RagFlowAgentCreateReqVO;
import com.ruinet.ragflow.pojo.vo.RagFlowAgentRespVO;
import com.ruinet.ragflow.pojo.vo.RagFlowAgentUpdateReqVO;
import com.ruinet.ragflow.service.RagFlowAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能体服务实现类
 *
 * @author 中锐网络
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagFlowAgentServiceImpl implements RagFlowAgentService {

    @Value("${ragflow.base-url}")
    private String baseUrl;

    @Value("${ragflow.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper;

    @Override
    public void createAgent(RagFlowAgentCreateReqVO reqVO) {
        String url = baseUrl + "/api/v1/agents";

        try {
            String jsonBody = objectMapper.writeValueAsString(reqVO);

            try (HttpResponse response = HttpRequest.post(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .execute()) {

                handleResponse(response, "创建智能体");
            }

        } catch (JsonProcessingException e) {
            log.error("创建智能体序列化参数失败", e);
            throw new ServiceException("创建智能体序列化参数失败");
        }
    }

    @Override
    public void deleteAgent(String agentId) {
        if (agentId == null || agentId.isBlank()) {
            throw new ServiceException("智能体ID不能为空");
        }
        
        String url = baseUrl + "/api/v1/agents/" + agentId;

        try (HttpResponse response = HttpRequest.delete(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .execute()) {

            handleResponse(response, "删除智能体");
        }
    }

    @Override
    public void updateAgent(String agentId, RagFlowAgentUpdateReqVO reqVO) {
        if (agentId == null || agentId.isBlank()) {
            throw new ServiceException("智能体ID不能为空");
        }

        String url = baseUrl + "/api/v1/agents/" + agentId;

        try {
            String jsonBody = objectMapper.writeValueAsString(reqVO);

            try (HttpResponse response = HttpRequest.put(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .execute()) {

                handleResponse(response, "更新智能体");
            }

        } catch (JsonProcessingException e) {
            log.error("更新智能体序列化参数失败", e);
            throw new ServiceException("更新智能体序列化参数失败");
        }
    }

    @Override
    public List<RagFlowAgentRespVO> listAgents(Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 30;
        }

        String url = String.format("%s/api/v1/agents?page=%d&page_size=%d", 
                baseUrl, page, pageSize);

        try (HttpResponse response = HttpRequest.get(url)
                .header("Authorization", "Bearer " + apiKey)
                .execute()) {

            JsonNode rootNode = handleResponse(response, "查询智能体列表");
            JsonNode dataNode = rootNode.get("data");

            if (dataNode != null && dataNode.isArray()) {
                return objectMapper.readValue(dataNode.traverse(), new TypeReference<List<RagFlowAgentRespVO>>() {});
            }
            return List.of();

        } catch (Exception e) {
            log.error("查询智能体列表失败", e);
            throw new ServiceException("查询智能体列表失败");
        }
    }

    private JsonNode handleResponse(HttpResponse response, String action) {
        String body = response.body();
        if (!response.isOk()) {
            log.error("RAGFlow HTTP请求失败: {}, statusCode={}, response={}", action, response.getStatus(), body);
            throw new ServiceException("RAGFlow 接口%s失败 HTTP Status %d", action, response.getStatus());
        }

        try {
            JsonNode rootNode = objectMapper.readTree(body);
            JsonNode codeNode = rootNode.get("code");
            if (codeNode == null || codeNode.asInt() != 0) {
                String message = rootNode.has("message") ? rootNode.get("message").asText() : "未知错误";
                log.error("RAGFlow 业务错误: {}, response={}", action, body);
                throw new ServiceException("RAGFlow 接口%s失败：%s", action, message);
            }
            return rootNode;
        } catch (JsonProcessingException e) {
            log.error("解析 RAGFlow 响应失败: {}", action, e);
            throw new ServiceException("解析 RAGFlow %s 响应失败", action);
        }
    }
}

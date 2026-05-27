package com.ruinet.ragflow.service;

import com.ruinet.ragflow.pojo.vo.RagFlowAgentCreateReqVO;
import com.ruinet.ragflow.pojo.vo.RagFlowAgentRespVO;
import com.ruinet.ragflow.pojo.vo.RagFlowAgentUpdateReqVO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 智能体服务测试类
 * 
 * @author 中锐网络
 */
@SpringBootTest
public class RagFlowAgentServiceImplTest {

    @Autowired
    private RagFlowAgentService ragFlowAgentService;

    /**
     * 构建一个最简的可运行 DSL（仅包含一个 Begin 节点）
     */
    private Map<String, Object> buildSimpleDsl() {
        Map<String, Object> dsl = new HashMap<>();
        
        // components
        Map<String, Object> beginNode = new HashMap<>();
        beginNode.put("downstream", List.of());
        beginNode.put("upstream", List.of());
        Map<String, Object> obj = new HashMap<>();
        obj.put("component_name", "Begin");
        obj.put("params", new HashMap<>());
        beginNode.put("obj", obj);
        
        Map<String, Object> components = new HashMap<>();
        components.put("begin", beginNode);
        dsl.put("components", components);

        // graph
        Map<String, Object> graph = new HashMap<>();
        graph.put("edges", List.of());
        
        Map<String, Object> node = new HashMap<>();
        node.put("id", "begin");
        node.put("type", "beginNode");
        node.put("sourcePosition", "left");
        node.put("targetPosition", "right");
        node.put("width", 200);
        node.put("height", 44);
        Map<String, Object> position = new HashMap<>();
        position.put("x", 50);
        position.put("y", 200);
        node.put("position", position);
        Map<String, Object> data = new HashMap<>();
        data.put("name", "begin");
        data.put("label", "Begin");
        node.put("data", data);
        
        graph.put("nodes", List.of(node));
        dsl.put("graph", graph);
        
        // others
        dsl.put("answer", List.of());
        dsl.put("history", List.of());
        dsl.put("messages", List.of());
        dsl.put("path", List.of());
        dsl.put("reference", List.of());

        return dsl;
    }

    @Test
    @Disabled("需要真实的 RAGFlow 环境配置")
    public void testCreateAgent() {
        RagFlowAgentCreateReqVO reqVO = new RagFlowAgentCreateReqVO();
        reqVO.setTitle("Java Test Agent");
        reqVO.setDescription("This is a test agent created via Java API");
        reqVO.setDsl(buildSimpleDsl());

        ragFlowAgentService.createAgent(reqVO);
        System.out.println("创建智能体成功");
    }

    @Test
    @Disabled("需要真实的 RAGFlow 环境配置以及具体的 agentId")
    public void testUpdateAgent() {
        String agentId = "REPLACE_WITH_REAL_AGENT_ID";
        
        RagFlowAgentUpdateReqVO reqVO = new RagFlowAgentUpdateReqVO();
        reqVO.setTitle("Java Test Agent Updated");
        reqVO.setDescription("Updated description");
        reqVO.setDsl(buildSimpleDsl());

        ragFlowAgentService.updateAgent(agentId, reqVO);
        System.out.println("更新智能体成功");
    }

    @Test
    @Disabled("需要真实的 RAGFlow 环境配置以及具体的 agentId")
    public void testDeleteAgent() {
        String agentId = "REPLACE_WITH_REAL_AGENT_ID";
        ragFlowAgentService.deleteAgent(agentId);
        System.out.println("删除智能体成功");
    }

    @Test
    @Disabled("需要真实的 RAGFlow 环境配置")
    public void testListAgents() {
        List<RagFlowAgentRespVO> list = ragFlowAgentService.listAgents(1, 10);
        assertNotNull(list);
        System.out.println("查询到智能体数量: " + list.size());
        for (RagFlowAgentRespVO agent : list) {
            System.out.println("Agent ID: " + agent.getId() + ", Title: " + agent.getTitle());
        }
    }
}

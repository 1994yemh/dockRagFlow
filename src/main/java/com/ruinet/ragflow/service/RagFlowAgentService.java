package com.ruinet.ragflow.service;

import com.ruinet.ragflow.pojo.vo.RagFlowAgentCreateReqVO;
import com.ruinet.ragflow.pojo.vo.RagFlowAgentRespVO;
import com.ruinet.ragflow.pojo.vo.RagFlowAgentUpdateReqVO;

import java.util.List;

/**
 * 智能体服务接口
 * 
 * @author 中锐网络
 */
public interface RagFlowAgentService {

    /**
     * 创建智能体
     *
     * @param reqVO 请求参数
     */
    void createAgent(RagFlowAgentCreateReqVO reqVO);

    /**
     * 删除智能体
     *
     * @param agentId 智能体ID
     */
    void deleteAgent(String agentId);

    /**
     * 更新智能体
     *
     * @param agentId 智能体ID
     * @param reqVO 更新参数
     */
    void updateAgent(String agentId, RagFlowAgentUpdateReqVO reqVO);

    /**
     * 分页查询智能体列表
     *
     * @param page 页码
     * @param pageSize 每页条数
     * @return 智能体列表
     */
    List<RagFlowAgentRespVO> listAgents(Integer page, Integer pageSize);
}

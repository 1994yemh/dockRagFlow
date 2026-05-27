package com.ruinet.ragflow.pojo.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建 RAGFlow 智能体/助理响应视图对象。
 *
 * @author 中锐网络
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "创建 RAGFlow 智能体助理响应出参")
public class RagFlowChatAssistantCreateRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "新建智能体的主键 ID / Chat ID", example = "0b2cbc8c877f11ef89070242ac120005")
    private String id;

    @Schema(description = "智能体名字", example = "新智能体名字")
    private String name;

    @Schema(description = "关联的知识库 ID 列表", example = "[\"0b2cbc8c877f11ef89070242ac120005\"]")
    private List<String> datasetIds;

    @Schema(description = "绑定的底层大模型 ID", example = "qwen-plus@Tongyi-Qianwen")
    private String llmId;
}

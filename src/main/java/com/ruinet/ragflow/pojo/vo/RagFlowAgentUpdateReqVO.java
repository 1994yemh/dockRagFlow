package com.ruinet.ragflow.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * @author 中锐网络
 */
@Data
@Schema(description = "更新智能体请求VO")
public class RagFlowAgentUpdateReqVO {

    @Schema(description = "智能体的名称", example = "Test Agent Updated")
    private String title;

    @Schema(description = "智能体的描述", example = "An updated test agent")
    private String description;

    @Schema(description = "智能体的 Canvas DSL 对象")
    private Map<String, Object> dsl;
}

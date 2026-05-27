package com.ruinet.ragflow.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author 中锐网络
 */
@Data
@Schema(description = "创建智能体请求VO")
public class RagFlowAgentCreateReqVO {

    @Schema(description = "智能体的名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "Test Agent")
    @NotBlank(message = "智能体名称不能为空")
    private String title;

    @Schema(description = "智能体的描述", example = "A test agent")
    private String description;

    @Schema(description = "智能体的 Canvas DSL 对象", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "dsl不能为空")
    private Map<String, Object> dsl;
}

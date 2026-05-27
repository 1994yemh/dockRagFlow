package com.ruinet.ragflow.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * RAGFlow 会话修改请求参数。
 *
 * @author 中锐网络
 */
@Data
@Schema(description = "RAGFlow 会话修改请求参数")
public class RagFlowSessionUpdateReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "修改后的会话名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "修改后的会话名称")
    @NotBlank(message = "会话名称不能为空")
    private String name;
}

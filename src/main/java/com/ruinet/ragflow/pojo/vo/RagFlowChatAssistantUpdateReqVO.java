package com.ruinet.ragflow.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 修改 RAGFlow 智能体/助理请求视图对象。
 *
 * @author 中锐网络
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "修改 RAGFlow 智能体助理请求入参")
public class RagFlowChatAssistantUpdateReqVO extends RagFlowChatAssistantCreateReqVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "聊天助手主键 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "0b2cbc8c877f11ef89070242ac120005")
    @NotBlank(message = "聊天助手主键 ID 不能为空")
    private String id;
}

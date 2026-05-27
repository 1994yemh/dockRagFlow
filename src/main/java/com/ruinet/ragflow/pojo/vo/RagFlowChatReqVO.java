package com.ruinet.ragflow.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * RAGFlow 聊天助手对话请求参数。
 *
 * @author 中锐网络
 */
@Data
@Schema(description = "RAGFlow 聊天助手对话请求参数")
public class RagFlowChatReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "提问内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你们的产品特点是什么？")
    @NotBlank(message = "提问内容不能为空")
    private String message;

    @Schema(description = "目标聊天助手(Chat)的ID，不传则使用系统配置的默认ID", example = "dbb4ed366e8611f09690a55a6daec4ef")
    private String chatId;
}

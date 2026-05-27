package com.ruinet.ragflow.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * RAGFlow 对话响应视图对象。
 *
 * @author 中锐网络
 */
@Data
@ToString
@Schema(description = "RAGFlow 智能体对话回复结果")
public class RagFlowChatRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "智能体返回的文本回答", example = "您好，系统环境变量可以通过以下步骤配置...")
    private String answer;

    @Schema(description = "实际调用的智能体 ID", example = "dbb4ed366e8611f09690a55a6daec4ef")
    private String chatId;
}

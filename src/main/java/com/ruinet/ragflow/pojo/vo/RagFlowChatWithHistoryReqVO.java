package com.ruinet.ragflow.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * RAGFlow 聊天助手带历史记忆的多轮对话请求参数。
 *
 * @author 中锐网络
 */
@Data
@Schema(description = "RAGFlow 聊天助手带历史记忆的多轮对话请求参数")
public class RagFlowChatWithHistoryReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关联的聊天助手 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2ca4b22e878011ef88fe0242ac120005")
    @NotBlank(message = "聊天助手 ID 不能为空")
    private String chatId;

    @Schema(description = "多轮对话的消息历史（包含本次的 user 提问，必须放在最后一条）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "消息列表不能为空")
    private List<RagFlowSessionRespVO.MessageVO> messages = new ArrayList<>();
}

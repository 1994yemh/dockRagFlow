package com.ruinet.ragflow.pojo.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * RAGFlow 会话响应参数。
 *
 * @author 中锐网络
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "RAGFlow 会话数据出参")
public class RagFlowSessionRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话主键 ID", example = "4606b4ec87ad11efbc4f0242ac120006")
    private String id;

    @Schema(description = "关联的聊天助手 ID", example = "2ca4b22e878011ef88fe0242ac120005")
    private String chatId;

    @Schema(description = "会话名称", example = "新会话")
    private String name;

    @Schema(description = "创建日期字符串", example = "Fri, 11 Oct 2024 08:46:14 GMT")
    private String createDate;

    @Schema(description = "创建时间戳", example = "1728636374571")
    private Long createTime;

    @Schema(description = "更新日期字符串", example = "Fri, 11 Oct 2024 08:46:14 GMT")
    private String updateDate;

    @Schema(description = "更新时间戳", example = "1728636374571")
    private Long updateTime;

    @Schema(description = "用户自定义 ID")
    private String userId;

    @Schema(description = "历史消息记录列表")
    private List<MessageVO> messages = new ArrayList<>();

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "单条消息数据")
    public static class MessageVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "消息角色（user 或 assistant）", example = "user")
        private String role;

        @Schema(description = "消息文本内容", example = "你好！")
        private String content;

        @Schema(description = "消息主键 ID")
        private String id;

        @Schema(description = "引用来源详情数据")
        private Object reference;
    }
}

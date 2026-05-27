package com.ruinet.ragflow.pojo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * @author 中锐网络
 */
@Data
@Schema(description = "智能体响应VO")
public class RagFlowAgentRespVO {

    @Schema(description = "智能体ID")
    private String id;

    @Schema(description = "智能体名称")
    private String title;

    @Schema(description = "智能体描述")
    private String description;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "画布类型")
    @JsonProperty("canvas_type")
    private String canvasType;

    @Schema(description = "创建日期")
    @JsonProperty("create_date")
    private String createDate;

    @Schema(description = "创建时间戳")
    @JsonProperty("create_time")
    private Long createTime;

    @Schema(description = "更新日期")
    @JsonProperty("update_date")
    private String updateDate;

    @Schema(description = "更新时间戳")
    @JsonProperty("update_time")
    private Long updateTime;

    @Schema(description = "用户ID")
    @JsonProperty("user_id")
    private String userId;

    @Schema(description = "画布DSL")
    private Map<String, Object> dsl;
}

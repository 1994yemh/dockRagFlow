package com.ruinet.ragflow.pojo.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * RAGFlow 知识库数据集数据出参。
 *
 * @author 中锐网络
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "RAGFlow 知识库数据集出参")
public class RagFlowDatasetRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "知识库主键 ID", example = "6e211ee0723611efa10a0242ac120007")
    private String id;

    @Schema(description = "知识库名称", example = "mysql")
    private String name;

    @Schema(description = "知识库头像图片")
    private String avatar;

    @Schema(description = "知识库描述")
    private String description;

    @Schema(description = "关联的文档总数", example = "1")
    private Integer documentCount;

    @Schema(description = "已切片的分片总数", example = "59")
    private Integer chunkCount;
}

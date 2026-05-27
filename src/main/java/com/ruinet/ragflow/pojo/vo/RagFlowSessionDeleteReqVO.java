package com.ruinet.ragflow.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * RAGFlow 会话删除请求参数。
 *
 * @author 中锐网络
 */
@Data
@Schema(description = "RAGFlow 会话删除请求参数")
public class RagFlowSessionDeleteReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "要删除的会话 ID 列表，如果删除全部可以不传", example = "[\"4606b4ec87ad11efbc4f0242ac120006\"]")
    private List<String> ids;

    @Schema(description = "是否删除所有会话（当 ids 未传、为 null 或为空数组时生效）", example = "false")
    private Boolean deleteAll = false;
}

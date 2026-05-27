package com.ruinet.ragflow.pojo.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * RAGFlow 聊天助手数据响应视图对象。
 *
 * @author 中锐网络
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "RAGFlow 聊天助手数据出参")
public class RagFlowChatAssistantRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "聊天助手主键 ID", example = "b1f2f15691f911ef81180242ac120003")
    private String id;

    @Schema(description = "聊天助手名字", example = "新聊天助手名字")
    private String name;

    @Schema(description = "Base64 编码的头像图片")
    private String icon;

    @Schema(description = "关联的知识库 ID 列表", example = "[\"527fa74891e811ef9c650242ac120006\"]")
    private List<String> datasetIds = new ArrayList<>();

    @Schema(description = "关联的知识库名称列表", example = "[\"dataset_1\"]")
    private List<String> kbNames = new ArrayList<>();

    @Schema(description = "描述", example = "A helpful Assistant")
    private String description;

    @Schema(description = "语言", example = "English")
    private String language;

    @Schema(description = "绑定的底层大模型 ID", example = "qwen-plus@Tongyi-Qianwen")
    private String llmId;

    @Schema(description = "底层大模型参数设置")
    private LlmSettingVO llmSetting;

    @Schema(description = "角色提示词配置")
    private PromptConfigVO promptConfig;

    @Schema(description = "相似度阈值", example = "0.2")
    private Double similarityThreshold;

    @Schema(description = "向量相似度权重", example = "0.3")
    private Double vectorSimilarityWeight;

    @Schema(description = "Top N 检索条数", example = "6")
    private Integer topN;

    @Schema(description = "Top K 检索候选块数", example = "1024")
    private Integer topK;

    @Schema(description = "重排模型 ID", example = "")
    private String rerankId;

    @Schema(description = "Prompt 类型", example = "simple")
    private String promptType;

    @Schema(description = "是否启用引文标注与来源展示 (1为显示, 0为关闭)", example = "1")
    private String doRefer;

    @Schema(description = "聊天助手状态 ('1' 表示启用, '0' 表示禁用)", example = "1")
    private String status;

    @Schema(description = "租户 ID", example = "69736c5e723611efb51b0242ac120007")
    private String tenantId;

    @Schema(description = "创建日期字符串", example = "Thu, 24 Oct 2024 11:18:29 GMT")
    private String createDate;

    @Schema(description = "创建时间戳", example = "1729768709023")
    private Long createTime;

    @Schema(description = "更新日期字符串", example = "Thu, 24 Oct 2024 11:18:29 GMT")
    private String updateDate;

    @Schema(description = "更新时间戳", example = "1729768709023")
    private Long updateTime;

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "大模型设置")
    public static class LlmSettingVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "模型类型", example = "chat")
        private String modelType;

        @Schema(description = "温度", example = "0.1")
        private Double temperature;

        @Schema(description = "Top P", example = "0.3")
        private Double topP;

        @Schema(description = "存在惩罚", example = "0.4")
        private Double presencePenalty;

        @Schema(description = "频率惩罚", example = "0.7")
        private Double frequencyPenalty;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "提示词角色配置")
    public static class PromptConfigVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "系统角色 Prompt 设定", example = "You are an intelligent assistant...")
        private String system;

        @Schema(description = "开场白", example = "您好！我是您的智能搜索助理。")
        private String prologue;

        @Schema(description = "未匹配知识库时的空回复词", example = "抱歉！在知识库中未找到相关内容。")
        private String emptyResponse;

        @Schema(description = "是否开启引文溯源与引文标注", example = "true")
        private Boolean quote;

        @Schema(description = "是否启用关键词检索匹配", example = "false")
        private Boolean keyword;

        @Schema(description = "是否启用语音合成(TTS)", example = "false")
        private Boolean tts;

        @Schema(description = "是否提炼多轮对话", example = "false")
        private Boolean refineMultiturn;

        @Schema(description = "是否使用知识图谱", example = "false")
        private Boolean useKg;

        @Schema(description = "是否开启深度思考/推理", example = "false")
        private Boolean reasoning;

        @Schema(description = "跨语言检索列表", example = "[]")
        private List<String> crossLanguages = new ArrayList<>();

        @Schema(description = "Tavily Web 检索密钥", example = "")
        private String tavilyApiKey;

        @Schema(description = "目录增强", example = "false")
        private Boolean tocEnhance;

        @Schema(description = "系统 Prompt 中使用的变量参数列表")
        private List<ParameterVO> parameters = new ArrayList<>();
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "Prompt 关联参数项")
    public static class ParameterVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "变量键名", example = "knowledge")
        private String key;

        @Schema(description = "是否可选", example = "false")
        private Boolean optional;
    }
}

package com.ruinet.ragflow.pojo.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建 RAGFlow 聊天助手请求视图对象。
 *
 * @author 中锐网络
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "创建 RAGFlow 聊天助手请求入参")
public class RagFlowChatAssistantCreateReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "聊天助手名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "新聊天助手名字")
    @NotBlank(message = "聊天助手名称不能为空")
    private String name;

    @Schema(description = "Base64 编码的头像图片", example = "")
    private String icon = "";

    @Schema(description = "聊天助手描述", example = "API 完整参数创建 Chat，用于验证 Web 端是否可编辑保存")
    private String description = "API 完整参数创建 Chat，用于验证 Web 端是否可编辑保存";

    @Schema(description = "智能体 Prompt 模式类型，默认 simple", example = "simple")
    private String promptType = "simple";

    @Schema(description = "是否启用引用标注与来源展示 (1为显示, 0为关闭)", example = "1")
    private String doRefer = "1";

    @Schema(description = "关联的知识库 ID 列表", example = "[\"0b2cbc8c877f11ef89070242ac120005\"]")
    private List<String> datasetIds = new ArrayList<>();

    @Schema(description = "指定的底层大模型 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "qwen3-32b@Tongyi-Qianwen")
    @NotBlank(message = "指定的底层大模型 ID 不能为空")
    private String llmId = "qwen3-32b@Tongyi-Qianwen";

    @Schema(description = "底层大模型参数设置")
    @Valid
    private LlmSettingVO llmSetting = new LlmSettingVO();

    @Schema(description = "角色提示词配置")
    @Valid
    private PromptConfigVO promptConfig = new PromptConfigVO();

    @Schema(description = "相似度阈值", example = "0.2")
    private Double similarityThreshold = 0.2;

    @Schema(description = "向量相似度权重", example = "0.3")
    private Double vectorSimilarityWeight = 0.3;

    @Schema(description = "Top N 检索条数", example = "8")
    private Integer topN = 8;

    @Schema(description = "Top K 检索候选块数", example = "1024")
    private Integer topK = 1024;

    @Schema(description = "重排模型 ID", example = "qwen3-rerank@Tongyi-Qianwen")
    private String rerankId = "qwen3-rerank@Tongyi-Qianwen";

    /**
     * 大模型参数配置。
     */
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "大模型设置")
    public static class LlmSettingVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "模型类型，支持 chat 和 image2text", example = "chat")
        private String modelType = "chat";

        @Schema(description = "温度", example = "0.1")
        private Double temperature = 0.1;

        @Schema(description = "Top P", example = "0.3")
        private Double topP = 0.3;

        @Schema(description = "存在惩罚", example = "0.4")
        private Double presencePenalty = 0.4;

        @Schema(description = "频率惩罚", example = "0.7")
        private Double frequencyPenalty = 0.7;
    }

    /**
     * 提示词角色配置。
     */
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "提示词角色配置")
    public static class PromptConfigVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "系统角色 Prompt 设定", example = "你是一个专业且严谨的智能搜索助手...")
        private String system = "你是一个专业且严谨的智能搜索助手。请根据提供的知识库内容，对用户的问题进行深刻的理解与条理清晰的归纳总结。回答应结构分明、专业精炼，避免直接照搬大段未处理的原始文本。如果知识库中存在相关内容，请用您自己的话配合 Markdown 格式排版进行解答。当所有知识库内容都与问题无关时，调用llm语言模型获取相关答案，回答问题。\n以下是知识库：\n{knowledge}\n以上是知识库。";

        @Schema(description = "开场白", example = "您好！我是您的智能搜索助理。")
        private String prologue = "您好！我是您的智能搜索助理。";

        @Schema(description = "未匹配知识库时的空回复词", example = "")
        private String emptyResponse = "";

        @Schema(description = "是否开启引文溯源与引文标注", example = "true")
        private Boolean quote = true;

        @Schema(description = "是否启用关键词检索匹配", example = "false")
        private Boolean keyword = false;

        @Schema(description = "是否启用语音合成(TTS)", example = "false")
        private Boolean tts = false;

        @Schema(description = "是否提炼多轮对话", example = "false")
        private Boolean refineMultiturn = false;

        @Schema(description = "是否使用知识图谱", example = "false")
        private Boolean useKg = false;

        @Schema(description = "是否开启深度思考/推理", example = "false")
        private Boolean reasoning = false;

        @Schema(description = "跨语言检索列表", example = "[]")
        private List<String> crossLanguages = new ArrayList<>();

        @Schema(description = "Tavily Web 检索密钥", example = "")
        private String tavilyApiKey = "";

        @Schema(description = "目录增强", example = "false")
        private Boolean tocEnhance = false;

        @Schema(description = "系统 Prompt 中使用的变量参数列表")
        @Valid
        private List<ParameterVO> parameters = new ArrayList<>();
    }

    /**
     * Prompt 关联参数项定义。
     */
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

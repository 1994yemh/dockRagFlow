/**
 * RAGFlow 聊天助手控制台 — 全局配置与表单模板
 * @author 中锐网络
 */

// Tailwind CSS 配置
tailwind.config = {
    theme: {
        extend: {
            fontFamily: {
                sans: ['Inter', 'Outfit', 'sans-serif'],
                mono: ['Fira Code', 'monospace']
            }
        }
    }
};

/**
 * 创建聊天助手表单的默认模板对象
 * @returns {Object} 包含所有表单字段的默认值
 */
function createDefaultForm() {
    return {
        name: '',
        icon: '',
        description: 'API 完整参数创建 Chat，用于验证 Web 端是否可编辑保存',
        prompt_type: 'simple',
        do_refer: '1',
        dataset_ids: [],
        llm_id: 'qwen3-32b@Tongyi-Qianwen',
        llm_setting: {
            model_type: 'chat',
            temperature: 0.1,
            top_p: 0.3,
            presence_penalty: 0.4,
            frequency_penalty: 0.7
        },
        prompt_config: {
            system: '你是一个专业且严谨的智能搜索助手。请根据提供的知识库内容，对用户的问题进行深刻的理解与条理清晰的归纳总结。回答应结构分明、专业精炼，避免直接照搬大段未处理的原始文本。如果知识库中存在相关内容，请用您自己的话配合 Markdown 格式排版进行解答。当所有知识库内容都与问题无关时，调用llm语言模型获取相关答案，回答问题。\n以下是知识库：\n{knowledge}\n以上是知识库。',
            prologue: '您好！我是您的智能搜索助理。',
            empty_response: '',
            quote: true,
            keyword: false,
            tts: false,
            refine_multiturn: false,
            use_kg: false,
            reasoning: false,
            cross_languages: [],
            tavily_api_key: '',
            toc_enhance: false,
            parameters: []
        },
        similarity_threshold: 0.2,
        vector_similarity_weight: 0.3,
        top_n: 8,
        top_k: 1024,
        rerank_id: 'qwen3-rerank@Tongyi-Qianwen'
    };
}

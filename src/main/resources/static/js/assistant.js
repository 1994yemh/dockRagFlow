/**
 * RAGFlow 聊天助手控制台 — Tab1 聊天助手管理模块
 * 包含助手 CRUD、表单管理、知识库多选、头像上传等逻辑
 * @author 中锐网络
 */

/**
 * 聊天助手管理 Composition 模块
 * @param {Function} addLog 日志记录函数
 * @returns {Object} 响应式状态和方法
 */
function useAssistantModule(addLog) {
    const { ref, computed } = Vue;

    // ==========================================
    //  响应式状态
    // ==========================================
    const list = ref([]);
    const datasetList = ref([]);
    const filterKeywords = ref('');
    const form = ref(createDefaultForm());
    const isEditMode = ref(false);
    const editId = ref('');

    // ==========================================
    //  计算属性
    // ==========================================

    /** 模糊搜索过滤后的助手列表 */
    const filteredList = computed(() => {
        if (!filterKeywords.value.trim()) return list.value;
        const kw = filterKeywords.value.toLowerCase();
        return list.value.filter(item =>
            item.name.toLowerCase().includes(kw) ||
            item.id.toLowerCase().includes(kw)
        );
    });

    /** 跨语言翻译列表的双向绑定计算属性 */
    const crossLanguagesText = computed({
        get: () => form.value.prompt_config.cross_languages ? form.value.prompt_config.cross_languages.join(', ') : '',
        set: (val) => {
            form.value.prompt_config.cross_languages = val.split(/[,\n]/)
                .map(s => s.trim())
                .filter(s => s.length > 0);
        }
    });

    // ==========================================
    //  知识库多选
    // ==========================================

    /** 切换知识库数据集的选中状态 */
    const toggleDataset = (dbId) => {
        const idx = form.value.dataset_ids.indexOf(dbId);
        if (idx > -1) {
            form.value.dataset_ids.splice(idx, 1);
        } else {
            form.value.dataset_ids.push(dbId);
        }
    };

    // ==========================================
    //  参数管理
    // ==========================================

    /** 添加自定义参数变量 */
    const addParameter = () => {
        if (!form.value.prompt_config.parameters) {
            form.value.prompt_config.parameters = [];
        }
        form.value.prompt_config.parameters.push({ key: '', optional: false });
    };

    /** 移除指定索引的参数变量 */
    const removeParameter = (idx) => {
        form.value.prompt_config.parameters.splice(idx, 1);
    };

    // ==========================================
    //  头像上传（Base64 编码 + Canvas 居中裁剪压缩）
    // ==========================================

    const uploadAvatar = (event) => {
        const file = event.target.files[0];
        if (!file) return;
        if (!file.type.startsWith('image/')) {
            alert('请确保选择的文件是图片！');
            return;
        }

        if (file.size > 20 * 1024 * 1024) {
            alert('选择的图片文件过大，请选择 20MB 以内的图片！');
            return;
        }

        const reader = new FileReader();
        reader.onload = (e) => {
            const img = new Image();
            img.onload = () => {
                const canvas = document.createElement('canvas');
                const ctx = canvas.getContext('2d');
                canvas.width = 200;
                canvas.height = 200;

                // 居中正方形裁剪算法
                let sx = 0, sy = 0, sWidth = img.width, sHeight = img.height;
                if (img.width > img.height) {
                    sWidth = img.height;
                    sx = (img.width - img.height) / 2;
                } else if (img.height > img.width) {
                    sHeight = img.width;
                    sy = (img.height - img.width) / 2;
                }

                ctx.drawImage(img, sx, sy, sWidth, sHeight, 0, 0, 200, 200);
                const compressedBase64 = canvas.toDataURL('image/jpeg', 0.8);
                form.value.icon = compressedBase64;
                addLog('SUCCESS', '图片自动居中裁剪压缩成功', {
                    original_size: (file.size / 1024).toFixed(1) + ' KB',
                    compressed_size: (compressedBase64.length / 1024).toFixed(1) + ' KB'
                }, {});
            };
            img.onerror = () => {
                alert('图片文件解析失败！');
            };
            img.src = e.target.result;
        };
        reader.readAsDataURL(file);
    };

    // ==========================================
    //  AJAX CRUD 操作
    // ==========================================

    /** 获取聊天助手列表 */
    const fetchList = async () => {
        try {
            const data = await RagFlowAPI.listChatAssistants();
            list.value = Array.isArray(data) ? data : [];
        } catch (e) {
            console.error('获取聊天助手列表失败', e);
            addLog('ERROR', 'GET /list-chat-assistants (加载助理列表)', {}, { error: e.message });
        }
    };

    /** 获取知识库数据集列表 */
    const fetchDatasets = async () => {
        try {
            const data = await RagFlowAPI.listDatasets();
            datasetList.value = Array.isArray(data) ? data : [];
            addLog('SUCCESS', 'GET /list-datasets (加载系统知识库列表)', {}, data);
        } catch (e) {
            console.error('获取知识库列表失败', e);
            addLog('ERROR', 'GET /list-datasets (加载系统知识库失败)', {}, { error: e.message });
        }
    };

    /** 重置表单到默认状态 */
    const resetForm = () => {
        form.value = createDefaultForm();
        isEditMode.value = false;
        editId.value = '';
    };

    /** 保存（创建/更新）聊天助手 */
    const saveAssistant = async () => {
        const actionName = isEditMode.value ? '更新聊天助手' : '创建聊天助手';
        const payload = { ...form.value };
        if (isEditMode.value) {
            payload.id = editId.value;
        }

        try {
            const result = isEditMode.value
                ? await RagFlowAPI.updateChatAssistant(payload)
                : await RagFlowAPI.createChatAssistant(payload);

            if (result.ok) {
                addLog('SUCCESS', `POST (${actionName})`, payload, result.data);
                alert(`${actionName}成功！`);
                resetForm();
                fetchList();
            } else {
                addLog('ERROR', `POST (${actionName})`, payload, result.data);
                alert(`操作失败: ${result.data.message || '未知错误'}`);
            }
        } catch (e) {
            addLog('ERROR', `POST (${actionName})`, payload, { error: e.message });
            alert(`发送请求异常: ${e.message}`);
        }
    };

    /** 加载助手数据到表单进行编辑 */
    const loadToEdit = (item) => {
        const itemCopy = JSON.parse(JSON.stringify(item));

        if (!itemCopy.llm_setting) itemCopy.llm_setting = {};
        if (!itemCopy.prompt_config) itemCopy.prompt_config = {};
        if (!itemCopy.prompt_config.parameters) itemCopy.prompt_config.parameters = [];

        form.value = {
            name: itemCopy.name || '',
            icon: itemCopy.icon || '',
            description: itemCopy.description || '',
            prompt_type: itemCopy.prompt_type || 'simple',
            do_refer: itemCopy.do_refer || '1',
            dataset_ids: itemCopy.dataset_ids || [],
            llm_id: itemCopy.llm_id || 'qwen3-32b@Tongyi-Qianwen',
            llm_setting: {
                model_type: itemCopy.llm_setting.model_type || 'chat',
                temperature: itemCopy.llm_setting.temperature ?? 0.1,
                top_p: itemCopy.llm_setting.top_p ?? 0.3,
                presence_penalty: itemCopy.llm_setting.presence_penalty ?? 0.4,
                frequency_penalty: itemCopy.llm_setting.frequency_penalty ?? 0.7
            },
            prompt_config: {
                system: itemCopy.prompt_config.system || '',
                prologue: itemCopy.prompt_config.prologue || '',
                empty_response: itemCopy.prompt_config.empty_response || '',
                quote: itemCopy.prompt_config.quote ?? true,
                keyword: itemCopy.prompt_config.keyword ?? false,
                tts: itemCopy.prompt_config.tts ?? false,
                refine_multiturn: itemCopy.prompt_config.refine_multiturn ?? false,
                use_kg: itemCopy.prompt_config.use_kg ?? false,
                reasoning: itemCopy.prompt_config.reasoning ?? false,
                cross_languages: itemCopy.prompt_config.cross_languages || [],
                tavily_api_key: itemCopy.prompt_config.tavily_api_key || '',
                toc_enhance: itemCopy.prompt_config.toc_enhance ?? false,
                parameters: itemCopy.prompt_config.parameters || []
            },
            similarity_threshold: itemCopy.similarity_threshold ?? 0.2,
            vector_similarity_weight: itemCopy.vector_similarity_weight ?? 0.3,
            top_n: itemCopy.top_n ?? 8,
            top_k: itemCopy.top_k ?? 1024,
            rerank_id: itemCopy.rerank_id || 'qwen3-rerank@Tongyi-Qianwen'
        };

        isEditMode.value = true;
        editId.value = itemCopy.id;

        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    /** 删除聊天助手（需要外部传入 session 相关的清理回调） */
    const deleteAssistant = async (id, onDeleted) => {
        if (!confirm('您确定要彻底删除该聊天助手吗？此操作无法撤销。')) return;

        try {
            const result = await RagFlowAPI.deleteChatAssistant(id);
            if (result.ok) {
                addLog('SUCCESS', `DELETE (删除聊天助手)`, { id }, result.data);
                alert('删除成功！');
                if (onDeleted) onDeleted(id);
                fetchList();
            } else {
                addLog('ERROR', `DELETE (删除聊天助手)`, { id }, result.data);
                alert(`删除失败: ${result.data.message || '未知错误'}`);
            }
        } catch (e) {
            addLog('ERROR', `DELETE (删除聊天助手)`, { id }, { error: e.message });
            alert(`删除异常: ${e.message}`);
        }
    };

    return {
        // 状态
        list, datasetList, filterKeywords, filteredList,
        form, isEditMode, editId, crossLanguagesText,
        // 方法
        toggleDataset, addParameter, removeParameter, uploadAvatar,
        fetchList, fetchDatasets, resetForm, saveAssistant, loadToEdit, deleteAssistant
    };
}

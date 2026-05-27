/**
 * RAGFlow 聊天助手控制台 — Vue3 主应用入口
 * 组合各模块并挂载到 #app
 * @author 中锐网络
 */
const { createApp, ref } = Vue;

createApp({
    setup() {
        // ==========================================
        //  公共状态
        // ==========================================

        /** 当前激活的导航 Tab */
        const activeTab = ref('assistant');

        /** 折叠面板状态 */
        const sections = ref({
            llm: true,
            prompt: false,
            retrieval: false
        });

        /** 切换折叠面板 */
        const toggleSection = (sec) => {
            sections.value[sec] = !sections.value[sec];
        };

        /** 调试日志列表 */
        const logs = ref([]);

        /** 日志记录工具函数 */
        const addLog = (type, action, req, resp) => {
            const time = new Date().toLocaleTimeString();
            logs.value.unshift({ time, type, action, req, resp });
            if (logs.value.length > 10) logs.value.pop();
        };

        // ==========================================
        //  模块初始化
        // ==========================================

        // Tab1: 聊天助手管理模块
        const assistant = useAssistantModule(addLog);

        // Tab2: 会话管理与流式对话模块
        const session = useSessionModule(addLog);

        // ==========================================
        //  模块间交互桥接
        // ==========================================

        /** 包装 switchToSessionTab，传入 activeTab 引用 */
        const switchToSessionTab = (item) => {
            session.switchToSessionTab(item, activeTab);
        };

        /** 包装 deleteAssistant，注入 session 清理回调 */
        const deleteAssistant = (id) => {
            assistant.deleteAssistant(id, session.onAssistantDeleted);
        };

        /** 包装 loadToEdit，展开所有折叠面板 */
        const loadToEdit = (item) => {
            assistant.loadToEdit(item);
            sections.value.llm = true;
            sections.value.prompt = true;
            sections.value.retrieval = true;
        };

        // ==========================================
        //  初始化加载
        // ==========================================
        assistant.fetchList();
        assistant.fetchDatasets();

        // ==========================================
        //  暴露给模板的所有属性和方法
        // ==========================================
        return {
            // 公共
            activeTab,
            sections,
            toggleSection,
            logs,

            // 助手管理（Tab1）
            list: assistant.list,
            datasetList: assistant.datasetList,
            filterKeywords: assistant.filterKeywords,
            filteredList: assistant.filteredList,
            form: assistant.form,
            isEditMode: assistant.isEditMode,
            crossLanguagesText: assistant.crossLanguagesText,
            toggleDataset: assistant.toggleDataset,
            addParameter: assistant.addParameter,
            removeParameter: assistant.removeParameter,
            uploadAvatar: assistant.uploadAvatar,
            fetchList: assistant.fetchList,
            resetForm: assistant.resetForm,
            saveAssistant: assistant.saveAssistant,
            loadToEdit,
            deleteAssistant,

            // 会话管理（Tab2）
            activeSandboxChat: session.activeSandboxChat,
            activeSessionId: session.activeSessionId,
            sessionList: session.sessionList,
            chatMessages: session.chatMessages,
            userInput: session.userInput,
            chatTyping: session.chatTyping,
            chatBox: session.chatBox,
            switchToSessionTab,
            createNewSession: session.createNewSession,
            selectSession: session.selectSession,
            renameSession: session.renameSession,
            deleteSession: session.deleteSession,
            clearAllSessions: session.clearAllSessions,
            sendMessage: session.sendMessage,
            renderMarkdown: session.renderMarkdown
        };
    }
}).mount('#app');

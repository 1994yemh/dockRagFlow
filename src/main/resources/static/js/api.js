/**
 * RAGFlow 聊天助手控制台 — API 请求封装层
 * 封装所有与后端 REST API 的通信逻辑
 * @author 中锐网络
 */
const RagFlowAPI = {

    // ==========================================
    //  聊天助手 CRUD
    // ==========================================

    /**
     * 获取聊天助手列表
     * @returns {Promise<Array>}
     */
    async listChatAssistants() {
        const res = await fetch('/api/v1/ragflow-chat/list-chat-assistants');
        return await res.json();
    },

    /**
     * 创建聊天助手
     * @param {Object} payload 助手配置
     * @returns {Promise<{ok: boolean, data: Object}>}
     */
    async createChatAssistant(payload) {
        const res = await fetch('/api/v1/ragflow-chat/create-chat-assistant', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        return { ok: res.ok && (!data.code || data.code === 0), data };
    },

    /**
     * 更新聊天助手
     * @param {Object} payload 助手配置（包含 id）
     * @returns {Promise<{ok: boolean, data: Object}>}
     */
    async updateChatAssistant(payload) {
        const res = await fetch('/api/v1/ragflow-chat/update-chat-assistant', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        return { ok: res.ok && (!data.code || data.code === 0), data };
    },

    /**
     * 删除聊天助手
     * @param {string} id 助手 ID
     * @returns {Promise<{ok: boolean, data: Object}>}
     */
    async deleteChatAssistant(id) {
        const res = await fetch(`/api/v1/ragflow-chat/delete-chat-assistant/${id}`, {
            method: 'DELETE'
        });
        const data = await res.json();
        return { ok: res.ok, data };
    },

    // ==========================================
    //  知识库数据集
    // ==========================================

    /**
     * 获取知识库列表
     * @returns {Promise<Array>}
     */
    async listDatasets() {
        const res = await fetch('/api/v1/ragflow-chat/list-datasets');
        return await res.json();
    },

    // ==========================================
    //  会话管理
    // ==========================================

    /**
     * 获取指定助手的会话列表
     * @param {string} chatId 聊天助手 ID
     * @returns {Promise<{ok: boolean, data: Object}>}
     */
    async listSessions(chatId) {
        const res = await fetch(`/api/v1/ragflow-chat/list-sessions/${chatId}`);
        const data = await res.json();
        return { ok: res.ok, data };
    },

    /**
     * 创建新会话
     * @param {string} chatId 聊天助手 ID
     * @param {Object} payload 包含 name 字段
     * @returns {Promise<{ok: boolean, data: Object}>}
     */
    async createSession(chatId, payload) {
        const res = await fetch(`/api/v1/ragflow-chat/create-session/${chatId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        return { ok: res.ok && !!data.id, data };
    },

    /**
     * 更新会话名称
     * @param {string} chatId 聊天助手 ID
     * @param {string} sessionId 会话 ID
     * @param {Object} payload 包含 name 字段
     * @returns {Promise<{ok: boolean, data: Object}>}
     */
    async updateSession(chatId, sessionId, payload) {
        const res = await fetch(`/api/v1/ragflow-chat/update-session/${chatId}/${sessionId}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        return { ok: res.ok, data };
    },

    /**
     * 获取会话详情（含消息历史）
     * @param {string} chatId 聊天助手 ID
     * @param {string} sessionId 会话 ID
     * @returns {Promise<{ok: boolean, data: Object}>}
     */
    async getSessionDetails(chatId, sessionId) {
        const res = await fetch(`/api/v1/ragflow-chat/get-session/${chatId}/${sessionId}`);
        const data = await res.json();
        return { ok: res.ok, data };
    },

    /**
     * 删除会话（支持批量或全部清空）
     * @param {string} chatId 聊天助手 ID
     * @param {Object} payload 包含 ids 数组或 delete_all 布尔值
     * @returns {Promise<{ok: boolean, data: Object}>}
     */
    async deleteSessions(chatId, payload) {
        const res = await fetch(`/api/v1/ragflow-chat/delete-sessions/${chatId}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        return { ok: res.ok, data };
    },

    // ==========================================
    //  流式对话
    // ==========================================

    /**
     * 发送流式对话请求（返回原始 Response 用于流式读取）
     * @param {Object} payload 包含 chat_id, session_id, messages
     * @returns {Promise<Response>}
     */
    async sendChatFlow(payload) {
        return await fetch('/api/v1/ragflow-chat/send-chat-flow', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
    }
};

/**
 * RAGFlow 聊天助手控制台 — Tab2 会话管理与流式对话模块
 * 包含会话 CRUD、流式打字机、Markdown 渲染等逻辑
 * @author 中锐网络
 */

/**
 * 会话管理 Composition 模块
 * @param {Function} addLog 日志记录函数
 * @returns {Object} 响应式状态和方法
 */
function useSessionModule(addLog, showModal) {
    const { ref, nextTick } = Vue;

    // ==========================================
    //  响应式状态
    // ==========================================
    const activeSandboxChat = ref(null);
    const activeSessionId = ref('');
    const sessionList = ref([]);
    const chatMessages = ref([]);
    const userInput = ref('');
    const chatTyping = ref(false);
    const chatBox = ref(null);
    const activeReference = ref(null);      // 当前活跃引文对象
    const activeMessageIndex = ref(null);    // 当前高亮选中的消息索引

    // 打字机缓冲队列与定时器状态（非响应式）
    let charQueue = [];
    let typingInterval = null;
    let isStreaming = false;
    let replyText = '';

    // ==========================================
    //  会话切换
    // ==========================================

    /**
     * 从助手列表页切换到会话管理页
     * @param {Object} item 聊天助手对象
     * @param {Ref} activeTab 当前激活的 Tab 引用
     */
    const switchToSessionTab = (item, activeTab) => {
        activeSandboxChat.value = item;
        activeTab.value = 'session';
        loadSessions(item.id);
    };

    // ==========================================
    //  会话 CRUD
    // ==========================================

    /** 加载指定助手的会话列表 */
    const loadSessions = async (chatId) => {
        try {
            const result = await RagFlowAPI.listSessions(chatId);
            if (result.ok) {
                sessionList.value = result.data || [];
                addLog('SUCCESS', `GET (获取会话列表)`, { chatId }, result.data);
                if (sessionList.value.length > 0) {
                    selectSession(sessionList.value[0].id);
                } else {
                    activeSessionId.value = '';
                    chatMessages.value = [];
                }
            } else {
                addLog('ERROR', `GET (获取会话列表)`, { chatId }, result.data);
                alert('获取会话列表失败：' + (result.data.message || '未知错误'));
            }
        } catch (e) {
            addLog('ERROR', `GET (获取会话列表异常)`, { chatId }, { error: e.message });
        }
    };

    /** 创建新会话 */
    const createNewSession = async () => {
        if (!activeSandboxChat.value) return;
        const chatId = activeSandboxChat.value.id;
        const defaultName = `会话 ${sessionList.value.length + 1}`;
        
        showModal({
            title: '新建会话',
            message: '请输入新会话的名称：',
            type: 'prompt',
            defaultValue: defaultName,
            placeholder: '输入会话名称...',
            onConfirm: async (sName) => {
                if (!sName || !sName.trim()) return;
                const payload = { name: sName.trim() };
                try {
                    const result = await RagFlowAPI.createSession(chatId, payload);
                    if (result.ok) {
                        addLog('SUCCESS', `POST (新建会话)`, payload, result.data);
                        sessionList.value.push(result.data);
                        selectSession(result.data.id);
                    } else {
                        addLog('ERROR', `POST (新建会话失败)`, payload, result.data);
                        showModal({ title: '创建失败', message: '创建会话失败：' + (result.data.message || '未知错误'), type: 'alert' });
                    }
                } catch (e) {
                    addLog('ERROR', `POST (新建会话异常)`, payload, { error: e.message });
                    showModal({ title: '发生异常', message: '发送请求异常：' + e.message, type: 'alert' });
                }
            }
        });
    };

    /** 选择会话并加载对话历史 */
    const selectSession = async (sesId) => {
        // 主动打断上轮打字机
        isStreaming = false;
        chatTyping.value = false;
        charQueue = [];
        if (typingInterval) {
            clearInterval(typingInterval);
            typingInterval = null;
        }

        activeSessionId.value = sesId;
        if (!activeSandboxChat.value || !sesId) return;
        const chatId = activeSandboxChat.value.id;

        try {
            const result = await RagFlowAPI.getSessionDetails(chatId, sesId);
            if (result.ok && result.data) {
                // 排除系统开场白
                let msgList = result.data.messages || [];
                if (msgList.length > 0 && msgList[0].role === 'assistant') {
                    msgList = msgList.slice(1);
                }
                chatMessages.value = msgList;
                // 自动拉取最后一条 assistant 消息的 reference 作为初始展示
                const lastAssistantMsg = [...msgList].reverse().find(m => m.role === 'assistant');
                activeReference.value = lastAssistantMsg?.reference || null;
                activeMessageIndex.value = lastAssistantMsg ? msgList.indexOf(lastAssistantMsg) : null;
                addLog('SUCCESS', `GET (获取会话详情)`, { chatId, sesId }, result.data);
                scrollToBottom();
            } else {
                addLog('ERROR', `GET (获取会话详情失败)`, { chatId, sesId }, result.data);
            }
        } catch (e) {
            addLog('ERROR', `GET (获取会话详情异常)`, { chatId, sesId }, { error: e.message });
        }
    };

    /** 重命名会话 */
    const renameSession = async (ses) => {
        if (!activeSandboxChat.value) return;
        const chatId = activeSandboxChat.value.id;
        
        showModal({
            title: '会话重命名',
            message: '请输入新的会话名称：',
            type: 'prompt',
            defaultValue: ses.name,
            placeholder: '输入新的名称...',
            onConfirm: async (newName) => {
                if (!newName || !newName.trim()) return;
                const payload = { name: newName.trim() };
                try {
                    const result = await RagFlowAPI.updateSession(chatId, ses.id, payload);
                    if (result.ok) {
                        addLog('SUCCESS', `PATCH (重命名会话)`, payload, result.data);
                        ses.name = result.data.name;
                    } else {
                        addLog('ERROR', `PATCH (重命名会话失败)`, payload, result.data);
                        showModal({ title: '重命名失败', message: '重命名失败：' + (result.data.message || '未知错误'), type: 'alert' });
                    }
                } catch (e) {
                    addLog('ERROR', `PATCH (重命名会话异常)`, payload, { error: e.message });
                    showModal({ title: '发生异常', message: '重命名发送异常：' + e.message, type: 'alert' });
                }
            }
        });
    };

    /** 删除单个会话 */
    const deleteSession = async (sesId) => {
        if (!activeSandboxChat.value) return;
        const chatId = activeSandboxChat.value.id;
        
        showModal({
            title: '删除会话',
            message: '您确定要删除该会话吗？历史消息记录将会清空。',
            type: 'confirm',
            onConfirm: async () => {
                const payload = { ids: [sesId], delete_all: false };
                try {
                    const result = await RagFlowAPI.deleteSessions(chatId, payload);
                    if (result.ok) {
                        addLog('SUCCESS', `DELETE (删除指定会话)`, payload, result.data);
                        sessionList.value = sessionList.value.filter(s => s.id !== sesId);
                        if (activeSessionId.value === sesId) {
                            if (sessionList.value.length > 0) {
                                selectSession(sessionList.value[0].id);
                            } else {
                                activeSessionId.value = '';
                                chatMessages.value = [];
                            }
                        }
                    } else {
                        addLog('ERROR', `DELETE (删除指定会话失败)`, payload, result.data);
                        showModal({ title: '删除失败', message: '删除会话失败：' + (result.data.message || '未知错误'), type: 'alert' });
                    }
                } catch (e) {
                    addLog('ERROR', `DELETE (删除指定会话异常)`, payload, { error: e.message });
                    showModal({ title: '发生异常', message: '删除会话发生异常：' + e.message, type: 'alert' });
                }
            }
        });
    };

    /** 清空全部会话 */
    const clearAllSessions = async () => {
        if (!activeSandboxChat.value) return;
        const chatId = activeSandboxChat.value.id;
        
        showModal({
            title: '清空全部会话',
            message: '确定清空该聊天助手下拥有的全部会话吗？此操作不可恢复。',
            type: 'confirm',
            onConfirm: async () => {
                const payload = { delete_all: true };
                try {
                    const result = await RagFlowAPI.deleteSessions(chatId, payload);
                    if (result.ok) {
                        addLog('SUCCESS', `DELETE (清空全部会话)`, payload, result.data);
                        sessionList.value = [];
                        activeSessionId.value = '';
                        chatMessages.value = [];
                    } else {
                        addLog('ERROR', `DELETE (清空全部会话失败)`, payload, result.data);
                        showModal({ title: '清空失败', message: '清空会话失败：' + (result.data.message || '未知错误'), type: 'alert' });
                    }
                } catch (e) {
                    addLog('ERROR', `DELETE (清空全部会话异常)`, payload, { error: e.message });
                    showModal({ title: '发生异常', message: '清空会话发生异常：' + e.message, type: 'alert' });
                }
            }
        });
    };

    // ==========================================
    //  流式对话（打字机缓冲队列 + SSE 字节流解析）
    // ==========================================

    const sendMessage = async () => {
        if (!userInput.value.trim() || chatTyping.value || !activeSessionId.value) {
            if (!activeSessionId.value) {
                showModal({
                    title: '提示',
                    message: '请先新建或选择一个会话以发起对话！',
                    type: 'alert'
                });
            }
            return;
        }

        const text = userInput.value;
        userInput.value = '';

        chatMessages.value.push({ role: 'user', content: text });
        activeReference.value = null; // 发送消息，清空上轮引文
        activeMessageIndex.value = null;
        scrollToBottom(false);
        chatTyping.value = true;

        // 映射为后端需要的多轮历史消息链格式
        const historyList = chatMessages.value.map(m => ({
            role: m.role,
            content: m.content
        }));

        const payload = {
            chat_id: activeSandboxChat.value.id,
            session_id: activeSessionId.value,
            messages: historyList
        };

        try {
            const res = await RagFlowAPI.sendChatFlow(payload);

            if (!res.ok) {
                const data = await res.json();
                chatMessages.value.push({ role: 'assistant', content: `【流式提问异常】：${data.message || '未获取到答案'}` });
                addLog('ERROR', `POST (流式追问失败)`, payload, data);
                chatTyping.value = false;
                scrollToBottom(true);
                return;
            }

            // 1. 推入空的 assistant 占位消息
            chatMessages.value.push({ role: 'assistant', content: '' });
            scrollToBottom(false);

            // 2. 初始化打字机状态
            charQueue = [];
            replyText = '';
            isStreaming = true;

            if (typingInterval) clearInterval(typingInterval);
            typingInterval = setInterval(() => {
                if (charQueue.length > 0) {
                    // 追赶机制：积压越多，吐字越快
                    let charsToTake = 1;
                    if (charQueue.length > 40) {
                        charsToTake = 5;
                    } else if (charQueue.length > 20) {
                        charsToTake = 3;
                    } else if (charQueue.length > 8) {
                        charsToTake = 2;
                    }

                    for (let i = 0; i < charsToTake; i++) {
                        if (charQueue.length > 0) {
                            replyText += charQueue.shift();
                        }
                    }

                    chatMessages.value[chatMessages.value.length - 1].content = replyText;
                    scrollToBottom(false);
                } else if (!isStreaming) {
                    clearInterval(typingInterval);
                    typingInterval = null;
                    chatTyping.value = false;
                    scrollToBottom(true);
                }
            }, 25);

            // 3. 流式字节读取
            const reader = res.body.getReader();
            const decoder = new TextDecoder('utf-8');
            let buffer = '';

            addLog('SUCCESS', `POST (流式连接建立成功)`, payload, {});

            while (true) {
                const { value, done } = await reader.read();
                if (done) break;

                buffer += decoder.decode(value, { stream: true });
                const lines = buffer.split('\n');
                buffer = lines.pop();

                for (const line of lines) {
                    if (line.startsWith('data:')) {
                        let jsonStr = line.substring(5);
                        if (jsonStr.startsWith(' ')) {
                            jsonStr = jsonStr.substring(1);
                        }
                        jsonStr = jsonStr.trim();

                        if (jsonStr) {
                            let textToken = '';
                            try {
                                const obj = JSON.parse(jsonStr);
                                if (obj && obj.text !== undefined) {
                                    textToken = obj.text;
                                }
                                // 流式提取引文数据，并动态关联至当前助理消息中
                                if (obj && obj.reference !== undefined && obj.reference !== null) {
                                    const currentMsg = chatMessages.value[chatMessages.value.length - 1];
                                    if (currentMsg && currentMsg.role === 'assistant') {
                                        currentMsg.reference = obj.reference;
                                        // 激活右侧引用面板的实时展示
                                        activeReference.value = obj.reference;
                                        activeMessageIndex.value = chatMessages.value.length - 1;
                                    }
                                }
                            } catch (err) {
                                textToken = jsonStr;
                            }

                            if (textToken) {
                                charQueue.push(...textToken.split(''));
                            }
                        }
                    }
                }
            }
            isStreaming = false;
        } catch (e) {
            isStreaming = false;
            chatTyping.value = false;
            if (typingInterval) {
                clearInterval(typingInterval);
                typingInterval = null;
            }
            chatMessages.value.push({ role: 'assistant', content: '【网络异常】：流式连接中断。' });
            addLog('ERROR', `POST (流式连接断开异常)`, payload, { error: e.message });
        }
        scrollToBottom(true);
    };

    // ==========================================
    //  辅助方法
    // ==========================================

    /** 滚动聊天框到底部，可选触发代码高亮 */
    const scrollToBottom = (forceHighlight = false) => {
        nextTick(() => {
            if (chatBox.value) {
                chatBox.value.scrollTop = chatBox.value.scrollHeight;
            }
            if (forceHighlight) {
                document.querySelectorAll('.markdown-body pre code').forEach(el => {
                    if (!el.dataset.highlighted) {
                        hljs.highlightElement(el);
                        el.dataset.highlighted = 'true';
                    }
                });
            }
        });
    };

    /** Markdown 渲染引擎 */
    const renderMarkdown = (text) => {
        if (!text) return '';
        try {
            return marked.parse(text, {
                breaks: true,
                gfm: true
            });
        } catch (e) {
            return text;
        }
    };

    /**
     * 当助手被删除时的清理回调
     * @param {string} deletedId 被删除的助手 ID
     */
    const onAssistantDeleted = (deletedId) => {
        if (activeSandboxChat.value && activeSandboxChat.value.id === deletedId) {
            activeSandboxChat.value = null;
            activeSessionId.value = '';
            sessionList.value = [];
        }
    };

    /** 选择并展示某条消息的引用来源 */
    const selectMessageReference = (msg, index) => {
        if (msg.role !== 'assistant') return;
        activeMessageIndex.value = index;
        activeReference.value = msg.reference || null;
    };

    return {
        // 状态
        activeSandboxChat, activeSessionId, sessionList,
        chatMessages, userInput, chatTyping, chatBox,
        activeReference, activeMessageIndex,
        // 方法
        switchToSessionTab, loadSessions, createNewSession,
        selectSession, renameSession, deleteSession, clearAllSessions,
        sendMessage, scrollToBottom, renderMarkdown, onAssistantDeleted,
        selectMessageReference
    };
}

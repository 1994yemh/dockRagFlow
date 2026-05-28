# 前端模块化拆分与会话气泡头像优化

## 上下文
- 现有 `index.html` 过于臃肿（1700行），包含了所有内联 CSS 样式和 JS 业务逻辑。
- 此前已将 CSS 拆分至 `css/app.css`，业务逻辑 JS 拆分至 `js/config.js`、`js/api.js`、`js/assistant.js`、`js/session.js`、`js/app.js`。
- 需进一步在 `index.html` 中引入这些文件，并移除内联部分。
- 此外，为优化调试会话沙盒的视觉效果，需去掉对话框中的文字“智能助理”与“您”，改为优雅的左右分栏气泡头像布局（助理使用机器人默认图标，用户使用所选聊天对话助手的头像 `activeSandboxChat.icon`）。

## 执行计划
1. **引入 CSS 链接**：移除 `index.html` 中第 33 行至第 199 行的 `<style>`，引入 `<link rel="stylesheet" href="css/app.css">`。
2. **重构消息头像气泡 UI**：
   - 重构“标准开场白 (Greeting)”板块，在气泡左侧加入默认 SVG 机器人图标，隐藏文字。
   - 重构“循环渲染消息 (chatMessages)”板块，在助理气泡左侧加入默认 SVG 机器人图标，在用户气泡右侧加入 `activeSandboxChat.icon`（或首字母）头像，隐藏文字标签。
   - 重构“流式加载中 (chatTyping)”板块，在气泡左侧加入默认 SVG 机器人图标，隐藏文字。
3. **引入拆分 JS 脚本**：移除底部的内联脚本（第 955 行至第 1698 行），改用以下外链引入：
   - `js/config.js`
   - `js/api.js`
   - `js/assistant.js`
   - `js/session.js`
   - `js/app.js`
4. **编译与功能验证**。

---

## 🪵 流式对话严重卡顿与交织错乱修复 (追加记录)

### 问题上下文
用户提问流式输出时，控制台与前端会严重卡顿约 15s，且输出文本发生语无伦次的字符交织错乱（例如内心思考文字与最终解答片段无序混杂），15s 后长度超过一定阈值又恢复正常速度。
**排查分析**：RAGFlow 原生 SSE 接口在模型进行推理时，`answer` 会先流式吐出 Thought 思考内容，正式回答时 `answer` 字段发生重置。原有 Java 后端差量截取逻辑 `fullAnswer.substring(previousAnswer[0].length())` 建立在严格单调追加的假设上。发生重写重置时，长度没有超过旧思考内容会导致 15s 的判定卡死，超过之后硬性截取子字符串造成了灾难性的内容错位和交织。

### 修复方案
在 `RagFlowChatServiceImpl.java` 中进行差量截取时，使用 `startsWith` 前缀判定机制。一旦大模型流发生非单调跳变重置（例如思考到正式答复切换），立即清空历史基准，将当前内容重新全量视为增量发出，彻底消除卡顿、乱码与越界拼接问题。

---

## ⚙️ 创建助手 llm_setting 强制空对象与系统提示词优化 (追加记录)

### 变更上下文
1. **接口入参兼容**：前端创建/更新聊天助手发送 `llm_setting` 时，虽然在前端带有温度等初始参数，但后端发往 RAGFlow 接口时，为保障 Web 端的正常编辑与保存兼容，需将 `llm_setting` 强行转换并替换为标准的空对象 `{}`，而前端无需进行任何修改，保持优雅的高聚合性。
2. **系统提示词更新**：升级全站默认的角色提示词模板，将无关情况下的警告话术升级为“调用 llm 语言模型获取相关答案，回答问题”，同时严格保留底层检索必需的 `{knowledge}` 通信后缀。

### 实现方案
1. **后端拦截**：在 `RagFlowChatServiceImpl.java` 的 `createChatAssistant` 和 `updateChatAssistant` 方法中，先使用 Jackson 的 `convertValue` 将入参转换为 `Map<String, Object>`，通过 `bodyMap.put("llm_setting", new HashMap<>())` 物理覆盖，再序列化发送。
2. **配置升级**：修改 [config.js](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/resources/static/js/config.js) 下的默认提示词文本。

---

## ❄️ 聊天卡片列表温度空白 Bug 修复 (追加记录)

### 故障上下文
在将后端发往 RAGFlow 的 `llm_setting` 强制拦截并设为空对象 `{}` 后，聊天助手卡片上的 `"温度:"` 后面出现了空白，无法再显示默认的 `0.1` 温度。
**排查分析**：原本 Vue 3 模板中的温度表达式为 `item.llm_setting ? item.llm_setting.temperature : '0.1'`。由于 `{}` 空对象不是 null/undefined，三元运算符判定为 true 并尝试去获取 `item.llm_setting.temperature` 属性。因空对象内部无该属性，其值为 `undefined`，导致 Vue 最终渲染出空白。

### 修复方案
修改 [index.html](file:///e:/myStudyProject/tool/ragFlowSearch/src/main/resources/static/index.html) 的卡片列表渲染模板，使用更高级的安全属性级联校验：
`{{ (item.llm_setting && item.llm_setting.temperature !== undefined && item.llm_setting.temperature !== null) ? item.llm_setting.temperature : '0.1' }}`。
当 `temperature` 属性未定义或为空时，能够完美、优雅地降级回退，展示系统默认温度 `'0.1'`。



## ⚙️ 恢复 llm_setting 为正常的默认参数值 (最新追加)

### 变更上下文
此前为了临时规避某些 Web 兼容报错，将 `llm_setting` 强制覆盖为空对象 `{}`。但这样导致聊天卡片温度显示空白，且无法保存具体的模型参数（如 `temperature` 等）。
为了恢复大模型设置的正常流转：
1. 前端代码保持不修改。
2. 后端全盘接管参数补齐。如果前端不传 `llm_setting` 或参数为 null，后端自动级联兜底合并为系统黄金默认参数（如 `temperature: 0.1`、`top_p: 0.3` 等）。

### 实现方案
1. **创建助手逻辑**：撤销 `createChatAssistant` 中的 Map 强转物理清空拦截，引入对 `reqVO.getLlmSetting()` 的非空级联判定补齐。使用 `objectMapper.writeValueAsString(reqVO)` 进行标准 JSON 序列化。
2. **更新助手逻辑**：同步修改 `updateChatAssistant` 方法，废除将其转为 Map 并 `bodyMap.put("llm_setting", new HashMap<>())` 覆盖的逻辑，对其引入完全相同的 `llmSetting` 级联非空补足逻辑。





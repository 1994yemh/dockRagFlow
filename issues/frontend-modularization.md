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


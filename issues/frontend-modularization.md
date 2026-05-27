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

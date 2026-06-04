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


## ⚙️ 前后端默认字段参数同步对齐 (最新追加)

### 变更上下文
为了保持前后端大模型智能体参数的统一，如果前端发起的创建/更新请求中没有覆盖某些非空属性，后端在反序列化映射类时也必须自带系统黄金默认值。
这涵盖了前端 `createDefaultForm` 方法中设定的各项非空属性，包括主模型的 `description`、`llm_id`、`rerank_id`、`top_n` 检索条数，以及 `prompt_config` 内部的角色系统 Prompt 与空匹配答复词等。

### 实现方案
1. **修改 VO 默认初始化值**：
   在 `RagFlowChatAssistantCreateReqVO.java` 中直接声明其默认值：
   *   `description` 设为 `"API 完整参数创建 Chat，用于验证 Web 端是否可编辑保存"`；
   *   `llmId` 设为 `"qwen3-32b@Tongyi-Qianwen"`；
   *   `rerankId` 设为 `"qwen3-rerank@Tongyi-Qianwen"`；
   *   `topN` 设为 `8`；
   *   `promptConfig.system` 设为与前端对齐的系统深度总结提示词模板（包含引文检索必须的 `{knowledge}` 插槽）；
   *   `promptConfig.emptyResponse` 设为与前端一致的空字符串 `""`。
2. **编译验证**：使用 `mvn clean compile` 进行了完整编译检查，结果为 **BUILD SUCCESS**。


## 📚 知识库问答引用来源详情面板集成与三栏 UI 升级 (最新追加)

### 变更上下文
为了能够在聊天对话右侧实时展示问答所命中的**知识库 ID**、**关联文档名**以及**切片内容片段**，高保真地实现了用户设计的引文追踪与溯源面板。

### 实现方案
1. **后端消息实体映射扩充**：
   在 `RagFlowSessionRespVO.MessageVO` 静态内部类中增加 `reference`（类型为 `Object`）字段，使得查询会话历史接口在利用 Jackson 转换数据时，能够原生自动反序列化并保存历史引文数据。
2. **后端流式对话接口打通**：
   重构 `RagFlowChatServiceImpl.java` 的 `sendChatFlow` 方法：将向前端逐帧推送的 SSE 增量 Map 转换为 `Map<String, Object>`，实时提取 RAGFlow 流数据中的 `reference` 节点（如果存在且非空）原样一并下发，彻底打通流式 RAG 引用源数据传递。
3. **前端状态管理与流解析增强**：
   在 `session.js` 中新增 `activeReference`（活跃引文）与 `activeMessageIndex`（高亮消息索引）响应式状态。在流式接收 SSE 帧解析时，动态捕获 `obj.reference` 并将其直接绑定在当前的 `assistant` 消息对象中。同时定义了交互函数 `selectMessageReference`。
4. **前端历史消息回显与多轮回溯**：
   当切换/加载会话时，自动拉取最后一条 `assistant` 消息对应的引用回显在右侧。当点击任何一个气泡时，右侧引用面板会自动同步切换展示该提问对应的引文卡片，提供强大的历史检索回溯能力。
5. **前端三栏响应式网格与高保真引用面板**：
   *   重构 `index.html` 的 TAB 2，调整为主流 RAG 并排网格布局：“左会话列表 (3 栏) + 中聊天窗口 (5 栏) + 右引用面板 (4 栏)”。
   *   高保真编写右侧“知识库引用”面板：在无引用数据时，优雅展示灰色书本占位图及灰色提示语；在包含引用数据时，循环渲染精美卡片（文件夹图标、文档名称、相似度百分比标签、切片内容滚动盒、具体的知识库 ID）。
   *   在 `app.css` 中追加微型滚动条样式、引文卡片 Hover 动效、占位书本发光及气泡高亮边框。
6. **编译验证**：通过 Maven 命令行运行 `mvn clean compile` 全量验证编译成功，无任何冲突 and 异常！


## ⚙️ 引文详情面板深度打磨与知识库名字映射对齐 (最新追加)

### 变更上下文
根据实际页面渲染的反馈，对三栏比例、标题字号、切片统计维度进行了打磨微调，并成功利用前端已获取的知识库列表，在引文卡片中将生涩的 `dataset_id` 转换为真实的**知识库中文名称**展现。

### 实现方案
1. **三栏网格比例优化为 2:7:3**：
   在 `index.html` 中，将历史会话面板（左）拉窄为 `col-span-2`，将引文来源面板（右）拉窄为 `col-span-3`，将腾出的宽度全部倾注于调试沙盒聊天窗口（中，拉宽为 `col-span-7`）。
   *   **效果**：页面空间利用率最大化，聊天窗口视野极其开阔，双侧比例非常高级洗炼。
2. **知识库真实名称转换对齐**：
   *   在 `static/js/app.js` 的 `setup()` 中定义并暴露了 `findDatasetName(datasetId)` 方法。
   *   **逻辑**：根据切片携带的 `dataset_id` 去前端 `assistant.datasetList` 响应式列表中比对，若匹配则返回其 `name` 属性，实现前端动态映射翻译。
   *   在 `index.html` 的切片卡片脚部，由生硬的 `"知识库 ID: dataset_id"` 升级为直观展现：`“知识库: 知识库中文名”`。
3. **引文面板头部轻量单行化**：
   *   针对“原标题大而空占了半屏”的问题，我们将引文来源面板顶部标题由原先的两行大字加分割线，压缩成精致的**单行 text-xs 紧凑横幅**：一个小巧优雅的书本图标 + `“知识库引用来源”` 的小巧标题（高度缩减了 60%）。
   *   **效果**：头部极为低调、精致，将右侧面板的 95% 以上的高度空间全部释放给引文卡片及切片正文滚动盒。
4. **消息气泡切片维度统计订正**：
   *   将助理回复底部的文案由原先不准确的 `“包含 X 个知识库引用”` 正确修正为 `“包含 X 处参考片段”`，客观指代文档切片。








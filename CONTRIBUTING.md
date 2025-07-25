# Contributing to TextToSpeechApp

感谢你对本项目的关注！请遵循以下协作规范：

## 协作规范

---

## 代码规范

---

- 所有 Java 文件需按功能分区加 `// region ... // endregion` 注释（如成员变量、UI控件、TTS相关、生命周期、事件绑定、辅助方法、回调等）。
- **分层结构要求：**
  - UI 相关类放在 `ui` 包
  - 业务逻辑类放在 `logic` 包
  - 工具类放在 `util` 包
  - 适配器类放在 `adapter` 包
  - 新增文件时请自动分层，并按功能分区加注释。
- 所有用户可见文本必须国际化，统一放入 `res/values/strings.xml`，并支持多语言（如 `values-en/strings.xml`）。
- 方法、变量、类名需见名知意，避免拼音和缩写。
- 布局、菜单、通知等资源引用必须用资源ID，不得硬编码。
- 复杂逻辑、分区、重要方法需加简明注释。

## 命名规范

---

- **类名**：大驼峰（PascalCase），如 `MainActivity`、`TextEditorActivity`、`TtsManager`、`ClearButtonHelper`。
- **方法名**：小驼峰（camelCase），如 `startSpeak()`、`updateStatusInfo()`、`setupClearButton()`。
- **变量名**：小驼峰（camelCase），如 `editText`、`btnSpeak`、`speechRate`。
- **常量名**：全大写，单词间下划线分隔，如 `AUDIO_FILE_NAME`、`PREFS_NAME`。
- **包名**：全小写，结构清晰，如 `com.citadawn.speechapp.ui`、`com.citadawn.speechapp.util`。
- **避免拼音和缩写**：除行业通用缩写（如 TTS、UI）外，变量、方法、类名均应见名知意。

## 注释规范

---

- **类注释**：每个类文件顶部可简要说明用途。
- **方法注释**：每个公开方法、复杂私有方法前加注释，说明功能、参数、返回值、异常等。
- **分区注释**：所有功能分区用 `// region ... // endregion` 注释。
- **复杂逻辑**：关键算法、业务流程、易混淆代码块处加行内注释。
- **国际化说明**：所有用户可见文本必须国际化，注释说明如有特殊文本不可国际化需注明原因。

## 新增文件要求

---

- 新增 Java 文件请参考如下模板分区：
  
  ```java
  // region 成员变量
  // endregion
  // region 生命周期
  // endregion
  // region 公开方法
  // endregion
  // region 私有方法
  // endregion
  // region 回调/内部类
  // endregion
  ```

- 新增适配器类请分区：成员变量、构造方法、适配器核心方法、内部类、辅助方法。

- 新增工具类请分区：静态工具方法、私有辅助方法。

## 提交流程

---

1. 请先拉取最新代码。
2. 保证本地编译通过、无警告。
3. 提交前请自查代码风格、注释和国际化。
4. 提交 PR 时请简要说明变更内容和影响范围。

## 其它协作说明

---

- 有问题请先查阅 `README.md` 和本文件。
- 欢迎提出 Issue 或 PR，建议附详细描述。
- 团队成员和外部贡献者均需遵循本规范。

## Markdown 文档规范

---

- 所有 Markdown 文档（如 README.md、CONTRIBUTING.md 等）最大标题为二级标题（即 ## ），不得出现一级标题（#）。
- 标题前后需有空行。
- 二级标题下需加分割线（---）和空行。
- 列表项之间不加空行。
- 代码块、引用等结构前后需加空行。

---

如有更优建议，欢迎补充！ 
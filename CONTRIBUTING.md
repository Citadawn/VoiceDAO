# Contributing to 语道 (VoiceDAO)

感谢你对本项目的关注！本文档将指导你如何为项目做出贡献。

## 快速开始

---

### 环境准备

1. 确保已安装 Android Studio 最新版本
2. 配置 JDK 17 或更高版本（项目使用 Java 17）
3. 安装必要的 Android SDK 工具

### 协作流程

1. Fork 项目到你的 GitHub 账户
2. 克隆你的 Fork 到本地：`git clone https://github.com/your-username/VoiceDAO.git`
3. 添加上游远程仓库：`git remote add upstream https://github.com/original-owner/VoiceDAO.git`
4. 创建功能分支：`git checkout -b feature/your-feature-name`
5. 开发完成后提交 PR，并简要说明变更内容和影响范围

## 开发规范

---

### 代码结构

项目采用分层架构，请按以下规则组织代码：

- **UI 层**：`ui` 包 - 所有 Activity、Fragment、Dialog 等界面组件
- **业务层**：`logic` 包 - 业务逻辑处理类
- **数据层**：`data` 包 - 数据模型、数据库操作等
- **工具层**：`util` 包 - 通用工具类和辅助方法
- **适配器层**：`adapter` 包 - RecyclerView、ListView 等适配器
- **网络层**：`network` 包 - API 调用、网络请求等

### 命名规范

- **类名**：使用 PascalCase（大驼峰），如 `MainActivity`、`TtsEngineHelper`
- **方法名/变量名**：使用 camelCase（小驼峰），如 `startSpeak()`、`isEngineAvailable`
- **常量名**：使用 UPPER_SNAKE_CASE（全大写+下划线），如 `AUDIO_FILE_NAME`、`MAX_RETRY_COUNT`
- **包名**：全小写，结构清晰，如 `com.citadawn.speechapp.util`
- **资源文件**：使用下划线分隔，如 `activity_main.xml`、`btn_primary_bg.xml`

### 代码注释

- 所有公共类和方法必须添加 JavaDoc 注释
- 复杂业务逻辑需要详细的行内注释
- 使用 `// region ... // endregion` 进行功能分区
- 示例：

```java
/**
 * TTS 引擎管理器
 * 负责管理所有可用的 TTS 引擎，包括引擎切换、状态监控等
 */
public class TtsEngineManager {

    // region 成员变量
    private final Context context;
    private final List<TtsEngine> availableEngines;
    // endregion

    // region 公开方法
    /**
     * 获取所有可用的 TTS 引擎
     * @return 可用引擎列表，如果没有则返回空列表
     */
    public List<TtsEngine> getAvailableEngines() {
        // 实现逻辑...
    }
    // endregion
}
```

### 资源管理

#### 颜色和主题

**资源使用原则**（平衡简洁性和可维护性）：

- **判断标准**：使用频率决定是否使用资源
  
  - **高频使用**（3次以上）：使用 `@color` 资源或主题属性
    - **通用文本和面板**：优先使用主题属性（支持深色/浅色模式）
      - 文本颜色：`?attr/colorOnSurface`
      - 背景颜色：`?attr/colorSurface`
      - 分割线：`?attr/colorOutline`
    - **语义化颜色**：使用 `@color` 资源（如警告、成功、错误等）
      - 便于统一管理和维护
  - **低频使用**（1-2次）：可以硬编码
    - 如果只使用一次且不太可能复用，可以直接硬编码
    - lint 会警告，但不会阻止构建

- **如何判断使用频率**：
  
  - 在 Android Studio 中使用 "Find Usages"（Alt+F7）查看资源的使用次数
  - 或者使用全局搜索（Ctrl+Shift+F）搜索颜色值或资源名称
  - 如果已经使用多次或预计会复用，使用资源；否则可以硬编码

#### 尺寸和字体

**资源使用原则**（平衡简洁性和可维护性）：

- **判断标准**：使用频率决定是否使用资源
  
  - **高频使用**（3次以上）：必须使用 `@dimen` 资源
    - 便于统一管理和修改
    - 如果将来需要调整，只需修改一处
  - **低频使用**（1-2次）：可以硬编码
    - 如果只使用一次且不太可能复用，可以直接硬编码
    - lint 会警告，但不会阻止构建
    - 这样可以让项目更简洁，避免不必要的资源文件

- **如何判断使用频率**：
  
  - 在 Android Studio 中使用 "Find Usages"（Alt+F7）查看资源的使用次数
  - 或者使用全局搜索（Ctrl+Shift+F）搜索资源名称
  - 如果已经使用多次或预计会复用，使用资源；否则可以硬编码

- **运行时设置字体大小**：

```java
// 推荐方式：在布局中定义
android:textSize="@dimen/sp_16"

// 运行时设置（如必须）
textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 
    getResources().getDimension(R.dimen.sp_16));
```

#### 样式复用

- 按钮样式：`@style/Widget.Button.Primary`、`@style/Widget.Button.Small`
- 文本样式：`@style/TextAppearance.App.Body`、`@style/TextAppearance.App.Caption`
- 卡片样式：`@style/Widget.CardView.Elevated`

### 代码质量

#### 代码优化原则

- 提取重复代码到工具类
- 将魔法数字定义为常量
- 使用 `WeakReference` 避免内存泄漏
- 优先使用 Kotlin 协程处理异步操作（如果项目支持）

#### 性能优化

- 避免在 `onDraw()` 中创建对象
- 使用 `ViewHolder` 模式优化列表性能
- 合理使用 `RecyclerView` 的 `setHasFixedSize()`
- 避免在主线程进行耗时操作

#### 内存管理

- 及时释放资源（如 MediaPlayer、AudioRecord 等）
- 使用 `WeakReference` 引用 Activity 或 Fragment
- 避免在静态变量中持有 Context 引用

### 依赖管理

- 统一使用 `gradle/libs.versions.toml` 管理依赖版本
- 定期升级 Android Gradle Plugin 和核心库
- 及时修复过时 API 的警告（lint 会检查，但警告不阻止构建）
- 使用 `implementation` 而非 `api` 依赖（除非必要）

### Lint 配置

项目采用 Android 官方推荐的标准 lint 配置：

- **错误检查**：严重错误会阻止构建（`abortOnError true`）
- **警告处理**：警告不会阻止构建（`warningsAsErrors false`），但建议及时修复
- **检查范围**：检查所有 lint 规则，确保代码质量
- **开发建议**：
  - 严重错误必须修复
  - 警告建议修复，但不强制
  - **尺寸/颜色/字体**：对于只使用一次的硬编码值，可以接受 lint 警告（在代码审查时说明原因）
  - **字符串**：硬编码字符串的警告必须修复，因为会影响国际化

## 国际化规范

---

### 字符串资源

**⚠️ 重要：字符串资源与尺寸/颜色资源不同，必须使用资源引用！**

- **强制要求**：所有用户可见文本必须使用 `@string/` 资源引用，**无论使用频率如何**
  
  - 即使某个字符串只使用一次，也必须放入 `res/values/strings.xml`
  - 这是为了支持国际化（i18n），即使当前只有一种语言

- **国际化支持**：
  
  - 支持多语言：`values-zh/strings.xml`（中文）、`values/strings.xml`（英文）
  - 以中文为默认语言，先添加中文再同步翻译
  - 确保所有语言的字符串资源 ID 完全一致

- **为什么字符串不能硬编码**：
  
  - 国际化需求：需要支持多语言切换
  - 即使现在只有一种语言，将来可能需要添加其他语言
  - 字符串资源是国际化的基础，硬编码会导致无法翻译

### 命名规范

- 使用描述性名称，如 `tts_engine_not_available` 而非 `error_msg`
- 按功能模块分组，如 `tts_*`、`ui_*`、`error_*`

### 复数处理

- 使用 `quantity` 属性处理复数形式
- 示例：

```xml
<plurals name="tts_engine_count">
    <item quantity="one">%d 个 TTS 引擎</item>
    <item quantity="other">%d 个 TTS 引擎</item>
</plurals>
```

## 测试规范

---

### 单元测试

- 所有业务逻辑类必须编写单元测试
- 测试覆盖率不低于 80%
- 使用 JUnit 4 或 JUnit 5 编写测试
- 测试类命名：`ClassNameTest`

### UI 测试

- 关键用户流程需要 UI 测试
- 使用 Espresso 框架编写 UI 测试
- 测试类命名：`ClassNameTest`

### 测试数据

- 使用测试专用的资源文件
- 避免在测试中依赖真实网络或数据库
- 使用 Mock 对象模拟外部依赖

## 文档规范

---

### Markdown 格式

- 最大标题为二级标题（`##`），不得使用一级标题
- 标题前后需有空行，二级标题下加分割线（`---`）
- 列表项之间不加空行
- 代码块和引用前后需加空行
- 键盘按键使用 `<kbd></kbd>` 标签，如 <kbd>Ctrl</kbd>+<kbd>C</kbd>

### 代码格式规范

- **行内代码**：变量名、方法名、类名、参数等使用反引号包围
- **代码块**：完整代码示例使用三个反引号，并标注语言类型
- **资源引用**：Android 资源引用使用行内代码格式
- **示例**：`MainActivity`、`setupInfoIcons()`、`@string/app_name`

## 开发模板

---

### Java 类模板

```java
/**
 * 类功能描述
 * 
 * @author 作者名
 * @since 版本号
 */
public class ClassName {

    // region 常量
    private static final String TAG = "ClassName";
    // endregion

    // region 成员变量
    private final Context context;
    // endregion

    // region 构造方法
    public ClassName(Context context) {
        this.context = context;
    }
    // endregion

    // region 公开方法
    // 公开方法实现
    // endregion

    // region 私有方法
    // 私有方法实现
    // endregion

    // region 回调/内部类
    // 回调接口和内部类
    // endregion
}
```

### 适配器类模板

```java
public class ExampleAdapter extends RecyclerView.Adapter<ExampleAdapter.ViewHolder> {

    // region 成员变量
    private final List<ExampleItem> items;
    // endregion

    // region 构造方法
    public ExampleAdapter(List<ExampleItem> items) {
        this.items = items;
    }
    // endregion

    // region 适配器核心方法
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 实现逻辑
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // 实现逻辑
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    // endregion

    // region 内部类
    static class ViewHolder extends RecyclerView.ViewHolder {
        // ViewHolder 实现
    }
    // endregion

    // region 辅助方法
    // 辅助方法实现
    // endregion
}
```

## 代码审查清单

---

提交 PR 前，请确保：

- [ ] 代码编译通过，无错误（警告建议修复，但不强制）
- [ ] 所有测试通过
- [ ] 遵循项目代码风格
- [ ] 添加必要的注释和文档
- [ ] 更新相关文档
- [ ] 检查资源文件是否国际化
- [ ] 验证 UI 在不同屏幕尺寸下的表现
- [ ] 检查内存泄漏和性能问题
- [ ] 运行 lint 检查，修复严重错误（警告建议修复）

## 相关资源

---

- [项目主页](./README.md) - 项目概述和快速开始
- [信息图标开发指南](./docs/guides/信息图标开发指南.md) - 信息图标的标准模板
- [状态信息区块开发说明](./docs/guides/状态信息区块开发说明.md) - 状态信息开发规范
- [测试模式添加测试项指南](./docs/guides/测试模式添加测试项指南.md) - 测试项开发步骤
- [API 参考文档](./docs/reference/) - 详细的 API 文档

## 问题反馈

---

如果你在开发过程中遇到问题：

1. 首先查看本文档和 `README.md`
2. 搜索现有的 Issues 和 PR
3. 如果问题仍未解决，请创建新的 Issue
4. 在 Issue 中详细描述问题、复现步骤和期望结果

## 贡献者指南

---

我们欢迎所有形式的贡献：

- 🐛 Bug 报告和修复
- ✨ 新功能开发
- 📚 文档改进
- 🎨 UI/UX 优化
- 🧪 测试用例补充
- 🌍 多语言支持

感谢你的贡献，让我们一起让 语道 (VoiceDAO) 变得更好！
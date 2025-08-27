## 项目简介

---

语道 (VoiceDAO) 是一个专注于文本转语音（TTS）功能的 Android 应用。用户可输入任意文本，调节语速和音调，一键朗读。该项目依赖于系统当前设置的
TTS 引擎，本身不内置语音合成能力，专注于提供简洁直观的用户界面和流畅的 TTS 体验。

## 主要功能

---

### 核心功能

- **文本转语音**：输入任意文本内容，支持多语言文本朗读
- **语速调节**：实时调节语音朗读速度，支持滑块控制和重置功能
- **音调调节**：灵活调节语音朗读音调，支持滑块控制和重置功能
- **语言选择**：支持多种语言切换，自动适配系统 TTS 引擎支持的语言
- **发音人选择**：支持多种发音人选择，提供丰富的语音体验
- **音频保存**：将朗读的语音保存为音频文件，支持多种音频格式

### 特色功能

- **信息图标**：每个功能项都配有信息图标，点击查看详细说明
- **测试模式**：为开发者提供测试模式，支持功能验证和调试
- **状态信息**：实时显示 TTS 引擎状态、音频保存目录等信息
- **自适应图标**：支持 Android 自适应图标，适配不同启动器形状

## 技术特性

---

### 架构设计

- **分层架构**：采用 UI、Logic、Util、Adapter 分层设计
- **代码分区**：使用 `// region ... // endregion` 注释进行代码分区
- **工具类封装**：提供丰富的工具类，简化开发流程

### 开发规范

- **资源管理**：统一使用资源引用，支持主题属性和语义色
- **国际化支持**：完整的中英文支持，使用 `strings.xml` 资源文件
- **代码质量**：遵循 Android 开发最佳实践，支持代码审查

## 快速开始

---

### 环境要求

- Android Studio 4.0+
- Android SDK API 21+ (Android 5.0)
- Java 8 或 Kotlin 1.3+

### 安装步骤

1. **克隆项目**
   
   ```bash
   git clone https://github.com/your-username/VoiceDAO.git
   cd VoiceDAO
   ```

2. **打开项目**
   
   - 使用 Android Studio 打开项目
   - 等待 Gradle 同步完成

3. **运行应用**
   
   - 连接 Android 设备或启动模拟器
   - 点击运行按钮，选择目标设备

### 系统要求

- Android 5.0 (API 21) 及以上版本
- 系统已安装并配置 TTS 引擎
- 建议使用 Android 8.0+ 获得最佳体验

## Android 系统 TTS 设置

---

Android 系统的"文字转语音（TTS）设置"页面用于选择系统默认 TTS 引擎、默认语言。

### 进入"文字转语音（TTS）设置"页面

**注意**：不同的手机进入方式可能不同。

1. 打开手机"设置"应用
2. 搜索"文字转语音"或依次进入"系统" > "语言和输入法" > "文字转语音输出"
3. 即可进入 TTS 设置页面，进行相关配置

### 主要功能

- **首选引擎**：选择当前系统默认 TTS 引擎（如 MultiTTS、TTS Server 等）
- **语言**：设置默认语言（如中文、英文、日语等）
- **语速/音高**：调节 TTS 朗读的语速和音调
- **试听语音**：可试听当前设置下的 TTS 发音效果

### 重要说明

- 此页面设置的"语言、语速、音高"会在全局生效，影响所有新创建的 TTS 实例的默认值
- 大多数 App（包括本项目）会在调用 TTS 朗读时自行指定参数，优先级高于系统设置页面
- 如果 TTS 引擎本身不支持某种语言，即使在此页面设置也不会生效，TTS 引擎的默认语言会使用系统语言
- 某些 TTS 引擎也可以设置语速和音高，比如 MultiTTS、TTS Server，其中 MultiTTS 设置的语速和音高只在
  MultiTTS 中有效，而 TTS Server 中设置的语速和音高在全局有效

## 开发指南

---

### 核心文档

- [文档索引](./docs/文档索引.md) - 项目文档导航中心
- [协作规范](./CONTRIBUTING.md) - 项目开发协作规范

### 功能开发指南

- [Toast 开发指南](./docs/guides/Toast开发指南.md) - Toast 消息提示的使用规范和最佳实践
- [信息图标开发指南](./docs/guides/信息图标开发指南.md) - 信息图标的标准模板和使用方法
- [状态信息区块开发说明](./docs/guides/状态信息区块开发说明.md) - 主界面状态信息的开发规范
- [测试模式添加测试项指南](./docs/guides/测试模式添加测试项指南.md) - 为测试模式新增测试项的步骤

### 参考资料

- [软件名称介绍](./docs/reference/软件名称介绍.md) - 项目名称"语道"和"VoiceDAO"的含义和品牌价值
- [术语表](./docs/reference/术语表.md) - 项目中常用专业术语的统一解释
- [启动器图标设计指南](./docs/reference/启动器图标设计指南.md) - 应用启动器图标的设计理念和实现方式
- [Locale 类](./docs/reference/Locale 类.md) - Java `Locale` 类的属性与方法详解
- [TextToSpeech 类](./docs/reference/TextToSpeech 类.md) - Android `TextToSpeech` 类的属性与方法详解
- [TextToSpeech.EngineInfo 类](./docs/reference/TextToSpeech.EngineInfo 类.md) - Android
  `TextToSpeech.EngineInfo` 类的属性与方法详解
- [Voice 类](./docs/reference/Voice 类.md) - Android `Voice` 类的属性与方法详解

## 项目结构

---

```
VoiceDAO/
├── app/                          # 应用模块
│   ├── src/main/
│   │   ├── java/com/citadawn/speechapp/
│   │   │   ├── ui/              # 用户界面
│   │   │   ├── util/            # 工具类
│   │   │   └── adapter/         # 适配器
│   │   └── res/                 # 资源文件
│   │       ├── layout/          # 布局文件
│   │       ├── values/          # 资源值
│   │       └── drawable/        # 图形资源
├── docs/                        # 项目文档
│   ├── guides/                  # 开发指南
│   ├── reference/               # 参考资料
│   └── faq/                     # 常见问题
├── CONTRIBUTING.md              # 协作规范
└── README.md                    # 项目说明
```

## 贡献指南

---

欢迎所有形式的贡献！请阅读 [CONTRIBUTING.md](./CONTRIBUTING.md) 了解详细的贡献指南。

### 贡献方式

- 🐛 报告 Bug
- 💡 提出新功能建议
- 📝 改进文档
- 🔧 提交代码修复
- 🌍 帮助国际化

### 开发流程

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

---

本项目采用 [MIT 许可证](./LICENSE) 开源。

## 联系方式

---

- 项目主页：[GitHub Repository](https://github.com/[your-username]/VoiceDAO)
- 问题反馈：[Issues](https://github.com/[your-username]/VoiceDAO/issues)
- 讨论交流：[Discussions](https://github.com/[your-username]/VoiceDAO/discussions)

---

**让文字发声，让信息传递更生动** ✨
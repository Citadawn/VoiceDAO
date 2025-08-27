官方文档：[TextToSpeech.EngineInfo | API reference | Android Developers](https://developer.android.com/reference/android/speech/tts/TextToSpeech.EngineInfo)

## 概述

---

`TextToSpeech.EngineInfo` 是 Android 文本转语音（TTS）API 中的一个内部类，用于表示已安装的 TTS
引擎信息。该类包含引擎的包名、显示名称、图标等基本信息，主要用于引擎列表展示和引擎切换功能。

## 字段

---

### name 字段

`TextToSpeech.EngineInfo.name` 是一个 `String` 类型的字段，表示 TTS 引擎的包名（Package Name）。

**字段含义**：

- **类型**：`String` - 字符串类型
- **作用**：存储 TTS 引擎应用的唯一标识符（包名）
- **格式**：标准的 Android 包名格式，如 `com.google.android.tts`
- **唯一性**：每个 TTS 引擎应用都有唯一的包名

**与 label 字段的区别**：

- **`name`**：技术性标识符，用于系统识别和程序逻辑
- **`label`**：用户友好的显示名称，用于 UI 展示

**注意事项**：

- **不可为空**：理论上 `name` 字段不应该为空，但建议进行空值检查
- **大小写敏感**：包名比较是大小写敏感的

### label 字段

`TextToSpeech.EngineInfo.label` 是一个 `String` 类型的字段，表示 TTS 引擎的用户友好显示名称。

**字段含义**：

- **类型**：`String` - 字符串类型
- **作用**：存储 TTS 引擎的显示名称，用于在 UI 中向用户展示
- **来源**：由 TTS 引擎应用在其 `AndroidManifest.xml` 中声明的 `android:label` 属性

**与 name 字段的区别**：

- **`name`**：TTS 引擎的包名（如 `com.google.android.tts`）
- **`label`**：TTS 引擎的显示名称（如 "Google 文字转语音引擎"）

**注意事项**：

- **本地化**：`label` 字段会根据系统语言自动本地化
- **可读性**：相比 `name` 字段，`label` 更适合在 UI 中显示
- **空值处理**：虽然理论上不应该为空，但建议在显示前进行空值检查

### icon 字段

`TextToSpeech.EngineInfo.icon` 是一个 `int` 类型的字段，表示 TTS 引擎的图标资源 ID。

**字段含义**：

- **类型**：`int` - 资源 ID
- **作用**：存储 TTS 引擎的图标资源标识符
- **来源**：由 TTS 引擎应用在其 `AndroidManifest.xml` 中声明

**注意事项**：

- **异常处理**：必须用 try-catch 包装，因为某些引擎可能没有图标或图标资源不可访问

## 方法

---

### toString

- **说明**：`toString()` 方法是 `TextToSpeech.EngineInfo` 类继承自 `Object` 类的方法，用于返回
  `EngineInfo` 对象的字符串表示。

- **方法签名**：

  ```java
  public String toString()
  ```

- **返回值**：返回一个 `String` 类型的字符串，包含 `EngineInfo` 对象的主要信息，通常格式为：

  ```java
  EngineInfo{name=包名, label=显示名称, icon=图标资源ID}
  ```
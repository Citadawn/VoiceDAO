# TTS 语言支持情况说明

## 概述

---

在 Android 文本转语音（TTS）系统中，每个语言都有对应的"支持情况"，表示当前 TTS 引擎对该语言的支持程度。支持情况基于 Android TextToSpeech API 的 `isLanguageAvailable()` 方法返回值，帮助用户了解每种语言的可用性。

## 支持情况分类

---

### 完全支持（Available）

- **常量**：`TextToSpeech.LANG_AVAILABLE`
- **中文显示**：完全支持
- **英文显示**：Available
- **颜色标识**：🟢 绿色
- **含义**：TTS 引擎完全支持指定的语言、国家和地区
- **示例**：`en_US`（美式英语）、`zh_CN`（简体中文）
- **可用性**：可以直接使用，无需额外配置

### 国家支持（Country Available）

- **常量**：`TextToSpeech.LANG_COUNTRY_AVAILABLE`
- **中文显示**：国家支持
- **英文显示**：Country Available
- **颜色标识**：🟣 紫色
- **含义**：TTS 引擎支持该语言和国家，但不一定支持具体的地区变体
- **示例**：请求 `en_CA`（加拿大英语），但引擎只支持 `en`（英语）
- **可用性**：可以使用，但可能使用通用的国家语言而非特定地区变体

### 变体支持（Country Var Available）

- **常量**：`TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE`
- **中文显示**：变体支持
- **英文显示**：Variant Available
- **颜色标识**：🔵 蓝色
- **含义**：TTS 引擎支持该语言、国家和特定的变体
- **示例**：`zh_CN_#Hans`（简体中文，汉斯变体）
- **可用性**：可以使用，支持更精确的语言变体

### 缺少数据（Missing Data）

- **常量**：`TextToSpeech.LANG_MISSING_DATA`
- **中文显示**：缺少数据
- **英文显示**：Missing Data
- **颜色标识**：🟡 黄色
- **含义**：TTS 引擎理论上支持该语言，但缺少必要的语音数据包
- **解决方法**：需要在系统 TTS 设置中下载对应的语音数据包
- **可用性**：下载数据包后才能使用

### 不支持（Not Supported）

- **常量**：`TextToSpeech.LANG_NOT_SUPPORTED`
- **中文显示**：不支持
- **英文显示**：Not Supported
- **颜色标识**：⚪ 灰色
- **含义**：当前 TTS 引擎完全不支持该语言
- **解决方法**：需要安装支持该语言的 TTS 引擎
- **可用性**：无法使用

### 未知（Unknown）

- **常量**：无对应常量
- **中文显示**：未知
- **英文显示**：Unknown
- **颜色标识**：⚪ 灰色
- **含义**：无法确定 TTS 引擎对该语言的支持情况
- **原因**：可能是 TTS 引擎异常或 API 调用失败
- **可用性**：建议避免使用

## 颜色标识系统

---

在 TTS 浏览器中，每种支持情况都有对应的颜色标识，方便用户快速识别：

| 颜色    | 支持情况   | 推荐程度 | 说明          |
| ----- | ------ | ---- | ----------- |
| 🟢 绿色 | 完全支持   | 最佳   | 推荐使用，获得最佳体验 |
| 🟣 紫色 | 国家支持   | 良好   | 通常也能正常使用    |
| 🔵 蓝色 | 变体支持   | 良好   | 支持精确匹配      |
| 🟡 黄色 | 缺少数据   | 需处理  | 需要先下载语音包    |
| ⚪ 灰色  | 不支持/未知 | 避免   | 应避免使用       |

## 技术实现

---

### Android API 获取方法

```java
// 检查语言支持情况
Locale targetLocale = new Locale("en", "US");
int supportStatus = tts.isLanguageAvailable(targetLocale);

// 根据支持情况处理
switch (supportStatus) {
    case TextToSpeech.LANG_AVAILABLE:
        // 完全支持，可直接使用
        break;
    case TextToSpeech.LANG_COUNTRY_AVAILABLE:
        // 国家支持，可以使用但可能降级
        break;
    case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
        // 变体支持，支持精确匹配
        break;
    case TextToSpeech.LANG_MISSING_DATA:
        // 缺少数据包，需要下载
        break;
    case TextToSpeech.LANG_NOT_SUPPORTED:
        // 不支持该语言
        break;
    default:
        // 未知状态
        break;
}
```

### 常量定义

```java
// 语言支持情况常量
TextToSpeech.LANG_AVAILABLE = 0
TextToSpeech.LANG_COUNTRY_AVAILABLE = 1
TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE = 2
TextToSpeech.LANG_MISSING_DATA = -1
TextToSpeech.LANG_NOT_SUPPORTED = -2
```

## 使用建议

---

### 选择优先级

1. **🟢 完全支持**：优先选择，获得最佳体验
2. **🟣 国家支持**：次优选择，通常也能正常使用
3. **🔵 变体支持**：精确匹配，适合特定需求
4. **🟡 缺少数据**：需要先下载语音包
5. **⚪ 不支持/未知**：避免使用

### 处理缺少数据的情况

当遇到"缺少数据"状态时：

1. **引导用户到系统 TTS 设置**
2. **下载对应的语音数据包**
3. **重新检查支持情况**
4. **确认下载完成后再次尝试使用**

### 降级策略

当首选语言不支持时，可以考虑：

1. **使用同语言的通用变体**（如 `en` 替代 `en_US`）
2. **使用系统默认语言**
3. **提示用户安装支持该语言的 TTS 引擎**

## 注意事项

---

1. **TTS 引擎差异**：不同 TTS 引擎对语言的支持情况可能有差异
2. **数据包依赖**：某些语言需要下载额外的语音数据包
3. **动态变化**：支持情况可能因 TTS 引擎更新、数据包安装等因素动态变化
4. **API 版本**：某些支持情况常量在不同 Android API 版本中可能有差异

## 相关文档

---

- [TextToSpeech 类文档](./TextToSpeech%20类.md) - Android TTS API 的使用指南
- [TTS 音质和延迟等级说明](./TTS%20音质和延迟等级说明.md) - 发音人质量等级说明
- [TTS 浏览器使用说明](./README.md#系统TTS设置页面说明) - 如何在应用中查看语言支持情况 
# TTS 音质和延迟等级说明

## 概述

---

在 Android 文本转语音（TTS）系统中，每个发音人（Voice）都有两个重要的属性：**音质等级**和**延迟等级**。这些等级帮助用户根据具体使用场景选择最合适的 TTS 发音人。

## 音质等级（Quality Level）

---

音质等级表示 TTS 发音人合成语音的质量水平，数值越高音质越好。

### 等级分类

| 等级                  | 数值  | 中文显示 | 英文显示      | 含义说明          |
| ------------------- | --- | ---- | --------- | ------------- |
| `QUALITY_VERY_LOW`  | 100 | 极低   | Very Low  | 可理解但不自然，机械感强  |
| `QUALITY_LOW`       | 200 | 低    | Low       | 不太像真人，但基本可用   |
| `QUALITY_NORMAL`    | 300 | 正常   | Normal    | 标准音质，日常使用足够   |
| `QUALITY_HIGH`      | 400 | 高    | High      | 接近真人发音，自然度较高  |
| `QUALITY_VERY_HIGH` | 500 | 极高   | Very High | 几乎无法区分真人，最高质量 |
| 未知情况                | -   | 未知   | Unknown   | 无法获取音质信息      |

### 使用场景建议

- **极低/低音质**：适用于对音质要求不高的场景，如系统提示音
- **正常音质**：日常文本朗读、阅读辅助等场景
- **高/极高音质**：有声书、播客、专业语音内容等对音质要求较高的场景

## 延迟等级（Latency Level）

---

延迟等级表示 TTS 发音人从接收文本到开始播放语音的响应时间，数值越低延迟越小。

### 等级分类

| 等级                  | 数值  | 中文显示 | 英文显示      | 延迟时间     | 适用场景      |
| ------------------- | --- | ---- | --------- | -------- | --------- |
| `LATENCY_VERY_LOW`  | 100 | 极低   | Very Low  | <20ms    | 实时对话、游戏等  |
| `LATENCY_LOW`       | 200 | 低    | Low       | 约20ms    | 交互式应用     |
| `LATENCY_NORMAL`    | 300 | 正常   | Normal    | 约50ms    | 一般朗读应用    |
| `LATENCY_HIGH`      | 400 | 高    | High      | 约200ms   | 网络合成，质量优先 |
| `LATENCY_VERY_HIGH` | 500 | 极高   | Very High | >200ms   | 网络合成，高质量  |
| 未知情况                | -   | 未知   | Unknown   | 无法获取延迟信息 |           |

### 使用场景建议

- **极低/低延迟**：实时语音交互、游戏配音、导航系统等
- **正常延迟**：一般文本朗读、阅读应用等
- **高/极高延迟**：对音质要求高且不要求实时性的场景

## 音质与延迟的权衡

---

在实际应用中，音质和延迟往往存在权衡关系：

### 一般规律

- **高音质发音人**通常延迟也较高，特别是网络合成的发音人
- **低延迟发音人**通常是本地合成，音质可能相对较低
- **网络合成**的发音人音质更好，但延迟较高
- **本地合成**的发音人响应快，但音质可能相对较低

### 网络要求

- **高延迟（400-500）**的发音人通常需要网络连接
- **低延迟（100-300）**的发音人通常是本地合成，无需网络

## 技术实现

---

### Android API 获取方法

```java
// 获取音质等级
int quality = voice.getQuality();

// 获取延迟等级
int latency = voice.getLatency();

// 判断是否需要网络
boolean requiresNetwork = voice.isNetworkConnectionRequired();
```

### 等级常量定义

```java
// 音质等级常量
Voice.QUALITY_VERY_LOW = 100
Voice.QUALITY_LOW = 200
Voice.QUALITY_NORMAL = 300
Voice.QUALITY_HIGH = 400
Voice.QUALITY_VERY_HIGH = 500

// 延迟等级常量
Voice.LATENCY_VERY_LOW = 100
Voice.LATENCY_LOW = 200
Voice.LATENCY_NORMAL = 300
Voice.LATENCY_HIGH = 400
Voice.LATENCY_VERY_HIGH = 500
```

## 注意事项

---

1. **TTS 引擎差异**：不同 TTS 引擎对等级的实现可能有差异
2. **设备性能**：设备性能可能影响实际的延迟表现
3. **网络环境**：网络质量会影响网络合成发音人的实际延迟
4. **动态变化**：某些发音人的等级可能因网络状态、设备负载等因素动态变化

## 相关文档

---

- [Voice 类文档](./Voice%20类.md) - Android Voice 类的详细说明
- [TextToSpeech 类文档](./TextToSpeech%20类.md) - Android TTS API 的使用指南
- [TTS 浏览器使用说明](./README.md#系统TTS设置页面说明) - 如何在应用中查看发音人信息 
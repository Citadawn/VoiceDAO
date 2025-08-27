官方文档：[Voice | API reference | Android Developers](https://developer.android.com/reference/android/speech/tts/Voice)

## 概述

---

`Voice` 是 Android 文本转语音（TTS）API 中用于描述“发音人”的类。每个 TTS
引擎可以为每种语言（Locale）提供多个不同的发音人（Voice），每个发音人有不同的属性和特性。

## 常量

---

- `QUALITY_VERY_LOW = 100`：极低音质
- `QUALITY_LOW = 200`：低音质
- `QUALITY_NORMAL = 300`：正常音质
- `QUALITY_HIGH = 400`：高音质
- `QUALITY_VERY_HIGH = 500`：极高音质
- `LATENCY_VERY_LOW = 100`：极低延迟
- `LATENCY_LOW = 200`：低延迟
- `LATENCY_NORMAL = 300`：正常延迟
- `LATENCY_HIGH = 400`：高延迟
- `LATENCY_VERY_HIGH = 500`：极高延迟

## 方法

---

### getFeatures

- **说明**：获取该发音人支持的所有“特性”字符串集合。

- **方法签名**：

  ```java
  public Set<String> getFeatures()
  ```

- **返回值**：`Set<String>`，如 `networkTts`、`emotion` 等。

- **用法示例**：

  ```java
  Set<String> features = voice.getFeatures();
  if (features != null && features.contains("networkTts")) {
      // 该发音人需要联网
  }
  ```

### getLatency

- **说明**：获取该发音人合成语音的预期延迟等级。

- **方法签名**：

  ```java
  public int getLatency()
  ```

- **返回值**：`int`，数值越低延迟越小，响应越快。

    - `Voice.LATENCY_VERY_LOW = 100`：极低延迟（<20ms）
    - `Voice.LATENCY_LOW = 200`：低延迟（约 20ms）
    - `Voice.LATENCY_NORMAL = 300`：正常延迟（约 50ms）
    - `Voice.LATENCY_HIGH = 400`：高延迟（网络合成，约 200ms）
    - `Voice.LATENCY_VERY_HIGH = 500`：极高延迟（网络合成，>200ms）

- **用法示例**：

  ```java
  int latency = voice.getLatency();
  if (latency <= Voice.LATENCY_LOW) {
      // 适合实时场景
  }
  ```

### getLocale

- **说明**：获取该发音人支持的语言和地区。

- **方法签名**：

  ```java
  public Locale getLocale()
  ```

- **返回值**：`Locale` 对象，如 `zh_CN`、`en_US` 等。

- **用法示例**：

  ```java
  Locale locale = voice.getLocale();
  Log.d("VoiceLocale", locale.toString());
  ```

### getName

- **说明**：获取该发音人的唯一标识符（字符串）。

- **方法签名**：

  ```java
  public String getName()
  ```

- **返回值**：`String`，如 `zh-cn-x-...`、`en-us-x-...`。

- **用法示例**：

  ```java
  String name = voice.getName();
  Log.d("VoiceName", name);
  ```

### getQuality

- **说明**：获取该发音人的音质等级。

- **方法签名**：

  ```java
  public int getQuality()
  ```

- **返回值**：`int`，数值越高音质越好。

    - `Voice.QUALITY_VERY_LOW = 100`：极低音质（可理解但不自然）
    - `Voice.QUALITY_LOW = 200`：低音质（不太像真人）
    - `Voice.QUALITY_NORMAL = 300`：正常音质
    - `Voice.QUALITY_HIGH = 400`：高音质（接近真人）
    - `Voice.QUALITY_VERY_HIGH = 500`：极高音质（几乎无法区分真人）

- **用法示例**：

  ```java
  int quality = voice.getQuality();
  if (quality >= Voice.QUALITY_HIGH) {
      // 高音质
  }
  ```

### isNetworkConnectionRequired

- **说明**：判断该发音人合成语音时是否需要网络。

- **方法签名**：

  ```java
  public boolean isNetworkConnectionRequired()
  ```

- **返回值**：`boolean`，true 表示必须联网，false 表示离线可用。

- **用法示例**：

  ```java
  if (voice.isNetworkConnectionRequired()) {
      // 需要联网
  }
  ```

### toString

- **说明**：返回该 Voice 对象的详细字符串描述，包含所有主要属性。

- **方法签名**：

  ```java
  public String toString()
  ```

- **返回值**：`String`，返回一个包含该 Voice 主要属性信息的字符串，内容包括：

    - `Name`（唯一标识）

    - `Locale`（语言/地区）

    - `Quality`（音质等级）

    - `Latency`（延迟等级）

    - `requiresNetwork`（是否需要联网）

    - `features`（支持的特性集合）

      如：

      ```java
      Voice[Name: zh-cn-x-..., locale: zh_CN, quality: 400, latency: 300, requiresNetwork: false, features: [networkTts, emotion]]
      ```

- **用法示例**：

  ```java
  Log.d("VoiceInfo", voice.toString());
  ```
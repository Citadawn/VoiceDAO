# TTS 初始化常见问题（FAQ）

## 1. TTS 初始化是干什么的？

TTS（TextToSpeech）初始化的作用是**创建并准备好语音合成引擎实例，让 App 能够把文本转换成语音**。只有初始化成功后，才能调用 `speak()`、`synthesizeToFile()` 等方法让 TTS 发声或生成音频。

**详细说明：**

- 初始化时，Android 会加载当前系统设置的 TTS 引擎（如 Google TTS、讯飞、MultiTTS 等），并与系统服务建立连接。
- 初始化过程会检测 TTS 引擎是否可用、是否支持当前系统、是否有可用的语音包等。
- 初始化完成后会回调 `OnInitListener`，你可以在这里设置语言、语速、音调、发音人等参数，并启用相关按钮。
- 初始化前调用 `speak()` 等方法会报错或无效，只有初始化成功后才能正常使用 TTS 功能。

**示例代码：**

```java
TextToSpeech tts = new TextToSpeech(context, status -> {
    if (status == TextToSpeech.SUCCESS) {
        // 初始化成功，可以设置参数、启用朗读按钮
    } else {
        // 初始化失败，提示用户
    }
});
```
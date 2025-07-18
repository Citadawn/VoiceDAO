## TTS 初始化

---

TTS（TextToSpeech）初始化的作用是创建并准备好语音合成引擎实例，让 App 能够把文本转换成语音。只有初始化成功后，才能调用 `speak()`、`synthesizeToFile()` 等方法让 TTS 发声或生成音频，初始化前调用 `speak()` 等方法会报错或无效。  

**示例代码：**  

```java
TextToSpeech tts = new TextToSpeech(this, status -> {
    if (status == TextToSpeech.SUCCESS) {
        // 初始化成功，可以设置参数、启用朗读按钮
    } else {
        // 初始化失败，提示用户
    }
});
```
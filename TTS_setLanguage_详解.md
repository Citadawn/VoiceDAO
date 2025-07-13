# TextToSpeech.setLanguage() 方法详解

## 1. 方法签名

```java
public int setLanguage(Locale loc)
```
- **参数**：`loc` —— 你希望 TTS 朗读时使用的语言和地区（`Locale` 对象）。
- **返回值**：`int`，表示设置结果的状态码。

---

## 2. 作用

`setLanguage()` 用于**设置当前 TTS 实例朗读时使用的语言和地区**。
比如你可以让 TTS 朗读中文、英文、日语、斯瓦希里语等。

---

## 3. 典型用法

```java
int result = tts.setLanguage(Locale.US); // 英语（美国）
int result = tts.setLanguage(new Locale("sw", "KE")); // 斯瓦希里语（肯尼亚）
```

---

## 4. 返回值详解

- `TextToSpeech.LANG_AVAILABLE`：完全支持该语言和地区。
- `TextToSpeech.LANG_COUNTRY_AVAILABLE`：支持该语言和国家，但不一定支持具体地区。
- `TextToSpeech.LANG_MISSING_DATA`：缺少该语言的数据包（需要在系统TTS设置中下载）。
- `TextToSpeech.LANG_NOT_SUPPORTED`：TTS引擎不支持该语言。
- 其他负数：错误。

**常用判断：**
```java
if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
    // 该语言不可用或需要下载语音包
}
```

---

## 5. 典型场景

- **切换朗读语言**：如中英文、日语、法语、斯瓦希里语等。
- **多语言朗读**：App 支持多语言输入时，动态切换 TTS 语言。
- **检测支持情况**：可遍历所有 Locale，判断 TTS 支持哪些语言。

---

## 6. 注意事项

- **setLanguage 只影响当前 TTS 实例**，不会影响系统全局设置。
- **部分 TTS 引擎不支持所有语言**，如 Google TTS 支持较多，部分国产 TTS 只支持中英文。
- **有些语言需要在系统TTS设置中下载语音包**，否则会返回 `LANG_MISSING_DATA`。
- **setLanguage 和 setVoice 可以配合使用**，但如果 Voice 和 Language 不匹配，可能会朗读失败或自动降级。

---

## 7. 相关方法

- `getAvailableLanguages()`（API 21+）：获取所有支持的语言集合。
- `getVoices()`：获取所有支持的发音人（Voice），每个 Voice 也有 Locale。
- `setVoice(Voice)`：直接切换发音人（包含语言、性别、风格等）。

---

## 8. 官方文档

- [TextToSpeech.setLanguage | Android Developers](https://developer.android.com/reference/android/speech/tts/TextToSpeech#setLanguage(java.util.Locale))

---

## 9. 示例代码

```java
Locale swahili = new Locale("sw", "KE");
int result = tts.setLanguage(swahili);
if (result == TextToSpeech.LANG_MISSING_DATA) {
    // 需要下载斯瓦希里语语音包
} else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
    // 当前TTS引擎不支持斯瓦希里语
} else {
    // 可以正常朗读斯瓦希里语
}
```

---

**总结：**  
`setLanguage()` 是 TTS 朗读多语言的核心方法，能让你的 App 支持多种语言的语音输出。返回值要及时判断，提示用户是否需要下载语音包或更换 TTS 引擎。 
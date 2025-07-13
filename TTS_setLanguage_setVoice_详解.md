# TextToSpeech.setLanguage() 与 setVoice() 方法详解

## 1. setLanguage 方法详解

### 方法签名

```java
public int setLanguage(Locale loc)
```

- **参数**：`loc` —— 你希望 TTS 朗读时使用的语言和地区（`Locale` 对象）。
- **返回值**：`int`，表示设置结果的状态码。

### 作用

`setLanguage()` 用于**设置当前 TTS 实例朗读时使用的语言和地区**。
比如你可以让 TTS 朗读中文、英文、日语、斯瓦希里语等。

### 典型用法

```java
int result = tts.setLanguage(Locale.US); // 英语（美国）
int result = tts.setLanguage(new Locale("sw", "KE")); // 斯瓦希里语（肯尼亚）
```

### 返回值详解

- `TextToSpeech.LANG_AVAILABLE`：完全支持该语言和地区。
- `TextToSpeech.LANG_COUNTRY_AVAILABLE`：支持该语言和国家，但不一定支持具体地区。
- `TextToSpeech.LANG_MISSING_DATA`：缺少该语言的数据包（需要在系统TTS设置中下载）。
- `TextToSpeech.LANG_NOT_SUPPORTED`：TTS引擎不支持该语言。
- 其他负数：错误。

### LANG_AVAILABLE 与 LANG_COUNTRY_AVAILABLE 的区别

- **`LANG_AVAILABLE`**：TTS 引擎**完全支持**你传入的语言和地区（Locale），有专门为这个地区定制的发音模型。
  
  - 例如：你传入 `Locale("en", "US")`（美式英语），TTS 引擎有专门的美式英语发音人。
  - 朗读效果：发音最地道，最贴合你指定的地区。

- **`LANG_COUNTRY_AVAILABLE`**：TTS 引擎**只支持你指定的语言和国家**，但没有专门为你指定的“地区”或“方言”定制的发音。
  
  - 例如：你传入 `Locale("en", "CA")`（加拿大英语），但 TTS 引擎没有专门的加拿大英语发音人，只能用美式英语或英式英语来朗读。
  - 朗读效果：发音是“通用的”或“最接近的”，不是你指定地区的特色口音。

**代码举例：**

```java
Locale usEnglish = new Locale("en", "US");
int result = tts.setLanguage(usEnglish);
if (result == TextToSpeech.LANG_AVAILABLE) {
    // 完全支持美式英语
} else if (result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
    // 支持英语，但没有专门的美式英语发音
}
```



### 典型场景

- **切换朗读语言**：如中英文、日语、法语、斯瓦希里语等。
- **多语言朗读**：App 支持多语言输入时，动态切换 TTS 语言。
- **检测支持情况**：可遍历所有 Locale，判断 TTS 支持哪些语言。

### 注意事项

- **setLanguage 只影响当前 TTS 实例**，不会影响系统全局设置。
- **部分 TTS 引擎不支持所有语言**，如 Google TTS 支持较多，部分国产 TTS 只支持中英文。
- **有些语言需要在系统TTS设置中下载语音包**，否则会返回 `LANG_MISSING_DATA`。
- **setLanguage 和 setVoice 可以配合使用**，但如果 Voice 和 Language 不匹配，可能会朗读失败或自动降级。
- 当你调用 `setLanguage(Locale loc)` 时，TTS 引擎会自动把当前的发音人（Voice）切换为该语言（locale）下的“默认发音人”。
  - 你不需要手动再调用 `setVoice()`，TTS 会帮你选一个该语言下最标准、最常用的 Voice。
  - 如果你想用该语言下的其他发音人（如男声、女声、儿童声），则需要用 `setVoice()` 手动切换。

### 相关方法

- `getAvailableLanguages()`（API 21+）：获取所有支持的语言集合。
- `getVoices()`：获取所有支持的发音人（Voice），每个 Voice 也有 Locale。
- `setVoice(Voice)`：直接切换发音人（包含语言、性别、风格等）。

### 官方文档

- [TextToSpeech.setLanguage | Android Developers](https://developer.android.com/reference/android/speech/tts/TextToSpeech#setLanguage(java.util.Locale))

### 示例代码

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

## 2. setLanguage 和 setVoice 的区别

| 方法          | 控制对象         | 典型参数   | 适用场景           | 备注              |
| ----------- | ------------ | ------ | -------------- | --------------- |
| setLanguage | 语言/地区        | Locale | 只关心语言          | 会自动选该语言的默认Voice |
| setVoice    | 具体发音人(Voice) | Voice  | 需精确选发音人/风格/性别等 | Voice自带语言属性     |

- **setLanguage** 只指定“说什么语言”，发音人由系统决定。
- **setVoice** 直接指定“用哪个发音人”，你可以选性别、风格、特色等，适合高级自定义。

---

## 3. setLanguage 和 setVoice 配合使用

### 推荐流程

1. **先用 setLanguage() 设置目标语言**，保证 TTS 引擎加载了目标语言环境。
2. **用 getVoices() 获取所有可用发音人**，筛选出 Locale 匹配的 Voice。
3. **用 setVoice() 精确切换到你想要的发音人**（如男/女声、儿童声等）。

### 代码示例

```java
// 1. 设置目标语言（如日语）
int langResult = tts.setLanguage(Locale.JAPAN);
if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
    // 该语言不可用
    return;
}

// 2. 获取所有可用 Voice
Set<Voice> voices = tts.getVoices();
Voice targetVoice = null;
for (Voice v : voices) {
    // 3. 选出日语的女声
    if (v.getLocale().equals(Locale.JAPAN) && v.getName().contains("female")) {
        targetVoice = v;
        break;
    }
}

// 4. 切换到目标 Voice
if (targetVoice != null) {
    tts.setVoice(targetVoice);
}
```

### 注意事项

- **顺序建议**：先 setLanguage，再 setVoice。
- **Voice 必须和 Language 匹配**，否则可能朗读失败或降级为默认 Voice。
- **部分 TTS 引擎 setVoice 后会自动覆盖 setLanguage**，但大多数情况下 setVoice 更精确。
- **setLanguage 的返回值要判断**，如果不支持该语言，setVoice 也会失败。

### 典型场景

- 多语言 App：用户输入多种语言，App 自动切换语言和发音人。
- 发音人选择：用户在 UI 上选择男声/女声/特色音色，App 先 setLanguage，再 setVoice。
- 朗读多段不同语言文本：每段切换 setLanguage 和 setVoice，保证朗读效果。

---

## 4. 总结

- `setLanguage` 控制“说什么语言”，发音人由系统自动选。
- `setVoice` 控制“用哪个发音人”，你可以选性别、风格、特色等。
- 配合使用，让 TTS 既能朗读正确语言，又能用你想要的声音风格。 
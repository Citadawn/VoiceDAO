## setLanguage 方法详解

---

### 方法签名

```java
public int setLanguage(Locale loc)
```

- **参数**：`loc` —— 目标语言和地区（`Locale` 对象）
- **返回值**：`int`，表示设置结果的状态码

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

#### LANG_AVAILABLE 与 LANG_COUNTRY_AVAILABLE 的区别

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
- [TextToSpeech.setVoice | Android Developers](https://developer.android.com/reference/android/speech/tts/TextToSpeech#setVoice(android.speech.tts.Voice))

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

### setVoice 方法与 setLanguage 的区别

| 方法          | 控制对象         | 典型参数   | 适用场景           | 备注              |
| ----------- | ------------ | ------ | -------------- | --------------- |
| setLanguage | 语言/地区        | Locale | 只关心语言          | 会自动选该语言的默认Voice |
| setVoice    | 具体发音人(Voice) | Voice  | 需精确选发音人/风格/性别等 | Voice自带语言属性     |

- **setLanguage** 只指定“说什么语言”，发音人由系统决定。
- **setVoice** 直接指定“用哪个发音人”，你可以选性别、风格、特色等，适合高级自定义。

### setLanguage 与 setVoice 配合使用

#### 推荐流程

1. 先 setLanguage() 设置目标语言
2. 用 getVoices() 获取所有可用发音人，筛选 Locale 匹配的 Voice
3. 用 setVoice() 精确切换到目标发音人

#### 代码示例

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

#### 注意事项

- 建议先 setLanguage，再 setVoice
- Voice 必须和 Language 匹配
- setVoice 后部分TTS引擎会覆盖 setLanguage
- setLanguage 的返回值要判断

## isSpeaking 方法详解

---

### 方法签名

```java
public boolean isSpeaking()
```

### 作用

判断当前 TTS 引擎是否正在朗读文本（有语音正在播放）

### 返回值

- `true`：TTS正在朗读
- `false`：TTS空闲

### 典型用法

#### 判断TTS是否正在朗读

```java
if (tts != null && tts.isSpeaking()) {
    // 正在朗读
    Toast.makeText(context, "TTS正在朗读", Toast.LENGTH_SHORT).show();
} else {
    // 没有朗读
    Toast.makeText(context, "TTS未在朗读", Toast.LENGTH_SHORT).show();
}
```

#### 实现“暂停/继续”按钮逻辑

```java
if (tts.isSpeaking()) {
    tts.stop(); // 停止当前朗读
} else {
    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null); // 开始朗读
}
```

#### 在 UI 上显示朗读状态

```java
boolean speaking = tts.isSpeaking();
tvStatus.setText(speaking ? "正在朗读" : "空闲");
```

### 注意事项

- `isSpeaking()` 只能判断**当前TTS实例**是否有朗读任务，不能判断系统全局TTS状态。
- 某些TTS引擎实现可能有延迟（如刚调用`speak()`时，`isSpeaking()`可能还未立即变为`true`）。
- 适合用于UI状态显示、避免重复朗读、实现“朗读中禁止再次点击”等场景。

### 官方文档

- [TextToSpeech.isSpeaking | Android Developers](https://developer.android.com/reference/android/speech/tts/TextToSpeech#isSpeaking())
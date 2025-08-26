官方文档：[TextToSpeech | Android Developers](https://developer.android.com/reference/android/speech/tts/TextToSpeech)

## 方法

---

### getAvailableLanguages

- **说明**：该方法是 `TextToSpeech`（TTS）API 在 API 21（Android 5.0）及以上提供的方法，其作用是获取当前 TTS 引擎支持的所有语言（`Locale`）集合。常用于动态获取可用语言列表，适合多语言朗读、国际化应用。

- **方法签名**：
  
  ```java
  public Set<Locale> getAvailableLanguages()
  ```

- **返回值**：`Set<Locale>`，即一组 `Locale` 对象，每个代表一种支持的语言和地区。

- **用法示例**：
  
  ```java
  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      Set<Locale> locales = tts.getAvailableLanguages();
      for (Locale locale : locales) {
          Log.d("TTS", "支持的语言: " + locale.toString());
      }
  }
  ```

- **注意事项**：
  
  - API 21+ 才支持：低于 Android 5.0 的设备没有此方法。
  - 返回内容取决于 TTS 引擎：不同 TTS 引擎支持的语言不同，返回的 `Locale` 集合也不同。
  - 部分语言需下载语音包：有些 `Locale` 虽然出现在列表中，但实际朗读时可能提示“缺少数据”，需要在系统 TTS 设置中下载对应语音包。
  - 与 `getVoices` 区别：`getAvailableLanguages()` 只返回语言，`getVoices()` 返回具体的发音人（`Voice`），每个 `Voice` 也有 `Locale` 属性。

### getDefaultLanguage

- **说明**：该方法用于获取当前 TTS 引擎的默认语言（`Locale`）。API 21+ 已废弃，推荐用 `getDefaultVoice().getLocale()`。常用于初始化时获取系统 TTS 默认语言。

- **方法签名**：
  
  ```java
  public Locale getDefaultLanguage()
  ```

- **返回值**：`Locale`，即当前 TTS 引擎的默认语言和地区（如 `zh_CN`、`en_US` 等）。

- **用法示例**：
  
  ```java
  Locale defaultLocale;
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      // API 21+ 使用 getDefaultVoice() 获取默认语音的语言
      Voice defaultVoice = tts.getDefaultVoice();
      defaultLocale = (defaultVoice != null) ? defaultVoice.getLocale() : Locale.getDefault();
  } else {
      // API 21 以下使用已弃用的 getDefaultLanguage()
      defaultLocale = tts.getDefaultLanguage();
  }
  Log.d("TTS", "TTS默认语言: " + defaultLocale);
  ```

- **注意事项**：
  
  - API 21+ 推荐用 `getDefaultVoice().getLocale()`。
  - 只能获取当前 TTS 引擎的默认语言，不能直接更改。要更改需引导用户到系统设置页面操作。
  - 返回值取决于系统 TTS 设置页面的选择。

### getLanguage

- **说明**：该方法用于获取当前 TTS 实例正在使用的语言（`Locale`）。反映的是你通过 `setLanguage(Locale locale)` 方法设置的当前朗读语言，而不是系统 TTS 引擎的默认语言。

- **方法签名**：
  
  ```java
  public Locale getLanguage()
  ```

- **返回值**：`Locale` 对象，表示当前 TTS 实例的语言和地区（如 `zh_CN`、`en_US` 等）。

- **用法示例**：
  
  ```java
  Locale currentLocale = tts.getLanguage();
  Log.d("TTS", "当前TTS实例语言: " + currentLocale);
  ```

- **注意事项**：
  
  - 与 `getDefaultLanguage()` 区分，后者是系统 TTS 引擎的默认语言。
  - 返回值取决于最近一次 `setLanguage` 的参数。
  - 兼容性好，早期 API 就有的方法。
  - 该方法在 API 21+ 中已被弃用，API 21 及以上请使用 `getVoice().getLocale()` 替代。

### isLanguageAvailable

- **说明**：该方法用于检测当前 TTS 引擎是否支持某个特定语言或地区。

- **方法签名**：
  
  ```java
  public int isLanguageAvailable(Locale loc)
  ```

- **参数**：`loc` ——需要检测的语言和地区（`Locale` 对象，如 `Locale.US`、`Locale.CHINA` 等）。

- **返回值**：`int`，表示支持情况的状态码：
  
  - `TextToSpeech.LANG_AVAILABLE`：完全支持该语言和地区（如 `en_US`）。
  - `TextToSpeech.LANG_COUNTRY_AVAILABLE`：支持该语言和国家，但不一定支持具体地区（如 `en_CA` 只支持 `en`）。
  - `TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE`：支持该语言、国家和变体（如 `zh_CN_ #Hans` ）。
  - `TextToSpeech.LANG_MISSING_DATA`：缺少该语言的数据包（需在系统 TTS 设置中下载）。
  - `TextToSpeech.LANG_NOT_SUPPORTED`：TTS 引擎不支持该语言。

- **用法示例**：
  
  ```java
  Locale target = Locale.US;
  int result = tts.isLanguageAvailable(target);
  switch (result) {
      case TextToSpeech.LANG_AVAILABLE:
          Log.d("TTS", "完全支持 " + target);
          break;
      case TextToSpeech.LANG_COUNTRY_AVAILABLE:
          Log.d("TTS", "支持该语言，但不完全支持该国家/地区: " + target);
          break;
      case TextToSpeech.LANG_MISSING_DATA:
          Log.d("TTS", "缺少该语言数据包: " + target);
          break;
      case TextToSpeech.LANG_NOT_SUPPORTED:
          Log.d("TTS", "不支持该语言: " + target);
          break;
  }
  ```

- **注意事项**：
  
  - 缺少数据包需下载：如返回 `LANG_MISSING_DATA`，需引导用户到系统 TTS 设置下载语音包。
  - 与 `getAvailableLanguages()` 配合：API 21+ 推荐用 `getAvailableLanguages()` 获取所有可用语言，再用本方法做精细判断。
  - 返回值需判断：不要直接用 `setLanguage()`，应先用本方法判断支持情况。

### setLanguage

- **说明**：该方法用于设置当前 TTS 实例朗读时使用的语言和地区。

- **方法签名**：
  
  ```java
  public int setLanguage(Locale loc)
  ```

- **参数**：`loc` —— 目标语言和地区（`Locale` 对象，如 `Locale.US`、`Locale.CHINA` 等）。

- **返回值**：`int`，表示支持情况的状态码：
  
  - `TextToSpeech.LANG_AVAILABLE`：完全支持该语言和地区（如 `en_US`）。
  - `TextToSpeech.LANG_COUNTRY_AVAILABLE`：支持该语言和国家，但不一定支持具体地区（如 `en_CA` 只支持 `en`）。
  - `TextToSpeech.LANG_MISSING_DATA`：支持该语言、国家和变体（如 `zh_CN_ #Hans` ）。
  - `TextToSpeech.LANG_MISSING_DATA`：缺少该语言的数据包（需在系统 TTS 设置中下载）。
  - `TextToSpeech.LANG_NOT_SUPPORTED`：TTS 引擎不支持该语言。

- **用法示例**：
  
  ```java
  Locale usEnglish = new Locale("en", "US");
  int result = tts.setLanguage(usEnglish);
  if (result == TextToSpeech.LANG_AVAILABLE) {
      // 完全支持美式英语
  } else if (result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
      // 支持英语，但没有专门的美式英语发音
  }
  ```

- **注意事项**：
  
  - 只影响当前 TTS 实例，不影响系统全局设置。
  - 有些语言需要在系统 TTS 设置中下载语音包，否则会返回 `LANG_MISSING_DATA`。
  - `setLanguage` 和 `setVoice` 可以配合使用，但如果 `Voice` 和 `Language` 不匹配，可能会朗读失败或自动降级。
  - 调用 `setLanguage` 后，TTS 会自动把当前发音人切换为该语言下的“默认发音人”。如需自定义发音人，需再调用 `setVoice()`。

### getEngines

- **说明**：该方法用于获取当前设备上已安装的所有 TTS 引擎信息列表。

- **方法签名**：
  
  ```java
  public List<TextToSpeech.EngineInfo> getEngines()
  ```

- **返回值**：`List<TextToSpeech.EngineInfo>`，每个对象代表一个已安装的 TTS 引擎，包含包名、显示名称、是否为系统引擎等信息。

- **用法示例**：
  
  ```java
  List<TextToSpeech.EngineInfo> engines = tts.getEngines();
  for (TextToSpeech.EngineInfo engine : engines) {
      Log.d("TTS", "引擎包名: " + engine.name + ", 显示名称: " + engine.label);
  }
  ```

- **注意事项**：
  
  - 只能获取已安装引擎列表，不能直接切换引擎。切换需引导用户到系统设置操作。
  - 返回值取决于设备上实际安装的 TTS 引擎。
  - 可配合 `getDefaultEngine()` 判断当前默认引擎是哪一个。

### getDefaultEngine

- **说明**：该方法用于获取当前系统设置的默认 TTS 引擎包名。

- **方法签名**：
  
  ```java
  public String getDefaultEngine()
  ```

- **返回值**：`String`，即当前系统设置的 TTS 引擎包名（如 `com.google.android.tts`、`com.iflytek.speechcloud` 等）。可用于兼容性判断、UI 展示等。

- **用法示例**：
  
  ```java
  String engine = tts.getDefaultEngine();
  Log.d("TTS", "当前默认TTS引擎包名: " + engine);
  ```

- **注意事项**：
  
  - 只能获取当前系统设置，不能直接切换 TTS 引擎。如果要切换引擎，需要引导用户到系统设置页面手动更改。
  - 返回值取决于用户在系统“文字转语音输出”设置页面的选择。
  - 可配合 `getEngines()` 判断当前默认引擎是哪一个。

### getVoices

- **说明**：该方法是 `TextToSpeech` API 在 API 21（Android 5.0）及以上提供的方法，用于获取当前 TTS 引擎支持的所有发音人（`Voice`）对象集合。

- **方法签名**：
  
  ```java
  public Set<Voice> getVoices()
  ```

- **返回值**：`Set<Voice>`，即一组 `Voice` 对象，每个代表一个支持的发音人，包含语言、性别、风格等信息。

- **用法示例**：
  
  ```java
  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      Set<Voice> voices = tts.getVoices();
      for (Voice v : voices) {
          Locale locale = v.getLocale();
          String name = v.getName();
          Set<String> features = v.getFeatures();
          Log.d("TTS", "发音人: " + name + "，语言: " + locale + "，特性: " + features);
      }
  }
  ```

- **注意事项**：
  
  - 仅支持 API 21 及以上。
  - 返回内容取决于 TTS 引擎，不同 TTS 引擎支持的 `Voice` 数量和特性不同。
  - `Voice` 特性（features）字段不完全统一，需根据实际引擎适配。
  - 部分 `Voice` 需联网，如 Google TTS 的部分高质量 `Voice`，`isNetworkConnectionRequired()` 返回 `true`。。

### getDefaultVoice

- **说明**：该方法是 `TextToSpeech` API 在 API 21（Android 5.0）及以上提供的方法，用于获取当前 TTS 引擎的默认发音人（`Voice`）对象。

- **方法签名**：
  
  ```java
  public Voice getDefaultVoice()
  ```

- **返回值**：`Voice`，即当前 TTS 引擎的默认发音人对象，包含语言、性别、风格等信息。

- **用法示例**：
  
  ```java
  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      Voice defaultVoice = tts.getDefaultVoice();
      if (defaultVoice != null) {
          Locale defaultLocale = defaultVoice.getLocale();
          String voiceName = defaultVoice.getName();
          Set<String> features = defaultVoice.getFeatures();
          Log.d("TTS", "默认发音人: " + voiceName + "，语言: " + defaultLocale + "，特性: " + features);
      }
  }
  ```

- **注意事项**：
  
  - 仅支持 API 21 及以上。
  - 推荐用 `getDefaultVoice().getLocale()` 获取默认语言。
  - 返回值可能为 `null`，需判空（极少数 TTS 引擎可能未实现）。

### getVoice

- **说明**：该方法是 `TextToSpeech` API 在 API 21（Android 5.0）及以上提供的方法，用于获取当前 TTS 实例正在使用的发音人（`Voice`）对象。

- **方法签名**：
  
  ```java
  public Voice getVoice()
  ```

- **返回值**：`Voice`，即当前 TTS 实例的发音人对象，包含语言、性别、风格等信息。

- **用法示例**：
  
  ```java
  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      Voice currentVoice = tts.getVoice();
      if (currentVoice != null) {
          Locale locale = currentVoice.getLocale();
          String voiceName = currentVoice.getName();
          Set<String> features = currentVoice.getFeatures();
          Log.d("TTS", "当前发音人: " + voiceName + "，语言: " + locale + "，特性: " + features);
      }
  }
  ```

- **注意事项**：
  
  - 仅支持 API 21 及以上。
  - 返回值可能为 `null`，需判空。
  - 与 `getDefaultVoice()` 区别，后者是 TTS 引擎的默认发音人而此方法反映的是通过 `setVoice(Voice voice)` 方法设置的当前发音人。

### setVoice

- **说明**：该方法用于设置当前 TTS 实例朗读时使用的发音人（`Voice`）。

- **方法签名**：
  
  ```java
  public int setVoice(Voice voice)
  ```

- **参数**：`voice` —— 目标发音人对象（Voice），可通过 `getVoices()` 获取。

- **返回值**：`int`，表示设置结果的状态码。`TextToSpeech.SUCCESS` 表示设置成功，`TextToSpeech.ERROR` 表示设置失败。

- **用法示例**：
  
  ```java
  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      Set<Voice> voices = tts.getVoices();
      for (Voice v : voices) {
          if (v.getLocale().equals(Locale.US) && v.getName().contains("female")) {
              int result = tts.setVoice(v);
              if (result == TextToSpeech.SUCCESS) {
                  // 设置成功
              }
              break;
          }
      }
  }
  ```

- **注意事项**：
  
  - Android 5.0 (API 21) 及以上支持该方法。
  - 只影响当前 TTS 实例，不影响系统全局设置。
  - 传入的 `Voice` 必须是当前 TTS 引擎支持的对象，否则会返回错误。
  - `Voice` 对象包含语言、性别、风格等信息，需与当前 `setLanguage` 设置的语言兼容。
  - 不同 TTS 引擎支持的 `Voice` 数量和特性不同，建议用 `getVoices()` 动态获取。

### getFeatures

- **说明**：该方法是 `TextToSpeech`（TTS）API 在 API 21（Android 5.0）及以上提供的方法，用于获取指定 `Locale`（语言/地区）下 TTS 引擎支持的所有特性（如“网络发音”、“高质量”、“低延迟”等）。常用于判断某语言下是否支持特定功能或风格（如是否支持网络合成、儿童音、新闻播报等）。

- **方法签名**：
  
  ```java
  public Set<String> getFeatures(Locale locale)
  ```

- **返回值**：`Set<String>`，包含所有该 `Locale` 下支持的特性字符串。常见特性有 `"networkTts"`, `"embeddedTts"`, `"male"`, `"female"`, `"child"`, `"news"`, `"conversational"` 等。返回空集表示无特性或不支持。

- **用法示例**：
  
  ```java
  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      Locale locale = Locale.US;
      Set<String> features = tts.getFeatures(locale);
      for (String feature : features) {
          Log.d("TTS", "特性: " + feature);
      }
      if (features.contains(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS)) {
          // 支持网络TTS
      }
  }
  ```

- **注意事项**：
  
  - 返回值依赖于当前 TTS 引擎和传入的 `Locale`，不同引擎、不同语言支持的特性不同。
  - 常见特性字符串可参考官方文档或 TTS 引擎说明，部分特性如 `"networkTts"` 需配合网络使用。
  - 如果传入的 `Locale` 不被支持，通常返回空集。
  - 可用于动态判断某语言下是否支持网络合成、特定风格或性别等功能，辅助 UI 控制和功能适配。

### getMaxSpeechInputLength

- **说明**：该方法用于获取 TTS 引擎单次朗读或合成时支持的最大文本长度（字符数）。为静态方法。

- **方法签名**：
  
  ```java
  public static int getMaxSpeechInputLength()
  ```

- **返回值**：`int`，TTS 引擎单次支持的最大输入字符数（通常为 3999），超出部分可能被截断或报错。

- **用法示例**：
  
  ```java
  int maxLen = TextToSpeech.getMaxSpeechInputLength();
  Log.d("TTS", "TTS单次最大输入字符数: " + maxLen);
  ```

- **注意事项**：
  
  - 静态方法，无需 TTS 实例即可调用。
  - 不同 TTS 引擎理论上可返回不同值，但绝大多数返回 3999。
  - 字符数而非字节数，中英文、标点、空格都算一个字符。
  - 超长文本需分段，否则可能导致朗读不完整或报错。
  - 实际开发中建议每次朗读不超过 3500 字符，留出安全余量，兼容所有设备和 TTS 引擎。

### isSpeaking

- **说明**：该方法用于判断当前 TTS 引擎是否正在朗读文本。

- **方法签名**：
  
  ```java
  public boolean isSpeaking()
  ```

- **返回值**：`boolean`，`true` 表示正在朗读，`false` 表示空闲。

- **用法示例**：
  
  ```java
  if (tts != null && tts.isSpeaking()) {
      Log.d("TTS", "TTS正在朗读");
  } else {
      Log.d("TTS", "TTS未在朗读");
  }
  ```

- **注意事项**：
  
  - 只能判断当前 TTS 实例是否有朗读任务，不能判断系统全局 TTS 状态。
  - 某些 TTS 引擎实现可能有延迟，如刚调用 `speak()` 时，`isSpeaking()` 可能还未立即变为 `true`，需配合监听器使用。
  - 适合用于 UI 状态显示、避免重复朗读、实现“朗读中禁止再次点击”等场景。
  - 朗读结束后，`isSpeaking()` 会自动变为 `false`。

### setPitch

- **说明**：该方法用于设置当前 TTS 实例朗读时的音调（高低）。常用于调节朗读高低音。

- **方法签名**：
  
  ```java
  public int setPitch(float pitch)
  ```

- **参数**：`pitch` —— 目标音调，`1.0f` 为正常音调，`<1.0f` 为低音，`>1.0f` 为高音。

- **返回值**：`int`，表示设置结果的状态码。`TextToSpeech.SUCCESS` 表示设置成功，`TextToSpeech.ERROR` 表示设置失败。

- **用法示例**：
  
  ```java
  tts.setPitch(1.0f); // 正常音调
  tts.setPitch(0.8f); // 低音
  tts.setPitch(1.5f); // 高音
  ```

- **注意事项**：
  
  - 只影响当前 TTS 实例，不影响系统全局设置。
  - 不同 TTS 引擎对 `pitch` 支持范围可能不同，通常建议 `0.5f ~ 2.0f` 之间。
  - 设置过高或过低的值，部分 TTS 引擎可能无效或自动调整为默认值。
  - 建议每次朗读前都设置一次，确保音调符合用户期望。

### setSpeechRate

- **说明**：该方法用于设置当前 TTS 实例朗读时的语速。常用于调节朗读快慢。

- **方法签名**：
  
  ```java
  public int setSpeechRate(float speechRate)
  ```

- **参数**：`speechRate` —— 目标语速，`1.0f` 为正常语速，`<1.0f` 为慢速，`>1.0f` 为快速。

- **返回值**：`int`，表示设置结果的状态码。 `TextToSpeech.SUCCESS` 表示设置成功，`TextToSpeech.ERROR` 表示设置失败。

- **用法示例**：
  
  ```java
  tts.setSpeechRate(1.0f); // 正常语速
  tts.setSpeechRate(0.7f); // 慢速
  tts.setSpeechRate(1.5f); // 快速
  ```

- **注意事项**：
  
  - 只影响当前 TTS 实例，不影响系统全局设置。
  - 不同 TTS 引擎对 `speechRate` 支持范围可能不同，通常建议 `0.5f ~ 2.0f` 之间。
  - 设置过高或过低的值，部分 TTS 引擎可能无效或自动调整为默认值。
  - 建议每次朗读前都设置一次，确保语速符合用户期望。

### speak(CharSequence text, int queueMode, Bundle params, String utteranceId)

- **说明**：该方法是 `TextToSpeech` API 在 API 21（Android 5.0）及以上提供的方法，用于让 TTS 引擎朗读指定文本。支持参数自定义、朗读队列控制、进度监听等。

- **方法签名**：
  
  ```java
  public int speak(CharSequence text, int queueMode, Bundle params, String utteranceId)
  ```

- **参数**：
  
  - `text`：要朗读的文本内容（`CharSequence`）。
  - `queueMode`：队列模式，`TextToSpeech.QUEUE_FLUSH`（清空队列后朗读）或 `TextToSpeech.QUEUE_ADD`（追加到队列）。
  - `params`：附加参数（`Bundle`），可为 `null`。可设置音量、流类型等。
  - `utteranceId`：本次朗读的唯一标识符（`String`），用于回调监听。

- **返回值**：`int`，表示操作结果。`TextToSpeech.SUCCESS` 表示操作成功，TTS 开始朗读；`TextToSpeech.ERROR` 表示操作失败，TTS 未能朗读。

- **用法示例**：
  
  ```java
  Bundle params = new Bundle();
  params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
  String utteranceId = "tts_" + System.currentTimeMillis();
  int result = tts.speak("你好，世界！", TextToSpeech.QUEUE_FLUSH, params, utteranceId);
  if (result == TextToSpeech.SUCCESS) {
    // 朗读已开始
  }
  ```

- **注意事项**：
  
  - `utteranceId` 用于区分不同朗读任务，配合 `UtteranceProgressListener` 监听朗读进度和状态。
  - 朗读文本长度不能超过 `TextToSpeech.getMaxSpeechInputLength()` 返回的最大字符数。
  - 朗读前建议先设置好语言、语速、音调等参数。

### speak(String text, int queueMode, HashMap<String, String> params)

- **说明**：该方法用于让 TTS 引擎朗读指定文本，兼容 API 21 以下，API 21+ 已被新方法替代，但仍可用。适合老设备或兼容性场景。

- **方法签名**：
  
  ```java
  public int speak(String text, int queueMode, HashMap<String, String> params)
  ```

- **参数**：
  
  - `text`：要朗读的文本内容（String）。
  - `queueMode`：队列模式，`TextToSpeech.QUEUE_FLUSH`（清空队列后朗读）或 `TextToSpeech.QUEUE_ADD`（追加到队列）。
  - `params`：附加参数（`HashMap<String, String>`），可为 `null`。可设置音量、`utteranceId` 等。

- **返回值**：`int`，表示操作结果。 `TextToSpeech.SUCCESS` 表示操作成功，TTS 开始朗读；`TextToSpeech.ERROR` 表示操作失败，TTS 未能朗读。

- **用法示例**：
  
  ```java
  HashMap<String, String> params = new HashMap<>();
  params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_001");
  params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "1.0");
  int result = tts.speak("你好，世界！", TextToSpeech.QUEUE_FLUSH, params);
  if (result == TextToSpeech.SUCCESS) {
      // 朗读已开始
  }
  ```

- **注意事项**：
  
  - `utteranceId` 用于区分不同朗读任务，配合 `UtteranceProgressListener` 监听朗读进度和状态。
  - 朗读文本长度不能超过 `TextToSpeech.getMaxSpeechInputLength()` 返回的最大字符数。

### stop

- **说明**：该方法用于立即停止当前 TTS 实例正在进行的所有朗读任务。

- **方法签名**：
  
  ```java
  public int stop()
  ```

- **返回值**：`int`，表示操作结果。`TextToSpeech.SUCCESS` 表示操作成功，所有朗读任务已被终止；`TextToSpeech.ERROR` 表示操作失败，TTS 未能停止朗读。

- **用法示例**：
  
  ```java
  if (tts != null && tts.isSpeaking()) {
      int result = tts.stop();
      if (result == TextToSpeech.SUCCESS) {
          // 已成功停止朗读
      }
  }
  ```

- **注意事项**：
  
  - 调用该方法会立即终止所有正在进行的朗读，无论是 `speak()` 还是 `synthesizeToFile()` 任务。
  - 停止后，TTS 实例处于空闲状态，可以重新调用 `speak()` 开始新的朗读。
  - 停止朗读不会释放 TTS 实例资源，如需彻底释放需调用 `shutdown()`。

### shutdown

- **说明**：该方法用于释放当前 TTS 实例占用的所有资源并关闭 TTS 引擎。建议在 `Activity` 或 `Service` 销毁时调用，防止内存泄漏。

- **方法签名**：
  
  ```java
  public void shutdown()
  ```

- **用法示例**：
  
  ```java
  @Override
  protected void onDestroy() {
      if (tts != null) {
          tts.stop();
          tts.shutdown();
      }
      super.onDestroy();
  }
  ```

- **注意事项**：
  
  - 调用该方法后，TTS 实例将不可再用，需重新创建才能继续使用 TTS 功能。
  - 建议在 `Activity` 或 `Service` 销毁时调用，防止内存泄漏和资源浪费。
  - `shutdown()` 会自动停止所有正在进行的朗读任务，无需手动调用 `stop()`，但通常建议先 `stop()` 再 `shutdown()` 以保证流程完整。
  - 调用后再次使用 TTS 相关方法会抛出异常或无效。

### synthesizeToFile(CharSequence text, Bundle params, ParcelFileDescriptor fileDescriptor, String utteranceId)

- **说明**：该方法是 `TextToSpeech` API 在 API 29（Android 10）及以上提供的方法，用于将指定文本合成为音频文件（而不是直接朗读）。

- **方法签名**：
  
  ```java
  public int synthesizeToFile(CharSequence text, Bundle params, ParcelFileDescriptor fileDescriptor, String utteranceId)
  ```

- **参数**：
  
  - `text`：要合成的文本内容（`CharSequence`）。
  - `params`：附加参数（`Bundle`），可为 `null`。可设置音量、流类型等。
  - `fileDescriptor`：目标音频文件的 `ParcelFileDescriptor`，用于指定输出文件。
  - `utteranceId`：本次合成的唯一标识符（`String`），用于回调监听。

- **返回值**：`int`，表示操作结果。`TextToSpeech.SUCCESS` 表示操作成功，合成任务已提交；`TextToSpeech.ERROR` 表示操作失败，未能合成音频。

- **用法示例**：
  
  ```java
  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
    File outFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "tts_output.wav");
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(outFile, ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_TRUNCATE);
    Bundle params = new Bundle();
    params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
    String utteranceId = "tts_file_" + System.currentTimeMillis();
    int result = tts.synthesizeToFile("你好，世界！", params, pfd, utteranceId);
    if (result == TextToSpeech.SUCCESS) {
        // 合成任务已提交
    }
    pfd.close();
  }
  ```

- **注意事项**：
  
  - 合成过程是异步的，需配合 `UtteranceProgressListener` 监听合成进度、完成或失败。
  - `fileDescriptor` 必须是可写的文件描述符，合成完成后需及时关闭。
  - 合成文本长度不能超过 `TextToSpeech.getMaxSpeechInputLength()` 返回的最大字符数。

### synthesizeToFile(CharSequence text, Bundle params, File file, String utteranceId)

- **说明**：该方法是 `TextToSpeech` API 在 API 21（Android 5.0）及以上提供的方法，用于将指定文本合成为音频文件。

- **方法签名**：
  
  ```java
  public int synthesizeToFile(CharSequence text, Bundle params, File file, String utteranceId)
  ```

- **参数**：
  
  - `text`：要合成的文本内容（`CharSequence`）。
  - `params`：附加参数（`Bundle`），可为 `null`。可设置音量、流类型等。
  - `file`：目标音频文件（`File`），用于指定输出文件路径。
  - `utteranceId`：本次合成的唯一标识符（`String`），用于回调监听。

- **返回值**：`int`，表示操作结果。`TextToSpeech.SUCCESS` 表示操作成功，合成任务已提交；`TextToSpeech.ERROR` 表示操作失败，未能合成音频。

- **用法示例**：
  
  ```java
  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      File outFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "tts_output.wav");
      Bundle params = new Bundle();
      params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
      String utteranceId = "tts_file_" + System.currentTimeMillis();
      int result = tts.synthesizeToFile("你好，世界！", params, outFile, utteranceId);
      if (result == TextToSpeech.SUCCESS) {
          // 合成任务已提交
      }
  }
  ```

- **注意事项**：
  
  - 合成过程是异步的，需配合 `UtteranceProgressListener` 监听合成进度、完成或失败。
  - `file` 必须是可写的文件对象，合成完成后可直接读取或播放。
  - 合成文本长度不能超过 `TextToSpeech.getMaxSpeechInputLength()` 返回的最大字符数。

### synthesizeToFile(String text, HashMap<String, String> params, String filename)

- **说明**：该方法用于将指定文本合成为音频文件，兼容 API 21 以下，API 21+ 已被新方法替代，但仍可用。适合老设备或兼容性场景。

- **方法签名**：
  
  ```java
  public int synthesizeToFile(String text, HashMap<String, String> params, String filename)
  ```

- **参数**：
  
  - `text`：要合成的文本内容（`String`）。
  - `params`：附加参数（`HashMap<String, String>`），可为 `null`。可设置音量、`utteranceId` 等。
  - `filename`：目标音频文件的完整路径（`String`），用于指定输出文件。

- **返回值**：`int`，表示操作结果。`TextToSpeech.SUCCESS` 表示操作成功，合成任务已提交；`TextToSpeech.ERROR` 表示操作失败，未能合成音频。

- **用法示例**：
  
  ```java
  String filename = getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/tts_output.wav";
  HashMap<String, String> params = new HashMap<>();
  params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_file_001");
  params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "1.0");
  int result = tts.synthesizeToFile("你好，世界！", params, filename);
  if (result == TextToSpeech.SUCCESS) {
      // 合成任务已提交
  }
  ```

- **注意事项**：
  
  - 合成过程是异步的，需配合 `UtteranceProgressListener` 监听合成进度、完成或失败。
  - `filename` 必须是可写的文件路径，合成完成后可直接读取或播放。
  - 合成文本长度不能超过 `TextToSpeech.getMaxSpeechInputLength()` 返回的最大字符数。

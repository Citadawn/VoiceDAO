官方文档：[Locale  |  API reference  |  Android Developers](https://developer.android.com/reference/java/util/Locale)

## 属性

---

### Script 属性

Locale 对象的脚本（Script）属性用于表示某种语言的书写系统或文字脚本。它在处理国际化（i18n）时非常有用，尤其是当一种语言有多种书写方式时，可以帮助更精确地指定和区分语言的使用场景。

**用途和意义**：区分语言的书写系统，一些语言可能有多种书写系统。例如：

- 中文可以使用简体（`Hans`）或繁体（`Hant`）。
- 塞尔维亚语可以用西里尔字母（`Cyrl`）或拉丁字母（`Latn`）。

使用 `script` 属性可以明确指定使用哪种书写系统，从而避免歧义。

**常见脚本代码**：

- `Hans`：简体中文
- `Hant`：繁体中文
- `Latn`：拉丁字母
- `Cyrl`：西里尔字母
- `Arab`：阿拉伯字母

### Variant 属性

在 Java 中，`Locale` 对象的 `Variant` 是一个可选的附加信息，用于进一步细化语言环境的定义。它通常用来表示特定的区域、技术标准或供应商特定的变体。虽然在大多数情况下，
`Language`（语言）和 `Country`（国家/地区）已经足够，但 `Variant` 提供了额外的灵活性。

**Variant 的作用**：

1. 细化语言环境：
    - 当同一种语言和国家/地区有多个变体时，`Variant` 用于区分这些变体。例如：
        - `Locale("en", "US", "POSIX")`：表示美国英语的 POSIX 变体。
        - `Locale("de", "DE", "PHONEBOOK")`：表示德国德语的电话簿排序规则。
2. 供应商或技术标准的支持：
    - 某些应用程序可能需要支持特定供应商的标准或技术实现，`Variant` 可以用来标识这些特定需求。
3. 自定义用途：
    - 开发者可以根据需要自定义 `Variant`，以便在应用程序中实现特定的逻辑或功能。

**常见的 Variant 值**：

- `POSIX`：表示符合 POSIX 标准的区域设置，通常用于 Unix/Linux 系统。
- `TRADITIONAL`：表示传统的区域设置，例如台湾地区的繁体中文。
- `REVISED`：表示修订后的区域设置，例如简化后的中文。

## 方法

---

### getDefault

- **作用**：该方法是 Java Locale 类的静态方法，用于获取当前 JVM 进程的全局默认 Locale。

- **方法签名**：

  ```java
  public static Locale getDefault()
  ```

- **返回值**：

    - 返回当前 JVM 进程的全局默认 Locale（如 `Locale.SIMPLIFIED_CHINESE`、`Locale.ENGLISH`、`Locale.US`
      等）。

- **默认 Locale 的来源**：

    - 默认 Locale 通常由操作系统的区域设置决定（如系统语言、国家/地区）。
    - 也可以通过 `Locale.setDefault(Locale newLocale)` 方法在运行时修改。

- **用法示例**：

  ```java
    Locale defaultLocale = Locale.getDefault();
    System.out.println(defaultLocale.toString()); // 例如：zh_CN、en_US
    System.out.println(defaultLocale.getDisplayLanguage()); // 例如：中文、English
    System.out.println(defaultLocale.getDisplayCountry());  // 例如：中国、United States
  ```

- **注意事项**：

    - `Locale.getDefault()` 获取的是全局默认 Locale，影响所有未单独指定 Locale 的国际化操作。
    - 可以通过 `Locale.setDefault(Locale newLocale)` 修改全局默认 Locale，影响后续所有相关操作。
    - Java 7 及以上可用 `Locale.getDefault(Locale.Category)` 分别获取“显示”和“格式化”类别的默认
      Locale。

### getDefault(Locale.Category category)

- **作用**：该方法是 Java 7 及以上版本中 Locale 类的静态方法，用于获取指定类别（Category）的默认
  Locale。它允许你分别获取/设置“显示”相关和“格式化”相关的默认 Locale。

- **方法签名**：

  ```java
  public static Locale getDefault(Locale.Category category)
  ```

- **参数**：

    - category

      类型：`Locale.Category`

      说明：指定要获取的 Locale 类型。常用的有两种：

        - `Locale.Category.DISPLAY`：用于界面显示（如菜单、按钮、消息等）的本地化。
        - `Locale.Category.FORMAT`：用于格式化（如日期、数字、货币等）的本地化。

- **返回值**：

    - 返回当前 JVM 进程中指定类别的默认 Locale。
    - 如果未单独设置，返回值等同于 `Locale.getDefault()`。

- **用法示例**：

  ```java
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      // 获取用于界面显示的默认 Locale
      Locale displayLocale = Locale.getDefault(Locale.Category.DISPLAY);
  
      // 获取用于格式化的默认 Locale
      Locale formatLocale = Locale.getDefault(Locale.Category.FORMAT);
  
      // 一般情况下，两者与 Locale.getDefault() 相同，除非你用 setDefault(Category, Locale) 单独设置过
  }
  ```

- **注意事项**：

    - 仅支持 API 24+/Java 7+
    - 可以通过`Locale.setDefault(Locale.Category category, Locale newLocale)`单独设置某一类别的默认
      Locale。
    - 如果未单独设置，`getDefault(Locale.Category.DISPLAY)`和`getDefault(Locale.Category.FORMAT)`
      返回的都是全局默认 Locale（即`Locale.getDefault()`）。

### setDefault(Locale newLocale)

- **作用**：该法是 Java Locale 类的静态方法，用于设置当前 JVM 进程的全局默认 Locale。这会影响所有未单独指定
  Locale 的国际化操作。

- **方法签名**：

  ```java
  public static void setDefault(Locale newLocale)
  ```

- **参数**：

    - newLocale

      类型：`Locale`

      说明：要设置为全局默认的 Locale 实例（如 `Locale.SIMPLIFIED_CHINESE`、`Locale.ENGLISH`、
      `new Locale("fr", "FR")` 等）。

- **功能说明**：

    - 设置后，`Locale.getDefault()` 返回的就是 `newLocale`。
    - 影响所有依赖默认 Locale 的国际化操作（如日期、数字、货币格式化，字符串本地化等）。
    - 仅影响当前 JVM 进程，不会影响系统全局或其他进程。

- **用法示例**：

  ```java
  Locale oldDefault = Locale.getDefault();
  Locale.setDefault(new Locale("fr", "FR"));
  System.out.println(Locale.getDefault()); // 输出：fr_FR
  
  // 恢复原默认Locale，避免影响后续逻辑
  Locale.setDefault(oldDefault);
  ```

- **注意事项**：

    - 该方法会影响所有未显式指定 Locale 的国际化操作，建议谨慎使用。
    - 修改后应及时恢复原默认 Locale，避免影响后续代码或第三方库。
    - 仅影响当前 JVM 进程，不会影响系统设置或其他应用。

### setDefault(Locale.Category category, Locale newLocale)

- **作用**：该方法是 Java 7 及以上版本中 Locale 类的静态方法，用于分别设置 JVM 进程中“显示”或“格式化”类别的默认
  Locale。它允许更细粒度地控制国际化行为。

- **方法签名**：

  ```java
  public static void setDefault(Locale.Category category, Locale newLocale)
  ```

- **参数**：

    - category

      类型：`Locale.Category`（枚举）

      说明：指定要设置的 Locale 类型。常用枚举值有：

        - `Locale.Category.DISPLAY`：用于界面显示（如菜单、按钮、消息等）的本地化。
        - `Locale.Category.FORMAT`：用于格式化（如日期、数字、货币等）的本地化。

    - newLocale

      类型：`Locale`

      说明：要设置为指定类别默认的 Locale 实例（如 `Locale.SIMPLIFIED_CHINESE`、`Locale.ENGLISH`、
      `new Locale("fr", "FR")` 等）。

- **功能说明**：

    - 只影响指定类别（显示/格式化）的默认 Locale，不影响全局默认 Locale。
    - 影响所有依赖该类别默认 Locale 的国际化操作。

- **用法示例**：

  ```java
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      // 保存原设置
      Locale oldDisplay = Locale.getDefault(Locale.Category.DISPLAY);
      Locale oldFormat = Locale.getDefault(Locale.Category.FORMAT);
  
      // 设置显示类别为法语，格式类别为德语
      Locale.setDefault(Locale.Category.DISPLAY, new Locale("fr", "FR"));
      Locale.setDefault(Locale.Category.FORMAT, new Loca
  
      System.out.println(Locale.getDefault(Locale.Category.DISPLAY)); // fr_FR
      System.out.println(Locale.getDefault(Locale.Category.FORMAT));  // de_DE
  
      // 恢复原设置
      Locale.setDefault(Locale.Category.DISPLAY, oldDisplay);
      Locale.setDefault(Locale.Category.FORMAT, oldFormat);
  }
  ```

- **注意事项**：

    - 仅在 Java 7 及以上版本可用。
    - 只影响当前 JVM 进程，不影响系统全局或其他进程。
    - 修改后建议及时恢复原设置，避免影响后续代码或第三方库。

### getDisplayLanguage

- **作用**：获取当前 Locale 对象所代表的语言名称，使用当前的默认语言环境进行本地化显示。

- **方法签名**：

  ```java
  public String getDisplayLanguage()
  ```

- **返回值**：

    - 返回当前 Locale 对象所代表的语言的本地化显示名称（如“英语”、“中文”、“French”等）。
    - 如果当前 Locale 没有指定语言，则返回空字符串。

- **显示语言**：

    - 返回值的语言由系统当前的默认 Locale（即 `Locale.getDefault()`）决定。
        - 例如，系统为中文环境时，返回“英语”；系统为英文环境时，返回“English”。

- **用法示例**：

  ```java
  Locale locale = new Locale("en", "US");
  String lang = locale.getDisplayLanguage(); // 系统为中文环境时，结果："英语"；英文环境时，结果："English"
  ```

- **注意事项**：

    - 该方法依赖于系统的默认 Locale，用户切换系统语言后，显示结果会自动适配。
    - 如果 Locale 没有语言部分（如`new Locale("", "US")`），则返回空字符串。
    - 若需指定显示语言，请使用`getDisplayLanguage(Locale inLocale)`方法。

### getDisplayLanguage(Locale inLocale)

- **作用**：获取当前 Locale 对象所代表的语言名称，并以指定的语言（由参数 locale 决定）进行本地化显示。

- **方法签名**：

  ```java
  public String getDisplayLanguage(Locale inLocale)
  ```

- **参数**:

    - inLocale

      类型：`Locale`

      说明：指定用于显示语言名称的语言环境。例如，传入 `Locale.ENGLISH` 则返回英文名称，传入
      `Locale.CHINESE` 则返回中文名称。

- **返回值**：

    - 返回当前 Locale 对象所代表的语言的本地化显示名称（如“英语”、“中文”、“French”等）。
    - 如果当前 Locale 没有指定语言，则返回空字符串。

- **用法示例**：

  ```java
  Locale locale = new Locale("en", "US");
  String langZh = locale.getDisplayLanguage(Locale.CHINESE); // "英语"
  String langEn = locale.getDisplayLanguage(Locale.ENGLISH); // "English"
  ```

- **注意事项**：

    - 该方法依赖于 Java 运行环境的本地化资源，部分罕见语言在某些语言环境下可能没有本地化名称，返回英文或代码。
    - 如果当前 Locale 没有语言部分（如`new Locale("", "US")`），则返回空字符串。

### getLanguage

- **作用**：用于获取当前 Locale 对象所代表的语言代码（ISO 639-1，两字母）。

- **方法签名**：

  ```java
  public String getLanguage()
  ```

- **返回值**：

    - 返回当前 Locale 对象的两字母语言代码（如 "`zh`"、"`en`"、"`fr`"、"`ja`" 等）。
    - 如果 Locale 没有指定语言，则返回空字符串。

- **用法示例**：

  ```java
  Locale locale = new Locale("en", "US");
  String lang = locale.getLanguage(); // "en"
  
  Locale locale4 = new Locale("", "US"); // 没有语言
  String lang4 = locale4.getLanguage(); // 结果：""
  ```

- **注意事项**：

    - 返回值为小写的两字母代码，符合 ISO 639-1 标准。
    - 如果 Locale 没有语言部分，返回空字符串。
    - 若需三字母代码，请使用 `getISO3Language()` 方法。

### getISO3Language

- **作用**：用于获取当前 Locale 对象所代表的语言的 ISO 639-2（三字母）语言代码。

- **方法签名**：

  ```java
  public String getISO3Language()
  ```

- **返回值**：

    - 返回当前 Locale 对象所代表的语言的三字母（ISO 639-2）语言代码（如 "zho"、"eng"、"fra" 等）。
    - 如果当前 Locale 没有指定语言，返回空字符串。
    - 如果语言代码无效或未知，抛出 `MissingResourceException`。

- **用法示例**：

  ```java
  Locale locale = new Locale("zh", "CN");
  String iso3 = locale.getISO3Language(); // "zho"
  
  Locale locale4 = new Locale("", "US"); // 没有语言
  String iso3Lang4 = locale4.getISO3Language(); // 结果：""
  ```

- **注意事项**：

    - 如果 Locale 没有语言部分，返回空字符串。
    - 如果语言代码无效或未知，抛出 `MissingResourceException`，建议用 try-catch 包裹。
    - 三字母代码与两字母代码的映射由 Java 内部资源文件维护，极少数语言可能不被支持。

### getISOLanguages

- **作用**：该方法是 Java Locale 类的静态方法，用于获取所有有效的 ISO 639-1 两字母语言代码列表。

- **方法签名**：

  ```java
  public static String[] getISOLanguages()
  ```

- **返回值**：

    - 返回一个字符串数组（`String[]`），包含所有有效的 ISO 639-1 两字母语言代码（如 "`zh`"、"`en`"、"
      `fr`"、"`ja`" 等）。
    - 这些代码可用于创建 `Locale` 对象的语言部分。

- **用法示例**：

  ```java
  String[] languages = Locale.getISOLanguages();
  System.out.println("ISO 语言代码总数: " + languages.length);
  for (int i = 0; i < 5; i++) {
      System.out.println(languages[i]);
  }
  // 可能输出：zh, en, fr, ja, de ...
  ```

- **注意事项**：

    - 返回的仅为两字母语言代码，不包含三字母代码或语言全名。
    - 代码顺序未必有实际意义，通常按字母顺序排列。
    - 这些代码与 `Locale` 构造函数的第一个参数（language）兼容。

### getISOLanguages(Locale.IsoLanguageCode type)

- **作用**：该方法是 Java 21 新增的 Locale 类静态方法，用于获取指定类型的 ISO 语言代码集合。相比传统的
  `getISOLanguages()`，它支持获取不同标准下的语言代码（如两字母、三字母等）。

- **方法签名**：

  ```java
    public static Set<String> getISOLanguages(Locale.IsoLanguageCode type)
  ```

- **参数**：

    - type

      类型：`Locale.IsoLanguageCode`（枚举）

      说明：指定要获取的语言代码类型。常用枚举值有：

        - `Locale.IsoLanguageCode.PART1`：ISO 639-1 两字母语言代码（如"`zh`", "`en`"）
        - `Locale.IsoLanguageCode.PART2_B`：ISO 639-2/B 三字母语言代码（Bibliographic，如"`chi`", "
          `eng`"）
        - `Locale.IsoLanguageCode.PART2_T`：ISO 639-2/T 三字母语言代码（Terminology，如 "`zho`", "
          `eng`"）
        - `Locale.IsoLanguageCode.PART3`：ISO 639-3 三字母语言代码（如 "`zho`","`eng`"）

- **返回值**：返回一个 `Set<String>`，包含所有指定类型的 ISO 语言代码。

- **用法示例**：

  ```java
  // 检查 Android API 版本
  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      // API 34+ 支持的新方法
      Set<String> part1 = Locale.getISOLanguages(Locale.IsoLanguageCode.PART1);
      System.out.println("两字母代码总数: " + part1.size());
  
      Set<String> part2b = Locale.getISOLanguages(Locale.IsoLanguageCode.PART2_B);
      System.out.println("三字母B代码总数: " + part2b.size());
  
      Set<String> part2t = Locale.getISOLanguages(Locale.IsoLanguageCode.PART2_T);
      System.out.println("三字母T代码总数: " + part2t.size());
  
      Set<String> part3 = Locale.getISOLanguages(Locale.IsoLanguageCode.PART3);
      System.out.println("三字母639-3代码总数: " + part3.size());
  }
  ```

- **注意事项**：

    - 该方法仅在 Java 21 及以上版本可用。
    - 返回的集合不可修改（unmodifiable）。
    - `Locale.IsoLanguageCode`枚举类型定义了支持的代码标准。
    - 返回的代码可用于 Locale 构造函数的 language 参数，但部分三字母代码可能不被所有 API 支持。

### getDisplayCountry

- **作用**：用于获取当前 Locale 对象所代表的国家/地区的本地化显示名称。

- **方法签名**：

  ```java
  public String getDisplayCountry()
  ```

- **返回值**：

    - 返回当前 Locale 对象所代表的国家/地区的本地化名称（如“中国”、“United States”等）。
    - 如果当前 Locale 没有指定国家，则返回空字符串。

- **显示语言**：

    - 返回值的语言由系统当前的默认 Locale（即 `Locale.getDefault()`）决定。
        - 例如，系统为中文环境时，返回“中国”；系统为英文环境时，返回“China”。

- **用法示例**：

  ```java
  Locale locale = new Locale("en", "US");
  String country = locale.getDisplayCountry(); // 系统为中文环境时，结果："美国"；英文环境时，结果："United States"
  ```

- **注意事项**：

    - 该方法依赖于系统的默认 Locale，用户切换系统语言后，显示结果会自动适配。
    - 如果 Locale 没有国家部分（如`new Locale("en")`），则返回空字符串。
    - 若需指定显示语言，请使用`getDisplayCountry(Locale inLocale)`方法。

### getDisplayCountry(Locale inLocale)

- **作用**：获取当前 Locale 对象所代表的国家/地区名称，并用指定的语言（由参数 inLocale 决定）进行本地化显示。

- **方法签名**：

  ```java
  public String getDisplayCountry(Locale inLocale)
  ```

- **参数**:

    - inLocale

      类型：Locale

      说明：指定用于显示国家/地区名称的语言环境。例如，传入 `Locale.ENGLISH` 则返回英文名称，传入
      `Locale.CHINESE` 则返回中文名称。

- **返回值**：

    - 返回当前 Locale 对象所代表的国家/地区的本地化显示名称（如“中国”、“United States”等）。
    - 如果当前 Locale 没有指定国家，则返回空字符串。

- **用法示例**：

  ```java
  Locale locale = new Locale("en", "US");
  String countryEn = locale.getDisplayCountry(Locale.ENGLISH); // 结果："United States"
  String countryZh = locale.getDisplayCountry(Locale.CHINESE); // 结果："美国"
  ```

- **注意事项**：

    - 该方法依赖于 Java 运行环境的本地化资源，部分罕见国家/地区在某些语言下可能没有本地化名称，返回英文或代码。
    - 如果当前 Locale 没有国家部分（如 new Locale("en")），则返回空字符串。

### getCountry

- **作用**：用于获取当前 Locale 对象所代表的国家/地区代码（ISO 3166-1 alpha-2，两字母）。

- **方法签名**：

  ```java
  public String getCountry()
  ```

- **返回值**：

    - 返回当前 Locale 对象的两字母国家/地区代码（如 "`CN`"、"`US`"、"`FR`"、"`JP`" 等）。
    - 如果 Locale 没有指定国家/地区，则返回空字符串。

- **用法示例**：

  ```java
  Locale locale = new Locale("en", "US");
  String country = locale.getCountry(); // "US"
  
  Locale locale4 = new Locale("en"); // 没有国家
  String country4 = locale4.getCountry(); // 结果：""
  ```

- **注意事项**：

    - 返回值为大写的两字母代码，符合 ISO 3166-1 alpha-2 标准。
    - 如果 Locale 没有国家部分，返回空字符串。
    - 若需三字母代码，请使用 `getISO3Country()` 方法。

### getISO3Country

- **作用**：用于获取当前 Locale 对象所代表的国家/地区的 ISO 3166-1 alpha-3（三字母）国家代码。
- **方法签名**：

```java
public String getISO3Country()
```

- **返回值**：
    - 返回当前 Locale 对象所代表的国家/地区的三字母（ISO 3166-1 alpha-3）国家代码（如 "CHN"、"USA"、"FRA"
      等）。
    - 如果当前 Locale 没有指定国家，返回空字符串。
    - 如果国家代码无效或未知，抛出 `MissingResourceException` 异常。
- **用法示例**：

```java
Locale locale = new Locale("zh", "CN");
String iso3 = locale.getISO3Country(); // "CHN"

Locale locale4 = new Locale("en"); // 没有国家
String iso3Country4 = locale4.getISO3Country(); // 结果：""
```

- **注意事项**：

    - 如果 Locale 没有国家部分，返回空字符串。
    - 如果国家代码无效或未知，抛出`MissingResourceException`，建议用 try-catch 包裹。
    - 三字母代码与两字母代码的映射由 Java 内部资源文件维护，极少数国家/地区可能不被支持。

### getISOCountries

- **作用**：该方法是 Java Locale 类的静态方法，用于获取所有有效的 ISO 3166-1 alpha-2 两字母国家/地区代码列表。

- **方法签名**：

  ```java
  public static String[] getISOCountries()
  ```

- **返回值**：

    - 返回一个字符串数组（`String[]`），包含所有有效的 ISO 3166-1 alpha-2 两字母国家/地区代码（如"CN"、"
      US"、"JP"、"FR"等）。
    - 这些代码可用于创建 Locale 对象的国家部分。

- **用法示例**：

  ```java
    String[] countries = Locale.getISOCountries();
    System.out.println("ISO 国家/地区代码总数: " + countries.length);
    for (int i = 0; i < 5; i++) {
        System.out.println(countries[i]);
    }
    // 可能输出：CN, US, JP, FR, DE ...
  ```

- **注意事项**：

    - 返回的仅为两字母国家/地区代码，不包含三字母代码或地区全名。
    - 代码顺序未必有实际意义，通常按字母顺序排列。
    - 这些代码与`Locale`构造函数的第二个参数（country）兼容。

### getISOCountries(Locale.IsoCountryCode type)

- **作用**：该方法是 Java 21 新增的 Locale 类静态方法，用于获取指定类型的 ISO 国家/地区代码集合。它比传统的
  `getISOCountries()` 更灵活，支持获取不同标准下的国家代码。

- **方法签名**：

  ```java
  public static Set<String> getISOCountries(Locale.IsoCountryCode type)
  ```

- **参数**：

    - type

      类型：`Locale.IsoCountryCode`（枚举）

      说明：指定要获取的国家/地区代码类型。常用枚举值有：

        - `Locale.IsoCountryCode.PART1_ALPHA2`：ISO 3166-1 alpha-2 两字母代码（如 "CN", "US"）
        - `Locale.IsoCountryCode.PART1_ALPHA3`：ISO 3166-1 alpha-3 三字母代码（如 "CHN", "USA"）
        - `Locale.IsoCountryCode.PART3`：ISO 3166-3 四字母已废弃国家代码（如 "BUHH"）

- **返回值**：返回一个 `Set<String>`，包含所有指定类型的 ISO 国家/地区代码。

- **用法示例**：

  ```java
  // 检查当前Android系统版本是否为Android U（API级别34）或更高
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      // API 34+ 支持的Java 21特性
      Set<String> alpha2 = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2);
      System.out.println("alpha-2 总数: " + alpha2.size()); // 如 "CN", "US", "JP"...
  
      Set<String> alpha3 = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3);
      System.out.println("alpha-3 总数: " + alpha3.size()); // 如 "CHN", "USA", "JPN"...
  
      Set<String> part3 = Locale.getISOCountries(Locale.IsoCountryCode.PART3);
      System.out.println("part3 总数: " + part3.size()); // 如 "BUHH"（已废弃国家代码）
  }
  ```

- **注意事项**：

    - 该方法仅在 Java 21 及以上版本可用。
    - 返回的集合不可修改（unmodifiable）。
    - `Locale.IsoCountryCode` 枚举类型定义了支持的代码标准。

### getDisplayName

- **作用**：获取当前 Locale 的完整本地化名称，即包括语言、国家/地区、变体等信息，并以系统当前默认语言环境进行本地化显示。

- **方法签名**：

  ```java
  public String getDisplayName()
  ```

- **返回值**：

    - 返回当前 Locale 对象的完整本地化名称（如“英语（美国）”、“法语（法国）”、“中文（中国）”等）。
    - 如果 Locale 只包含语言，则只返回语言名称；如果包含国家/地区或变体，则会组合显示。

- **显示语言**：

    - 返回值的语言由系统当前的默认 Locale（即 `Locale.getDefault()`）决定。
        - 例如，系统为中文环境时，返回“英语（美国）”；系统为英文环境时，返回“English (United States)”。

- **用法示例**：

  ```java
    Locale locale1 = new Locale("en", "US");
    String name1 = locale1.getDisplayName(); // 中文系统："英语（美国）"；英文系统："English (United States)"
  ```

- **注意事项**：

    - 该方法依赖于系统的默认 Locale，用户切换系统语言后，显示结果会自动适配。
    - 若需指定显示语言，请使用 `getDisplayName(Locale inLocale)` 方法。

### getDisplayName(Locale inLocale)

- **作用**：获取当前 Locale 对象的完整本地化名称，并以指定的语言（由参数 locale 决定）进行本地化显示。

- **方法签名**：

  ```java
  public String getDisplayName(Locale inLocale)
  ```

- **参数**：

    - inLocale

      类型：`Locale`

      说明：指定用于显示名称的语言环境。例如，传入`Locale.ENGLISH`则返回英文名称，传入`Locale.CHINESE`
      则返回中文名称。

- **返回值**：

    - 返回当前 Locale 对象的完整本地化名称（如“英语（美国）”、“法语（法国）”、“中文（中国）”等），并用参数指定的语言环境显示。
    - 如果 Locale 只包含语言，则只返回语言名称；如果包含国家/地区或变体，则会组合显示。

- **用法示例**：

  ```java
  Locale locale = new Locale("en", "US");
  String nameZh = locale.getDisplayName(Locale.CHINESE); // "英语（美国）"
  String nameEn = locale.getDisplayName(Locale.ENGLISH); // "English (United States)"
  ```

- **注意事项**：

    - 该方法依赖于 Java 运行环境的本地化资源，部分罕见语言/国家组合在某些语言环境下可能没有本地化名称，返回英文或代码。
    - 如果 Locale 没有语言部分（如 `new Locale("", "US")`），则返回空字符串。

### toLanguageTag

- **作用**：方法是 Java 7（API 19/Android 4.4）及以上版本中 Locale 类的实例方法，用于将当前 Locale 对象转换为
  BCP 47 标准的语言标签字符串。

- **方法签名**：

  ```java
  public String toLanguageTag()
  ```

- **返回值**：

    - 返回一个符合 BCP 47 标准的语言标签字符串（如 "`zh-CN`"、"`en-US`"、"`fr-FR`"、"`zh-Hans-CN`" 等）。
    - 如果 Locale 没有语言部分，返回 "`und`"（undetermined，未确定）。

- **典型格式**：

    - 语言-国家（如 "`en-US`"、"`zh-CN`"）
    - 语言-脚本-国家（如 "`zh-Hans-CN`"、"`sr-Cyrl-RS`"）
    - 语言-国家-变体（如 "`en-US-POSIX`"）

- **用法示例**：

  ```java
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      Locale locale1 = new Locale("zh", "CN");
      String tag1 = locale1.toLanguageTag(); // 结果："zh-CN"
  
      Locale locale2 = new Locale.Builder().setLanguage("zh").setScript("Hans").setRegion("CN").build();
      String tag2 = locale2.toLanguageTag(); // 结果："zh-Hans-CN"
  
      Locale locale3 = new Locale("en", "US", "POSIX");
      String tag3 = locale3.toLanguageTag(); // 结果："en-US-POSIX"
  
      Locale locale4 = new Locale("", "US");
      String tag4 = locale4.toLanguageTag(); // 结果："und-US"
  }
  ```

- **注意事项**：

    - 该方法仅在 Java 7/Android 4.4 及以上版本可用。
    - 生成的标签可直接用于 `Locale.forLanguageTag(String tag)` 还原为 Locale 对象。
    - 如果 Locale 没有语言部分，返回 "`und`"。

### forLanguageTag

- **作用**：该方法是 Java 7（API 19/Android 4.4）及以上版本中 Locale 类的静态方法，用于根据 BCP 47
  标准的语言标签字符串创建对应的 Locale 对象。

- **方法签名**：

  ```java
  public static Locale forLanguageTag(String languageTag)
  ```

- **参数**：

    - languageTag

      类型：`String`

      说明：符合 BCP 47 标准的语言标签字符串（如 "`zh-CN`"、"`en-US`"、"`fr-FR`"、"`zh-Hans-CN`" 等）。

- **返回值**：

    - 返回一个与指定语言标签对应的 `Locale` 实例。
    - 如果 `languageTag` 为空或无效，返回 `Locale.ROOT`（空 Locale）。

- **典型格式**：

    - 语言-国家（如 "`en-US`"、"`zh-CN`"）
    - 语言-脚本-国家（如 "`zh-Hans-CN`"、"`sr-Cyrl-RS`"）
    - 语言-国家-变体（如 "`en-US-POSIX`"）

- **用法示例**：

  ```java
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      Locale locale1 = Locale.forLanguageTag("zh-CN");
      System.out.println(locale1); // zh_CN
  
      Locale locale2 = Locale.forLanguageTag("zh-Hans-CN");
      System.out.println(locale2.getLanguage()); // zh
      System.out.println(locale2.getScript());   // Hans
      System.out.println(locale2.getCountry());  // CN
  
      Locale locale3 = Locale.forLanguageTag("en-US-POSIX");
      System.out.println(locale3); // en_US_POSIX
  
      Locale locale4 = Locale.forLanguageTag(""); // 空字符串
      System.out.println(locale4); // ""
  }
  ```

- **注意事项**：

    - 该方法仅在 Java 7/Android 4.4 及以上版本可用。
    - 解析能力依赖于 Java 运行环境对 BCP 47 的支持，部分罕见标签可能无法完全还原。
    - 推荐与 `toLanguageTag()` 配合使用，保证互操作性。

### getAvailableLocales

- **作用**：该方法是 Java Locale 类的静态方法，用于获取当前 Java 运行环境中所有可用的 Locale 实例。

- **方法签名**：

  ```java
  public static Locale[] getAvailableLocales()
  ```

- 返回值

    - 返回一个 `Locale` 数组（`Locale[]`），包含当前 Java 运行环境支持的所有 Locale 实例。
    - 这些 Locale 由 Java 平台和已安装的本地化资源包共同决定。

- **用法示例**：

  ```java
  Locale[] locales = Locale.getAvailableLocales();
  System.out.println("可用Locale总数: " + locales.length);
  for (int i = 0; i < 5; i++) {
      System.out.println(locales[i]);
  }
  // 可能输出：zh_CN, en_US, fr_FR, ja_JP, de_DE ...
  ```

- **注意事项**：

    - 返回的 Locale 包含所有 Java 支持的语言-国家-变体组合，不一定都被 TTS、日期格式化等底层服务支持。
    - 某些 Locale 可能仅用于特定的本地化资源包，实际支持情况依赖于 JRE/JDK 及其本地化包。
    - 与 `TextToSpeech.getAvailableLanguages()` 不同，后者仅返回 TTS 引擎实际支持的语言。

### toString

- **作用**：用于返回当前 Locale 对象的字符串表示。该字符串通常用于调试、日志输出或快速查看 Locale
  的各组成部分。

- **方法签名**：

  ```java
  public String toString()
  ```

- **返回值**：

    - 返回当前 Locale 对象的字符串表示，格式通常为：
        - language
        - language_country
        - language_country_variant
    - 其中：
        - language：两字母语言代码（如 "`en`"、"`zh`"）
        - country：两字母国家/地区代码（如 "`US`"、"`CN`"）
        - variant：变体（如 "`POSIX`"、"`PINYIN`"）

- **用法示例**：

  ```java
  Locale locale1 = new Locale("en", "US");
  System.out.println(locale1.toString()); // 输出："en_US"
  
  Locale locale2 = new Locale("zh", "CN", "PINYIN");
  System.out.println(locale2.toString()); // 输出："zh_CN_PINYIN"
  
  Locale locale3 = new Locale("fr");
  System.out.println(locale3.toString()); // 输出："fr"
  
  Locale locale4 = new Locale("", "US");
  System.out.println(locale4.toString()); // 输出："_US"
  ```

- **注意事项**：

    - `toString()` 只包含语言、国家、变体，不包含脚本（Script）和 Unicode 扩展等新特性。
    - 若需标准 BCP 47 语言标签，请使用 `toLanguageTag()` 方法。

### getScript

- **作用**：该方法是 Java 7（API 19/Android 4.4）及以上版本中 Locale 类的实例方法，用于获取当前 Locale
  对象的脚本（Script）代码。
- **方法签名**：

```java
public String getScript()
```

- **返回值**：

    - 返回当前 Locale 对象的脚本（Script）代码（如 "`Hans`"、"`Hant`"、"`Latn`"、"`Cyrl`" 等）。
    - 如果 Locale 没有指定脚本，则返回空字符串。

- **典型脚本代码举例**：

    - "`Hans`"：简体（Simplified Chinese）
    - "`Hant`"：繁体（Traditional Chinese）
    - "`Latn`"：拉丁文（Latin）
    - "`Cyrl`"：西里尔文（Cyrillic）
    - "`Arab`"：阿拉伯文（Arabic）

- **用法示例**：

  ```java
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      Locale locale = new Locale.Builder().setLanguage("zh").setScript("Hans").setRegion("CN").build();
      String script = locale.getScript(); // "Hans"
  ```

        Locale noScript = new Locale("en", "US");
        String script4 = noScript.getScript(); // 结果：""

  }

  ```
  
  ```

- **注意事项**：

    - 该方法仅在 Java 7/Android 4.4 及以上版本可用。
    - 返回值为 BCP 47 标准的四字母脚本代码。
    - 如果 Locale 没有脚本部分，返回空字符串。

### getDisplayScript

- **作用**：该方法是 Java 7（API 级别 19/Android 4.4）及以上版本中 Locale 类的实例方法，用于获取当前
  Locale 对象的脚本（Script）名称，并以系统当前默认语言环境进行本地化显示。

- **方法签名**：

  ```java
  public String getDisplayScript()
  ```

- **返回值**：

    - 返回当前 Locale 对象的脚本（Script）名称的本地化显示名称（如“拉丁文”、“西里尔文”、“阿拉伯文”等）。
    - 如果 Locale 没有指定脚本，则返回空字符串。

- **显示语言**：

    - 返回值的语言由系统当前的默认 Locale（即 `Locale.getDefault()`）决定。
        - 例如，系统为中文环境时，返回“西里尔文”；系统为英文环境时，返回“Cyrillic”。

- **用法示例**：

  ```java
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      Locale locale1 = new Locale.Builder().setLanguage("sr").setScript("Cyrl").setRegion("RS").build();
      String script1 = locale1.getDisplayScript(); // 中文系统："西里尔文"；英文系统："Cyrillic"
  }
  ```

- **注意事项**：

    - 该方法依赖于 Locale 是否包含脚本信息（如 "Hans"、"Hant"、"Latn"、"Cyrl" 等）。
    - 如果 Locale 没有脚本部分，返回空字符串。
    - 若需指定显示语言，请使用`getDisplayScript(Locale inLocale)`方法。
    - 仅在 Java 7/Android 4.4 及以上版本可用。

### getDisplayScript(Locale inLocale)

- **作用**：该方法是 Java 7（API 19/Android 4.4）及以上版本中 Locale 类的实例方法，用于获取当前 Locale
  对象的脚本（Script）名称，并以指定的语言（由参数 inLocale 决定）进行本地化显示。

- **方法签名**：

  ```java
  public String getDisplayScript(Locale inLocale)
  ```

- **参数**：

    - inLocale

      类型：`Locale`

      说明：指定用于显示脚本名称的语言环境。例如，传入 `Locale.ENGLISH` 则返回英文名称，传入
      `Locale.CHINESE` 则返回中文名称。

- **返回值**：

    - 返回当前 Locale 对象的脚本（Script）名称的本地化显示名称（如“拉丁文”、“西里尔文”、“阿拉伯文”、“简体”、“繁体”等）。
    - 如果 Locale 没有指定脚本，则返回空字符串。

- **用法示例**：

  ```java
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      Locale locale = new Locale.Builder().setLanguage("zh").setScript("Hans").setRegion("CN").build();
      String scriptZh = locale.getDisplayScript(Locale.CHINESE); // "简体"
      String scriptEn = locale.getDisplayScript(Locale.ENGLISH); // "Simplified"
  }
  ```

- **注意事项**：

    - 该方法依赖于 Locale 是否包含脚本信息（如 "Hans"、"Hant"、"Latn"、"Cyrl" 等）。
    - 如果 Locale 没有脚本部分，返回空字符串。
    - 仅在 Java 7/Android 4.4 及以上版本可用。

### getDisplayVariant(Locale inLocale)

- **作用**：用于获取当前 Locale 对象的变体（Variant）名称，并以指定的语言（由参数 inLocale 决定）进行本地化显示。

- **方法签名**：

  ```java
  public String getDisplayVariant(Locale inLocale)
  ```

- **参数**：

    - inLocale

      类型：`Locale`

      说明：指定用于显示变体名称的语言环境。例如，传入 `Locale.ENGLISH` 则返回英文名称，传入
      `Locale.CHINESE` 则返回中文名称。

- **返回值**：

    - 返回当前 Locale 对象的变体（Variant）名称的本地化显示名称（如“拼音”、“注音”、“POSIX”等）。
    - 如果 Locale 没有指定变体，则返回空字符串。

- **用法示例**：

  ```java
  Locale locale = new Locale("zh", "CN", "PINYIN");
  String variantZh = locale.getDisplayVariant(Locale.CHINESE); // "拼音"
  String variantEn = locale.getDisplayVariant(Locale.ENGLISH); // "Pinyin"
  ```

- **注意事项**：

    - 该方法依赖于 Locale 是否包含变体信息（Variant）。
    - 如果 Locale 没有变体部分，返回空字符串。
    - 变体通常用于进一步细分同一语言-国家-脚本下的特殊用法。

### getVariant

- **作用**：用于获取当前 Locale 对象的变体（Variant）代码。

- **方法签名**：

  ```java
  public String getVariant()
  ```

- **返回值**：

    - 返回当前 Locale 对象的变体（Variant）代码（如"`POSIX`"、"`PINYIN`"、"`TRADITIONAL`" 等）。
    - 如果 Locale 没有指定变体，则返回空字符串。

- **用法示例**：

  ```java
  Locale locale = new Locale("en", "US", "POSIX");
  String variant = locale.getVariant(); // "POSIX"
  
  Locale locale3 = new Locale("fr", "FR");
  String variant3 = locale3.getVariant(); // 结果：""
  ```

- **注意事项**：

    - 变体代码区分大小写，通常为大写字母。
    - 如果 Locale 没有变体部分，返回空字符串。
    - 变体的具体含义和支持情况依赖于应用场景和底层实现。

### getDisplayVariant

- **作用**：用于获取当前 Locale 对象的变体（Variant）名称，并以系统当前默认语言环境进行本地化显示。

- **方法签名**：

  ```java
  public String getDisplayVariant()
  ```

- **返回值**：

    - 返回当前 Locale 对象的变体（Variant）名称的本地化显示名称（如“拼音”、“注音”、“POSIX”等）。
    - 如果 Locale 没有指定变体，则返回空字符串。

- **显示语言**：

    - 返回值的语言由系统当前的默认 Locale（即 `Locale.getDefault()`）决定。
        - 例如，系统为中文环境时，返回“拼音”；系统为英文环境时，返回“Pinyin”。

- **用法示例**：

  ```java
  Locale locale1 = new Locale.Builder()
      .setLanguage("zh")
      .setScript("Hans")
      .setRegion("CN")
      .setVariant("PINYIN")
      .build();
  String variant1 = locale1.getDisplayVariant(); // 中文系统："拼音"；英文系统："Pinyin"
  
  Locale locale2 = new Locale("en", "US", "POSIX");
  String variant2 = locale2.getDisplayVariant(); // 中文系统："POSIX"；英文系统："POSIX"
  ```

- **注意事项**：

    - 该方法依赖于 Locale 是否包含变体信息（Variant）。
    - 如果 Locale 没有变体部分，返回空字符串。
    - 变体通常用于进一步细分同一语言-国家-脚本下的特殊用法。
    - 若需指定显示语言，请使用 `getDisplayVariant(Locale inLocale)` 方法。

### getDisplayVariant (Locale inLocale)

- **作用**：用于获取当前 Locale 对象的变体（Variant）名称，并以指定的语言（由参数 inLocale 决定）进行本地化显示。

- **方法签名**：

  ```java
  public String getDisplayVariant(Locale inLocale)
  ```

- **参数**：

    - inLocale

      类型：`Locale`

      说明：指定用于显示变体名称的语言环境。例如，传入`Locale.ENGLISH`则返回英文名称，传入
      `Locale.CHINESE`则返回中文名称。

- **返回值**：

    - 返回当前 Locale 对象的变体（Variant）名称的本地化显示名称（如“拼音”、“注音”、“POSIX”等）。
    - 如果 Locale 没有指定变体，则返回空字符串。

- **用法示例**：

  ```java
  Locale locale = new Locale("zh", "CN", "PINYIN");
  String variantZh = locale.getDisplayVariant(Locale.CHINESE); // "拼音"
  String variantEn = locale.getDisplayVariant(Locale.ENGLISH); // "Pinyin"
  ```

- **注意事项**：

    - 该方法依赖于 Locale 是否包含变体信息（Variant），如果 Locale 没有变体部分，返回空字符串。
    - 变体通常用于进一步细分同一语言-国家-脚本下的特殊用法。
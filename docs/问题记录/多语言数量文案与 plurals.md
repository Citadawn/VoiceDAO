# 多语言数量文案与 plurals

## 现象

---

界面需要显示**带数量**的句子，且英文等语言须随数量改变语法（单数 / 复数），例如：

- `1 TTS engine` / `5 TTS engines`
- `No items selected`（数量为 0 时的专门说法）

若在 Java 里写 `count == 1 ? "engine" : "engines"` 再拼进 `@string`，翻译人员无法按语言规则维护，波兰语、俄语等还可能需要 `few`、`many` 等更多分支。

中文里「1 个引擎」「5 个引擎」句式通常相同，**`<plurals>` 对中文几乎无语法收益**，但对英文（及多种外语）是标准做法。

## 原因

---

| 因素 | 说明 |
|------|------|
| 各语言复数规则不同 | Android 用 `quantity`（`zero` / `one` / `two` / `few` / `many` / `other`）按**当前语言**选条目，不由应用手写 if |
| `<string>` 只存一条 | 无法表达「同一含义、多种数量语法」 |
| 中文 | 多数场景 `one` 与 `other` 可写相同文案；系统仍按数量选条，但翻译上常合并为一条语义 |

`<plurals>` **只适用于数量为整数、且文案随数量变化**的场景；状态枚举（成功 / 失败）、性别、固定选项等应使用多个 `@string` 或 `string-array`，不能滥用 plurals 充当通用分支。

## 用法

---

**资源**（`values/strings.xml` 英文示例）：

```xml
<plurals name="debug_items_selected">
    <item quantity="zero">No debug items selected</item>
    <item quantity="one">%d debug item selected</item>
    <item quantity="other">%d debug items selected</item>
</plurals>
```

**资源**（`values-zh/strings.xml`；中文可两条相同）：

```xml
<plurals name="debug_items_selected">
    <item quantity="one">已选 %d 项调试项</item>
    <item quantity="other">已选 %d 项调试项</item>
</plurals>
```

**Java**：

```java
int count = selectedCount;
String text = getResources().getQuantityString(
        R.plurals.debug_items_selected, count, count);
```

第二个参数 `count` 用于**选择** `quantity` 条目；后续参数填入 `%d`、`%s` 等占位符。

## 语道项目现状

---

当前仓库**未使用** `<plurals>` 与 `getQuantityString`；界面文案以固定 `@string` 为主。若英文界面出现「共 N 个引擎 / 已选 N 项」且须正确单复数，在 `values` / `values-zh` 中成对添加同名 `plurals` 即可。

## 自测

---

1. 系统语言为英文：`count = 0 / 1 / 2` 时文案分别符合 `zero` / `one` / `other`（若已定义）。
2. 系统语言为中文：数量变化时语句通顺，无英文式单复数错误。
3. 两种语言的 `plurals` **name 与 quantity 键一致**，仅 `<item>` 正文不同。

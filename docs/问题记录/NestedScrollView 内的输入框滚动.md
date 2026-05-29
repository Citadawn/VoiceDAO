# NestedScrollView 内的输入框滚动

## 现象

---

主界面朗读文本输入框设 `maxLines` 等限制高度，正文超出后须能在框内单独上下滑动阅读。可能出现两类问题：

1. **框内滚不动**：手指在输入框上拖动时，只有外层主界面在滚，框内正文不动。
2. **框内能滚但画面弹回光标一侧**（部分为偶发）：光标仍停在某一位置（例如第 3 行），用户却在输入框内上下滑动，想阅读屏幕上已被挡住的、光标上面或下面的段落（例如第 10 行）；滑动过程中或松手后，正文突然整体滑回光标附近，无法停在用户正在看的那一段。

文本编辑器（`activity_text_editor.xml`）的 `EditText` 位于 Toolbar 与底栏之间，**没有**外层 `NestedScrollView`，一般不出现上述问题，使用标准 `EditText` 即可。

## 原因

---

主界面整页包在 `NestedScrollView`（`activity_main.xml`）内，输入框与下方设置项同属可滚区域。

| 问题 | 成因 |
|------|------|
| 框内滚不动 | 父级 `NestedScrollView` 默认拦截滑动手势，框内 `scrollY` 不变 |
| 画面弹回光标一侧 | 用户用手指改变了框内 `scrollY` 以浏览长文，但框架仍通过 `bringPointIntoView`、`requestRectangleOnScreen` 等把包含光标的区域滚进可视范围，与「只看别处、暂不移动光标」冲突 |

标准 `EditText` 在框内滑动浏览长文时，同样可能触发上述「跟光标」行为。`ManualScrollEditText` 继承 `EditText`，在手动滑动期间重写 `bringPointIntoView`、`requestRectangleOnScreen`，抑制自动滚向光标。

## 解决办法

---

**主界面**（`activity_main.xml`）使用 `ManualScrollEditText`（`app/src/main/java/com/citadawn/speechapp/ui/ManualScrollEditText.java`），在标准 `EditText` 之上增加两类处理：

1. **嵌套滚动**：内容溢出时，在框内手势期间对父级 `requestDisallowInterceptTouchEvent(true)`，框内滚动优先于外层页面。
2. **滑动浏览**：手指拖滚期间抑制 `bringPointIntoView`、`requestRectangleOnScreen`；点按定位光标或继续输入时恢复跟光标。

**文本编辑器**等无外层 `NestedScrollView` 的页面继续使用标准 `EditText`。

## 自测

---

1. 主界面输入超过 `maxLines` 的长文，在框内拖动：框内应滚、外层不应跟滚。
2. 光标停在文中段，在框内反复上下滑动去看其他段落（含慢滑、快滑），松手后画面应停在当前阅读位置，不应弹回光标一侧。
3. 点按移动光标或继续输入，光标应正常跟随。
4. 文本编辑器长文：框内可滚，无外层页面抢滚动。

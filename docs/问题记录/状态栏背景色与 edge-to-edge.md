# 状态栏背景色与 edge-to-edge

## 现象

---

语道各页面的 Toolbar 背景为深色（`@color/main_dark`，`#272727`），状态栏区域应与 Toolbar **同色**，系统图标（时间、电量等）为**浅色**。实际可能出现下列情况：

1. **状态栏背景色无法生效**：调用 `window.setStatusBarColor(main_dark)` 后，状态栏仍为系统默认浅色、透明底下的页面内容，或与 Toolbar 色差明显；在 targetSdk 35 及以上、部分厂商 ROM 上更易出现。
2. **图标颜色与背景不匹配**：状态栏已变色，但系统图标仍为白色（浅色条）或黑色（深色条），与 Toolbar 对比度错误、难以辨认。
3. **全屏蒙层 Dialog 盖住状态栏配色**：打开调试面板（`TestModeDialog`）等铺满屏幕的 Dialog 时，状态栏区域出现半透明灰蒙层或另一套配色，与主界面 Toolbar 不一致；关闭 Dialog 后偶发需切换页面才恢复。
4. **刘海、挖孔、横竖屏切换后顶部错位**：若状态栏高度写死为固定 `dp`，顶部色块与系统图标区域可能对不齐，或与 Toolbar 之间出现缝隙。

## 原因

---

| 因素 | 说明 |
|------|------|
| targetSdk 35+ 强制 edge-to-edge | 系统倾向透明状态栏，由应用自行处理 inset；单纯 `setStatusBarColor` 在部分机型上不稳定或被覆盖 |
| 状态栏高度非固定常量 | 系统通过 `WindowInsets` 报告顶部 inset；刘海、挖孔、横竖屏、分屏、厂商 ROM 均会导致高度变化，不宜写死 `24dp` 等数值 |
| Activity 与 Dialog 各有一套 Window | 全屏 Dialog 的 Window 默认铺满屏幕含状态栏区域；透明状态栏 + `dimAmount` 蒙层时，状态栏处显示的是 Dialog 窗口的半透明底，而非 Activity 内 Toolbar 的延伸色 |
| 图标样式独立于背景色 | `windowLightStatusBar` / `WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS` 决定图标深浅，须与 Toolbar 背景一并配置 |

## 解决办法

---

语道使用 `SystemBarsHelper`（`app/src/main/java/com/citadawn/speechapp/util/SystemBarsHelper.java`）统一处理 Activity 与全屏蒙层 Dialog。主题 `Theme.VoiceDAO` 声明透明系统栏与 `android:windowLightStatusBar=false`。

**Activity（主界面、文本编辑器、TTS 浏览器）**

1. `onCreate` 中调用 `SystemBarsHelper.enable(activity)`：内部执行 `EdgeToEdge.enable`，并将状态栏设为透明、图标为浅色。
2. Toolbar 贴顶布局，调用 `applyToolbarTopInsets(toolbar)`：读取 `WindowInsetsCompat.Type.statusBars()` 的 `top` 作为 Toolbar 的 `paddingTop`，背景色自然延伸至状态栏区域，标题与导航图标避开系统图标。
3. 根容器调用 `applyContentMarginInsets(contentRoot)`：处理左右与底部导航栏 inset；顶部由 Toolbar 负责。
4. `onResume` 中调用 `SystemBarsHelper.reapply(activity)`：从 Dialog 返回后恢复透明状态栏与图标样式。

**全屏蒙层 Dialog（调试面板）**

在 `TestModeDialog` 等窗口上调用 `applySolidStatusBarForOverlay(window, context)`：对 Dialog 的 Window 设置 `FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS`，状态栏实色为 `main_dark`，图标仍为浅色；避免透明蒙层在状态栏区域「洗白」主界面配色。

文本编辑器底部栏须单独监听 `WindowInsetsCompat.Type.ime()`，与系统栏 inset 取较大值，该逻辑保留在 `TextEditorActivity`，不在 `SystemBarsHelper` 内。

## 自测

---

1. 主界面、文本编辑器、TTS 浏览器：状态栏与 Toolbar 同色（`#272727`），系统图标清晰可辨。
2. 连续点击标题打开调试面板：状态栏保持实色深色，不出现灰蒙层盖住的状态栏。
3. 关闭调试面板或弹出其中的说明 `AlertDialog` 后返回：状态栏样式与主界面一致。
4. 若有刘海机或开启手势导航：顶部无内容与系统图标重叠，Toolbar 标题不被遮挡。

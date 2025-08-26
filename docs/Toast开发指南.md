# Toast 开发指南

---

## 概述

本指南介绍如何在 VoiceDAO 项目中正确使用 Toast 消息提示功能。项目使用自定义 Toast 样式，确保所有 Toast 消息具有一致的外观和用户体验。

## Toast 样式规范

---

### 视觉设计

- **背景**：白色圆角矩形
- **文字颜色**：深色（`@color/text_primary`）
- **圆角半径**：8dp
- **内边距**：12dp
- **阴影效果**：8dp 高度阴影
- **边框**：浅灰色细边框

### 布局特性

- **最大行数**：支持最多 3 行文本
- **文本溢出**：超出部分显示省略号
- **自适应宽度**：根据内容自动调整宽度
- **垂直居中**：文本垂直居中对齐

## 使用方法

---

### 基本用法

在需要显示 Toast 消息的地方，使用 `ToastHelper` 工具类：

```java
// 显示字符串消息
ToastHelper.showShort(context, "这是一条消息");

// 显示资源字符串
ToastHelper.showShort(context, R.string.message_key);

// 显示带格式化参数的资源字符串
ToastHelper.showShort(context, R.string.message_with_params, "参数1", "参数2");
```

### 实际示例

```java
// 成功提示
ToastHelper.showShort(this, R.string.toast_save_audio_success);

// 错误提示
ToastHelper.showShort(this, R.string.toast_save_audio_write_fail);

// 警告提示
ToastHelper.showShort(this, R.string.toast_text_exceeds_limit, maxLength);

// 信息提示
ToastHelper.showShort(this, "自定义消息内容");
```

## 开发规范

---

### 1. 使用 ToastHelper 工具类

**✅ 正确做法：**

```java
import com.citadawn.speechapp.util.ToastHelper;

// 在 Activity 或 Fragment 中
ToastHelper.showShort(this, R.string.message_key);
```

**❌ 错误做法：**

```java
// 直接使用系统 Toast，不会应用自定义样式
Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
```

### 2. 字符串资源管理

**✅ 正确做法：**

```java
// 使用字符串资源 ID
ToastHelper.showShort(this, R.string.toast_save_success);
```

**❌ 错误做法：**

```java
// 硬编码字符串，不支持国际化
ToastHelper.showShort(this, "保存成功");
```

### 3. 国际化支持

所有 Toast 消息必须支持多语言：

**英文资源文件**：`app/src/main/res/values/strings.xml`

```xml
<string name="toast_save_success">Save successful</string>
```

**中文资源文件**：`app/src/main/res/values-zh/strings.xml`

```xml
<string name="toast_save_success">保存成功</string>
```

### 4. 消息长度控制

- **短消息**：建议不超过 20 个字符
- **中等消息**：建议不超过 40 个字符
- **长消息**：超过 40 个字符时考虑使用对话框

## 常见使用场景

---

### 操作反馈

```java
// 保存操作成功
ToastHelper.showShort(this, R.string.toast_save_audio_success);

// 操作失败
ToastHelper.showShort(this, R.string.toast_save_audio_write_fail);

// 操作进行中
ToastHelper.showShort(this, R.string.toast_saving_audio);
```

### 状态提示

```java
// 系统状态
ToastHelper.showShort(this, R.string.status_ready);

// 用户操作提示
ToastHelper.showShort(this, R.string.hint_input_text);
```

### 错误处理

```java
// 输入验证
if (text.isEmpty()) {
    ToastHelper.showShort(this, R.string.hint_input_text);
    return;
}

// 权限或系统错误
if (!hasPermission()) {
    ToastHelper.showShort(this, R.string.toast_permission_denied);
    return;
}
```

## 添加新的 Toast 消息

---

### 步骤 1：定义字符串资源

在 `app/src/main/res/values/strings.xml` 中添加：

```xml
<string name="toast_new_feature">New feature available</string>
```

在 `app/src/main/res/values-zh/strings.xml` 中添加：

```xml
<string name="toast_new_feature">新功能可用</string>
```

### 步骤 2：在代码中使用

```java
// 显示新功能提示
ToastHelper.showShort(this, R.string.toast_new_feature);
```

### 步骤 3：测试验证

- 确保英文和中文环境下都能正确显示
- 验证文本长度是否合适
- 检查在不同屏幕尺寸下的显示效果

## 最佳实践

---

### 1. 消息内容设计

- **简洁明了**：用最少的文字表达核心信息
- **用户友好**：使用用户能理解的术语
- **一致性**：保持与其他 Toast 消息的风格一致

### 2. 显示时机

- **及时反馈**：操作完成后立即显示
- **避免干扰**：不要在用户正在输入时显示
- **频率控制**：避免短时间内显示过多 Toast

### 3. 错误处理

- **具体明确**：说明具体的错误原因
- **提供建议**：给出解决错误的建议
- **避免技术术语**：使用用户能理解的语言

## 故障排除

---

### 常见问题

**Q: Toast 显示为系统默认样式**
A: 确保使用 `ToastHelper.showShort()` 而不是 `Toast.makeText()`

**Q: 字符串资源找不到**
A: 检查字符串资源是否在 `strings.xml` 中正确定义

**Q: 中文显示为英文**
A: 检查 `values-zh/strings.xml` 中是否有对应的中文翻译

**Q: Toast 样式异常**
A: 检查 `custom_toast_layout.xml` 和 `toast_background.xml` 文件是否存在

### 调试技巧

```java
// 临时使用系统 Toast 进行调试
Toast.makeText(context, "调试信息", Toast.LENGTH_LONG).show();

// 使用 Log 输出调试信息
Log.d("ToastDebug", "Toast 消息内容: " + message);
```

### 常见警告和解决方案

**警告：Avoid passing null as the view root**

**原因**：在 `LayoutInflater.inflate()` 中传递 `null` 作为父视图，可能导致布局参数无法正确解析。

**解决方案**：使用临时父视图来正确解析布局参数：

```java
// ✅ 正确做法
ViewGroup parent = new android.widget.FrameLayout(context);
View toastView = LayoutInflater.from(context).inflate(R.layout.custom_toast_layout, parent, false);

// ❌ 错误做法
View toastView = LayoutInflater.from(context).inflate(R.layout.custom_toast_layout, null);
```

**警告：Value of parameter 'duration' is always 'Toast.LENGTH_SHORT'**

**原因**：`showCustomToast` 方法中的 `duration` 参数总是被传入相同的值，参数没有被充分利用。

**解决方案**：简化方法签名，移除不必要的参数：

```java
// ✅ 正确做法
private static void showCustomToast(Context context, String message) {
    Toast toast = new Toast(context);
    toast.setDuration(Toast.LENGTH_SHORT);
    // ... 其他代码
}

// ❌ 错误做法
private static void showCustomToast(Context context, String message, int duration) {
    Toast toast = new Toast(context);
    toast.setDuration(duration); // duration 总是 Toast.LENGTH_SHORT
    // ... 其他代码
}
```

## 相关文件

---

- **工具类**：`app/src/main/java/com/citadawn/speechapp/util/ToastHelper.java`
- **布局文件**：`app/src/main/res/layout/custom_toast_layout.xml`
- **背景样式**：`app/src/main/res/drawable/toast_background.xml`
- **字符串资源**：`app/src/main/res/values/strings.xml`
- **中文字符串**：`app/src/main/res/values-zh/strings.xml`

## 总结

---

通过遵循本指南，您可以：

1. 确保所有 Toast 消息具有一致的自定义样式
2. 正确使用 `ToastHelper` 工具类
3. 支持多语言国际化
4. 维护良好的用户体验
5. 遵循项目的开发规范

记住：**始终使用 `ToastHelper.showShort()` 方法，而不是直接使用 `Toast.makeText()`**。这样可以确保所有 Toast 消息都应用了项目的自定义样式规范。 
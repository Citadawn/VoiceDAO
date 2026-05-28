package com.citadawn.speechapp.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.citadawn.speechapp.R;

/**
 * Toast 显示工具类
 * 提供简化的 Toast 显示方法，统一管理 Toast 显示逻辑
 * 使用自定义布局，实现圆角矩形、白底黑字、阴影和内边距效果
 */
public class ToastHelper {
    @Nullable
    private static Toast sLastToast;
    @Nullable
    private static TextView sLastToastTextView;

    // region 静态工具方法

    /**
     * 取消当前正在显示或排队中的 Toast。
     */
    public static void cancel() {
        if (sLastToast != null) {
            sLastToast.cancel();
            sLastToast = null;
            sLastToastTextView = null;
        }
    }

    /**
     * 若当前已有本工具显示的 Toast，则仅更新文案并重新展示；否则新建 Toast。
     * 用于需要随操作连续刷新提示的场景。
     */
    public static void showOrUpdateShort(@NonNull Context context, @NonNull String message) {
        if (sLastToast != null && sLastToastTextView != null) {
            if (!message.contentEquals(sLastToastTextView.getText())) {
                sLastToastTextView.setText(message);
            }
            sLastToast.show();
            return;
        }
        showCustomToast(context, message);
    }

    /**
     * 显示短时间 Toast 消息
     *
     * @param context      上下文
     * @param messageResId 消息资源ID
     */
    public static void showShort(@NonNull Context context, int messageResId) {
        showCustomToast(context, context.getString(messageResId));
    }

    /**
     * 显示短时间 Toast 消息（带格式化参数）
     *
     * @param context      上下文
     * @param messageResId 消息资源ID
     * @param formatArgs   格式化参数
     */
    public static void showShort(@NonNull Context context, int messageResId, Object... formatArgs) {
        showCustomToast(context, context.getString(messageResId, formatArgs));
    }

    /**
     * 显示短时间 Toast 消息（字符串）
     *
     * @param context 上下文
     * @param message 消息文本
     */
    public static void showShort(@NonNull Context context, String message) {
        showCustomToast(context, message);
    }

    /**
     * 显示自定义样式的 Toast
     *
     * @param context 上下文
     * @param message 消息文本
     */
    private static void showCustomToast(@NonNull Context context, String message) {
        cancel();
        Toast toast = new Toast(context);

        // 设置显示时长为短时间
        toast.setDuration(Toast.LENGTH_SHORT);

        // 创建自定义视图，使用 FrameLayout 作为临时父视图来正确解析布局参数
        ViewGroup parent = new android.widget.FrameLayout(context);
        View toastView = LayoutInflater.from(context).inflate(R.layout.custom_toast_layout, parent, false);
        TextView textView = toastView.findViewById(R.id.toast_text);
        textView.setText(message);

        // 设置自定义视图
        toast.setView(toastView);

        sLastToast = toast;
        sLastToastTextView = textView;
        toast.show();
    }

    // endregion
} 
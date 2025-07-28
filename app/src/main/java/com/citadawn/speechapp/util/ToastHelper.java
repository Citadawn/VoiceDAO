package com.citadawn.speechapp.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast 显示工具类
 * 提供简化的 Toast 显示方法，统一管理 Toast 显示逻辑
 */
public class ToastHelper {
    // region 静态工具方法
    
    /**
     * 显示短时间 Toast 消息
     * @param context 上下文
     * @param messageResId 消息资源ID
     */
    public static void showShort(Context context, int messageResId) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 显示短时间 Toast 消息（带格式化参数）
     * @param context 上下文
     * @param messageResId 消息资源ID
     * @param formatArgs 格式化参数
     */
    public static void showShort(Context context, int messageResId, Object... formatArgs) {
        Toast.makeText(context, context.getString(messageResId, formatArgs), Toast.LENGTH_SHORT).show();
    }

    // endregion
} 
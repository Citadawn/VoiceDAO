package com.citadawn.speechapp.util;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * 延迟任务管理工具类
 * 提供安全的延迟任务执行，避免内存泄漏
 */
public class DelayedTaskHelper {
    // region 静态工具方法

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * 安全地延迟执行任务
     *
     * @param view        关联的View（用于防止内存泄漏）
     * @param delayMillis 延迟时间（毫秒）
     * @param task        要执行的任务
     */
    public static void postDelayed(View view, long delayMillis, Runnable task) {
        WeakReference<View> viewRef = new WeakReference<>(view);
        mainHandler.postDelayed(() -> {
            View v = viewRef.get();
            if (v != null && v.getWindowToken() != null) {
                task.run();
            }
        }, delayMillis);
    }

    /**
     * 延迟清除文本视图内容
     *
     * @param textView    要清除的文本视图
     * @param delayMillis 延迟时间（毫秒）
     */
    public static void clearTextDelayed(android.widget.TextView textView, long delayMillis) {
        postDelayed(textView, delayMillis, () -> textView.setText(""));
    }

    // endregion
} 
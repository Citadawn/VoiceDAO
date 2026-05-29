package com.citadawn.speechapp.util;

import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.ViewCompat;

import com.citadawn.speechapp.R;

/**
 * 清空按钮辅助工具类
 * 提供清空按钮的双击确认功能和智能文字缩放
 */
public class ClearButtonHelper {
    // region 静态工具方法

    /**
     * 设置清空按钮的双击确认功能
     * 第一次点击显示"确认清空"并变红色，第二次点击才真正清空内容
     *
     * @param btnClear 清空按钮
     * @param editText 要清空的编辑框
     */
    public static void setupClearButton(@NonNull final Button btnClear, @NonNull final EditText editText) {
        final long[] lastClickTime = {0};

        // 为清空按钮设置自动文本大小调整
        ButtonTextHelper.setupAutoTextSize(btnClear);

        btnClear.setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            if (now - lastClickTime[0] < Constants.CLEAR_BUTTON_DOUBLE_CLICK_DELAY) { // 1500ms内双击
                editText.setText("");
                ButtonTextHelper.setTextWithAutoSize(btnClear, R.string.clear);
                applyButtonBackground(btnClear, R.drawable.btn_main_bg);
            } else {
                ButtonTextHelper.setTextWithAutoSize(btnClear, R.string.clear_again);
                applyButtonBackground(btnClear, R.drawable.btn_warning_bg);
                DelayedTaskHelper.postDelayed(btnClear, Constants.CLEAR_BUTTON_TEXT_RESTORE_DELAY, () -> {
                    ButtonTextHelper.setTextWithAutoSize(btnClear, R.string.clear);
                    applyButtonBackground(btnClear, R.drawable.btn_main_bg);
                });
            }
            lastClickTime[0] = now;
        });
    }

    /**
     * 设置按钮背景；清除 Material/AppCompat 的 backgroundTint，避免警告色 drawable 不生效。
     */
    private static void applyButtonBackground(@NonNull Button button, int drawableResId) {
        if (button instanceof AppCompatButton) {
            ((AppCompatButton) button).setSupportBackgroundTintList(null);
        } else {
            ViewCompat.setBackgroundTintList(button, null);
        }
        button.setBackgroundResource(drawableResId);
    }

    // endregion
}
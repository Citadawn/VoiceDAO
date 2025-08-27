package com.citadawn.speechapp.util;

import android.widget.Button;
import android.widget.EditText;

import com.citadawn.speechapp.R;

/**
 * 清空按钮辅助工具类
 * 提供清空按钮的双击确认功能和智能文字缩放
 */
public class ClearButtonHelper {
    // region 静态工具方法

    /**
     * 设置清空按钮的双击确认功能
     * 第一次点击显示"再次点击清空"，第二次点击才真正清空内容
     *
     * @param btnClear 清空按钮
     * @param editText 要清空的编辑框
     */
    public static void setupClearButton(final Button btnClear, final EditText editText) {
        final long[] lastClickTime = {0};

        // 为清空按钮设置自动文本大小调整
        ButtonTextHelper.setupAutoTextSize(btnClear);

        btnClear.setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            if (now - lastClickTime[0] < Constants.CLEAR_BUTTON_DOUBLE_CLICK_DELAY) { // 1500ms内双击
                editText.setText("");
                ButtonTextHelper.setTextWithAutoSize(btnClear, R.string.clear);
            } else {
                ButtonTextHelper.setTextWithAutoSize(btnClear, R.string.clear_again);
                DelayedTaskHelper.postDelayed(btnClear, Constants.CLEAR_BUTTON_TEXT_RESTORE_DELAY, () -> ButtonTextHelper.setTextWithAutoSize(btnClear, R.string.clear));
            }
            lastClickTime[0] = now;
        });
    }
    // endregion
}
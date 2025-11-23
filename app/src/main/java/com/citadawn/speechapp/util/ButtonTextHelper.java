package com.citadawn.speechapp.util;

import android.graphics.Paint;
import android.util.TypedValue;
import android.widget.Button;

/**
 * 按钮文本自动调整工具类
 * 提供按钮文本大小自动调整功能，确保文本完全显示在按钮内
 */
public class ButtonTextHelper {
    // region 静态工具方法

    /**
     * 为按钮设置自动文本大小调整
     * 当按钮文本变化时自动调整文本大小
     *
     * @param button 要设置自动调整的按钮
     */
    public static void setupAutoTextSize(Button button) {
        if (button == null) return;
        // 保存原始文本大小
        final float originalTextSize = button.getTextSize();

        // 设置文本变化监听器
        button.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    adjustTextSize(button, s.toString(), originalTextSize);
                } else {
                    // 如果文本为空，恢复原始大小
                    button.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        // 初始化时也调整一次
        if (button.getText().length() > 0) {
            adjustTextSize(button, button.getText().toString(), originalTextSize);
        }
    }

    /**
     * 调整按钮文本大小
     * 根据按钮宽度和文本长度自动调整字号，确保文本完全显示
     *
     * @param button           要调整的按钮
     * @param text             要显示的文本
     * @param originalTextSize 原始文本大小
     */
    public static void adjustTextSize(Button button, String text, float originalTextSize) {
        button.post(() -> {
            int buttonWidth = button.getWidth() - button.getPaddingLeft() - button.getPaddingRight();
            if (buttonWidth <= 0) {
                // 如果按钮宽度还未确定，使用原始大小
                button.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize);
                return;
            }

            float trySize = originalTextSize;
            Paint paint = new Paint();
            paint.set(button.getPaint());
            // 最小字体大小为 8sp
            float minSize = 8f * button.getContext().getResources().getDisplayMetrics().scaledDensity;

            paint.setTextSize(trySize);
            while (paint.measureText(text) > buttonWidth && trySize > minSize) {
                trySize -= 1f;
                paint.setTextSize(trySize);
            }

            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
        });
    }

    /**
     * 设置按钮文本并自动调整大小
     *
     * @param button 要设置的按钮
     * @param text   要设置的文本
     */
    public static void setTextWithAutoSize(Button button, String text) {
        button.setText(text);
        // 文本变化监听器会自动调整大小
    }

    /**
     * 设置按钮文本并自动调整大小（使用资源ID）
     *
     * @param button    要设置的按钮
     * @param textResId 文本资源ID
     */
    public static void setTextWithAutoSize(Button button, int textResId) {
        setTextWithAutoSize(button, button.getContext().getString(textResId));
    }

    // endregion
} 
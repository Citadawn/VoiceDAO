package com.citadawn.speechapp;

import android.widget.Button;
import android.widget.EditText;
import android.util.TypedValue;
import android.graphics.Paint;
import android.view.View;

public class ClearButtonHelper {
    public static void setupClearButton(final Button btnClear, final EditText editText) {
        final long[] lastClickTime = {0};
        final float btnClearOriginalTextSize = btnClear.getTextSize();

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                if (now - lastClickTime[0] < 1500) { // 1500ms内双击
                    editText.setText("");
                    btnClear.setText("清空");
                    btnClear.setTextSize(TypedValue.COMPLEX_UNIT_PX, btnClearOriginalTextSize);
                } else {
                    btnClear.setText("再次点击清空");
                    setSmartTextSize(btnClear, "再次点击清空");
                    btnClear.postDelayed(() -> {
                        btnClear.setText("清空");
                        btnClear.setTextSize(TypedValue.COMPLEX_UNIT_PX, btnClearOriginalTextSize);
                    }, 1500);
                }
                lastClickTime[0] = now;
            }
        });
    }

    // 智能缩放按钮文字字号
    public static void setSmartTextSize(final Button button, final String text) {
        button.post(() -> {
            int buttonWidth = button.getWidth() - button.getPaddingLeft() - button.getPaddingRight();
            float trySize = 16f; // 初始字号
            Paint paint = new Paint();
            paint.set(button.getPaint());
            float minSize = 8f;
            paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, trySize, button.getResources().getDisplayMetrics()));
            while (paint.measureText(text) > buttonWidth && trySize > minSize) {
                trySize -= 1f;
                paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, trySize, button.getResources().getDisplayMetrics()));
            }
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, trySize);
        });
    }
} 
package com.citadawn.speechapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.appcompat.widget.AppCompatEditText;

public class ScrollableEditText extends AppCompatEditText {
    public ScrollableEditText(Context context) {
        super(context);
    }
    public ScrollableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ScrollableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 让EditText在内容超出时优先消费滑动事件
        if (canScrollVertically(1) || canScrollVertically(-1)) {
            getParent().requestDisallowInterceptTouchEvent(true);
            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                getParent().requestDisallowInterceptTouchEvent(false);
            }
        }
        return super.onTouchEvent(event);
    }
} 
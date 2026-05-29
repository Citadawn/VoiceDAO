package com.citadawn.speechapp.ui;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * 置于 {@link androidx.core.widget.NestedScrollView} 内的多行输入框。
 * <ul>
 *   <li>手指在框内滑动时，整段手势仅滚动输入框，不交给外层页面。</li>
 *   <li>内容未溢出时，手势交给外层 {@link androidx.core.widget.NestedScrollView}。</li>
 *   <li>用户手动拖滚浏览时，抑制 {@link #bringPointIntoView} 等「跟光标」导致的视图回弹。</li>
 * </ul>
 */
public class ManualScrollEditText extends AppCompatEditText {

    private final int touchSlop;
    private float gestureStartY;
    private boolean userScrolling;
    private boolean suppressAutoScrollToCursor;

    public ManualScrollEditText(@NonNull Context context) {
        this(context, null);
    }

    public ManualScrollEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.editTextStyle);
    }

    public ManualScrollEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setVerticalScrollBarEnabled(true);
        setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
        applyJustifiedAlignment();
    }

    private void applyJustifiedAlignment() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_CHARACTER);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }
    }

    public boolean isUserScrolling() {
        return userScrolling;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        userScrolling = false;
        suppressAutoScrollToCursor = false;
    }

    @Override
    public boolean bringPointIntoView(int offset) {
        if (suppressAutoScrollToCursor) {
            return true;
        }
        return super.bringPointIntoView(offset);
    }

    @Override
    public boolean requestRectangleOnScreen(Rect rectangle, boolean immediate) {
        if (suppressAutoScrollToCursor) {
            return true;
        }
        return super.requestRectangleOnScreen(rectangle, immediate);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                gestureStartY = event.getY();
                userScrolling = false;
                suppressAutoScrollToCursor = false;
                // 内容可溢出时先阻止父级 NestedScrollView 抢手势，否则框内无法起滑
                updateParentDisallowIntercept(hasScrollableContent());
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = event.getY() - gestureStartY;
                if (Math.abs(deltaY) > touchSlop) {
                    userScrolling = true;
                    suppressAutoScrollToCursor = true;
                }
                if (hasScrollableContent()) {
                    updateParentDisallowIntercept(true);
                } else {
                    updateParentDisallowIntercept(false);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                updateParentDisallowIntercept(false);
                if (!userScrolling) {
                    suppressAutoScrollToCursor = false;
                }
                userScrolling = false;
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void updateParentDisallowIntercept(boolean disallow) {
        ViewParent parent = getParent();
        while (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
            parent = parent.getParent();
        }
    }

    private boolean hasScrollableContent() {
        Layout layout = getLayout();
        if (layout == null) {
            return false;
        }
        int innerHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        return innerHeight > 0 && layout.getHeight() > innerHeight;
    }
}

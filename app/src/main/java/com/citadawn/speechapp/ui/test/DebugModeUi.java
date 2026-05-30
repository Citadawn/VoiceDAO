package com.citadawn.speechapp.ui.test;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.citadawn.speechapp.R;

/**
 * 调试模式：Toolbar 标题连点反馈、强调色读取、溢出菜单「退出调试模式」样式。
 * 静态控件配色在布局 XML 中声明（{@link R.color#accent_warning}、{@link R.drawable#btn_debug_mode_bg} 等）。
 */
public final class DebugModeUi {

    private static final long TITLE_TAP_FEEDBACK_MS = 340L;

    private DebugModeUi() {
    }

    /**
     * 连点 Toolbar 标题时的左右轻摆 + 旋转回弹，并触发短触觉反馈。
     */
    public static void playTitleTapFeedback(@NonNull View target) {
        playTitleTapFeedback(target, null);
    }

    public static void playTitleTapFeedback(@NonNull View target, @Nullable Runnable onRestore) {
        target.animate().cancel();
        ObjectAnimator prior = (ObjectAnimator) target.getTag(R.id.debug_mode_title_tap_animator);
        if (prior != null) {
            prior.cancel();
        }
        resetTitleTapAnimationState(target);

        float dx = 5f * target.getResources().getDisplayMetrics().density;
        PropertyValuesHolder translationX = PropertyValuesHolder.ofFloat(
                View.TRANSLATION_X, 0f, -dx, dx, -dx * 0.6f, dx * 0.6f, 0f);
        PropertyValuesHolder rotation = PropertyValuesHolder.ofFloat(
                View.ROTATION, 0f, -8f, 8f, -5f, 5f, -2f, 2f, 0f);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(target, translationX, rotation);
        animator.setDuration(TITLE_TAP_FEEDBACK_MS);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                finishTitleTapFeedback(target, animation, onRestore);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
                finishTitleTapFeedback(target, animation, onRestore);
            }
        });
        target.setTag(R.id.debug_mode_title_tap_animator, animator);
        animator.start();
        target.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
    }

    private static void resetTitleTapAnimationState(@NonNull View target) {
        target.setTranslationX(0f);
        target.setRotation(0f);
    }

    private static void finishTitleTapFeedback(@NonNull View target, @NonNull Animator animation,
            @Nullable Runnable onRestore) {
        if (animation == target.getTag(R.id.debug_mode_title_tap_animator)) {
            target.setTag(R.id.debug_mode_title_tap_animator, null);
        }
        resetTitleTapAnimationState(target);
        if (onRestore != null) {
            onRestore.run();
        }
    }

    public static int accentColor(@NonNull Context context) {
        return ContextCompat.getColor(context, R.color.accent_warning);
    }

    /**
     * 为溢出菜单中的「退出调试模式」项设置调试强调色标题。
     */
    public static void applyExitDebugModeMenuItemStyle(@NonNull Context context, @NonNull MenuItem item) {
        CharSequence label = context.getString(R.string.test_mode_exit);
        SpannableString spannable = new SpannableString(label);
        spannable.setSpan(new ForegroundColorSpan(accentColor(context)), 0, spannable.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        item.setTitle(spannable);
    }
}

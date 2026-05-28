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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.citadawn.speechapp.R;

/**
 * 调试模式相关控件的统一配色（{@link R.color#debug_mode_accent}）与标题连点反馈。
 */
public final class DebugModeUi {

    private static final long TITLE_TAP_FEEDBACK_MS = 300L;

    private DebugModeUi() {
    }

    /**
     * 连点 Toolbar 标题时的左右轻摆 + 轻微旋转，并触发短触觉反馈。
     */
    public static void playTitleTapFeedback(@NonNull View target) {
        target.animate().cancel();
        ObjectAnimator prior = (ObjectAnimator) target.getTag(R.id.debug_mode_title_tap_animator);
        if (prior != null) {
            prior.cancel();
        }
        target.setTranslationX(0f);
        target.setRotation(0f);

        float dx = 5f * target.getResources().getDisplayMetrics().density;
        PropertyValuesHolder translationX = PropertyValuesHolder.ofFloat(
                View.TRANSLATION_X, 0f, -dx, dx, -dx * 0.55f, dx * 0.55f, 0f);
        PropertyValuesHolder rotation = PropertyValuesHolder.ofFloat(
                View.ROTATION, 0f, -3f, 3f, -1.5f, 1.5f, 0f);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(target, translationX, rotation);
        animator.setDuration(TITLE_TAP_FEEDBACK_MS);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                resetTitleTapAnimationState(target, animation);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
                resetTitleTapAnimationState(target, animation);
            }
        });
        target.setTag(R.id.debug_mode_title_tap_animator, animator);
        animator.start();
        target.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
    }

    private static void resetTitleTapAnimationState(@NonNull View target, @NonNull Animator animation) {
        if (animation == target.getTag(R.id.debug_mode_title_tap_animator)) {
            target.setTag(R.id.debug_mode_title_tap_animator, null);
        }
        target.setTranslationX(0f);
        target.setRotation(0f);
    }

    public static int accentColor(@NonNull Context context) {
        return ContextCompat.getColor(context, R.color.debug_mode_accent);
    }

    public static void applyAccentText(@Nullable TextView... textViews) {
        if (textViews == null) {
            return;
        }
        for (TextView textView : textViews) {
            if (textView != null) {
                textView.setTextColor(accentColor(textView.getContext()));
            }
        }
    }

    public static void styleDebugButton(@NonNull Button button) {
        button.setBackgroundResource(R.drawable.btn_debug_mode_bg);
        button.setTextColor(ContextCompat.getColor(button.getContext(), R.color.white));
    }

    public static void styleDebugPanelPrimaryButton(@NonNull Button button) {
        styleDebugButton(button);
    }

    public static void tintCheckbox(@NonNull CheckBox checkBox) {
        checkBox.setButtonTintList(ContextCompat.getColorStateList(checkBox.getContext(),
                R.color.debug_mode_checkbox_tint));
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

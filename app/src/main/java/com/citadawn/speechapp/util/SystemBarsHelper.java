package com.citadawn.speechapp.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.citadawn.speechapp.R;

/**
 * 系统栏（状态栏、导航栏）与 edge-to-edge 布局的统一入口。
 * <p>
 * Activity：{@link #enable(ComponentActivity)} + {@link #applyToolbarTopInsets(Toolbar)}
 * + {@link #applyContentMarginInsets(View)}。
 * 全屏蒙层 Dialog：{@link #applySolidStatusBarForOverlay(Window, Context)}。
 */
public final class SystemBarsHelper {

    private SystemBarsHelper() {
    }

    /**
     * 启用 edge-to-edge，并将状态栏图标设为浅色（适配深色 Toolbar）。
     */
    public static void enable(@NonNull ComponentActivity activity) {
        EdgeToEdge.enable(activity);
        applyDarkToolbarAppearance(activity.getWindow());
    }

    /**
     * 从子窗口返回或系统栏被覆盖后，恢复 Activity 的透明状态栏与图标样式。
     */
    public static void reapply(@NonNull ComponentActivity activity) {
        applyDarkToolbarAppearance(activity.getWindow());
    }

    /**
     * Toolbar 背景延伸至状态栏区域，内容区通过 top padding 避开系统图标。
     */
    public static void applyToolbarTopInsets(@NonNull Toolbar toolbar) {
        final int initialLeft = toolbar.getPaddingLeft();
        final int initialRight = toolbar.getPaddingRight();
        final int initialBottom = toolbar.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
            Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(initialLeft, statusBars.top, initialRight, initialBottom);
            v.post(() -> alignToolbarContent(toolbar));
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(toolbar);
        toolbar.post(() -> alignToolbarContent(toolbar));
    }

    /**
     * 标题与 ⋮ 等菜单项在 Toolbar 内容区垂直居中对齐（大字号标题需关闭 includeFontPadding）。
     */
    public static void alignToolbarContent(@NonNull Toolbar toolbar) {
        TextView titleView = toolbar.findViewById(androidx.appcompat.R.id.action_bar_title);
        if (titleView != null) {
            titleView.setIncludeFontPadding(false);
            ViewGroup.LayoutParams params = titleView.getLayoutParams();
            if (params instanceof Toolbar.LayoutParams toolbarParams) {
                toolbarParams.gravity = Gravity.CENTER_VERTICAL;
                titleView.setLayoutParams(toolbarParams);
            } else if (params instanceof androidx.appcompat.app.ActionBar.LayoutParams actionBarParams) {
                actionBarParams.gravity = Gravity.CENTER_VERTICAL;
                titleView.setLayoutParams(actionBarParams);
            }
        }
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof ActionMenuView menuView) {
                ViewGroup.LayoutParams params = menuView.getLayoutParams();
                if (params instanceof Toolbar.LayoutParams toolbarParams) {
                    toolbarParams.gravity = Gravity.CENTER_VERTICAL;
                    menuView.setLayoutParams(toolbarParams);
                }
                break;
            }
        }
    }

    /**
     * 为根内容容器应用左右与底部系统栏边距（顶部由 Toolbar inset 处理）。
     */
    public static void applyContentMarginInsets(@NonNull View contentRoot) {
        ViewCompat.setOnApplyWindowInsetsListener(contentRoot, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(contentRoot);
    }

    /**
     * 全屏蒙层 Dialog：状态栏使用与 Toolbar 相同的实色，避免透明蒙层盖住 Activity 状态栏配色。
     */
    public static void applySolidStatusBarForOverlay(@NonNull Window window, @NonNull Context context) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(context, R.color.main_dark));
        applyDarkToolbarAppearance(window);
    }

    private static void applyDarkToolbarAppearance(@Nullable Window window) {
        if (window == null) {
            return;
        }
        window.setStatusBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        } else {
            View decorView = window.getDecorView();
            int flags = decorView.getSystemUiVisibility();
            decorView.setSystemUiVisibility(flags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
}

package com.citadawn.speechapp.util;

import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;

/**
 * 状态栏辅助工具类
 * 用于设置状态栏背景色和文字颜色
 */
public class StatusBarHelper {
    
    // region 静态工具方法
    
    /**
     * 设置状态栏背景色和文字颜色
     * 将状态栏文字颜色设置为黑色（适配浅色背景）
     * 
     * @param window Activity的Window对象
     */
    public static void setupStatusBar(Window window) {
        if (window == null) {
            return;
        }
        
        // 设置状态栏文字为黑色（浅色状态栏）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) 及以上使用 WindowInsetsController
            WindowInsetsController insetsController = window.getInsetsController();
            if (insetsController != null) {
                // 设置状态栏文字为黑色（适配浅色背景）
                insetsController.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        } else {
            // Android 10 (API 29) 及以下使用 SystemUiVisibility
            View decorView = window.getDecorView();
            int flags = decorView.getSystemUiVisibility();
            // 设置浅色状态栏标志，强制状态栏文字为黑色
            decorView.setSystemUiVisibility(flags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
    
    /**
     * 强制设置状态栏颜色
     * 用于某些系统版本（如Android 15）可能需要重新设置的情况
     * 
     * @param window Activity的Window对象
     */
    public static void forceStatusBarColor(Window window) {
        if (window == null) {
            return;
        }
        
        // 重新应用状态栏设置
        setupStatusBar(window);
    }
    
    // endregion
}
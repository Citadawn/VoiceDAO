package com.citadawn.speechapp.util;

import android.widget.SeekBar;

/**
 * SeekBar 工具类
 * 提供简化的 SeekBar 监听器设置方法，统一管理 SeekBar 交互逻辑
 */
public class SeekBarHelper {
    // region 静态工具方法

    /**
     * 设置 SeekBar 监听器（简化版本）
     *
     * @param seekBar              要设置监听器的 SeekBar
     * @param onProgressChanged    进度变化回调
     * @param onStartTrackingTouch 开始触摸回调
     * @param onStopTrackingTouch  停止触摸回调
     */
    public static void setSeekBarListener(SeekBar seekBar,
                                          OnProgressChangedListener onProgressChanged,
                                          OnTrackingTouchListener onStartTrackingTouch,
                                          OnTrackingTouchListener onStopTrackingTouch) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (onProgressChanged != null) {
                    onProgressChanged.onProgressChanged(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (onStartTrackingTouch != null) {
                    onStartTrackingTouch.onTrackingTouch(seekBar);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (onStopTrackingTouch != null) {
                    onStopTrackingTouch.onTrackingTouch(seekBar);
                }
            }
        });
    }

    /**
     * 设置 SeekBar 监听器（仅进度变化）
     *
     * @param seekBar           要设置监听器的 SeekBar
     * @param onProgressChanged 进度变化回调
     */
    public static void setSeekBarListener(SeekBar seekBar, OnProgressChangedListener onProgressChanged) {
        setSeekBarListener(seekBar, onProgressChanged, null, null);
    }

    /**
     * 进度变化监听器接口
     */
    public interface OnProgressChangedListener {
        void onProgressChanged(int progress);
    }

    /**
     * 触摸跟踪监听器接口
     */
    public interface OnTrackingTouchListener {
        void onTrackingTouch(SeekBar seekBar);
    }

    // endregion
} 
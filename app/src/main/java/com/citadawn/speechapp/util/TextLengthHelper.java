package com.citadawn.speechapp.util;

import android.speech.tts.TextToSpeech;

/**
 * 文本长度检查工具类
 * 提供统一的文本长度验证功能，使用 TTS API 动态获取最大字数限制
 */
public class TextLengthHelper {
    // region 静态工具方法

    /**
     * 获取 TTS 引擎支持的最大文本长度
     *
     * @return 最大文本长度（字符数）
     */
    public static int getMaxTextLength() {
        return TextToSpeech.getMaxSpeechInputLength();
    }

    /**
     * 检查文本是否超过最大长度限制
     *
     * @param text 要检查的文本
     * @return true 如果文本超过限制，false 否则
     */
    public static boolean isTextTooLong(String text) {
        if (text == null) return false;
        return text.length() > getMaxTextLength();
    }

    // endregion
} 
package com.citadawn.speechapp.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import java.util.List;

/**
 * TTS引擎信息处理工具类
 * 提供引擎信息获取、图标加载、默认引擎处理等功能
 */
public class TtsEngineHelper {
    
    private static final String TAG = "TtsEngineHelper";
    
    /**
     * 获取TTS引擎信息（包含名称和图标）
     * 
     * @param tts TTS实例
     * @param context 上下文
     * @param iconView 图标视图（可为null）
     * @return 引擎显示名称
     */
    public static String getTtsEngineInfo(TextToSpeech tts, Context context, android.widget.ImageView iconView) {
        if (tts == null) {
            if (iconView != null) {
                iconView.setVisibility(android.view.View.GONE);
            }
            return context.getString(com.citadawn.speechapp.R.string.tts_engine_unknown);
        }

        try {
            String engineName = tts.getDefaultEngine();
            if (engineName == null || engineName.isEmpty()) {
                if (iconView != null) {
                    iconView.setVisibility(android.view.View.GONE);
                }
                return context.getString(com.citadawn.speechapp.R.string.tts_engine_unknown);
            }

            String displayName = engineName;
            int iconResId = 0;
            List<TextToSpeech.EngineInfo> engines = tts.getEngines();

            for (TextToSpeech.EngineInfo engine : engines) {
                if (engineName.equals(engine.name)) {
                    // 使用当前界面语言获取引擎显示名称
                    displayName = TtsLanguageVoiceHelper.getLocalizedEngineName(engine.label, context);
                    iconResId = engine.icon;
                    break;
                }
            }

            // 加载引擎图标
            if (iconView != null && iconResId != 0) {
                loadEngineIcon(engineName, iconResId, iconView, context);
            }

            return displayName;

        } catch (Exception e) {
            if (iconView != null) {
                iconView.setVisibility(android.view.View.GONE);
            }
            Log.e(TAG, "获取TTS引擎信息失败", e);
            return context.getString(com.citadawn.speechapp.R.string.tts_engine_unknown);
        }
    }

    /**
     * 加载引擎图标
     * 
     * @param engineName 引擎包名
     * @param iconResId 图标资源ID
     * @param iconView 图标视图
     * @param context 上下文
     */
    private static void loadEngineIcon(String engineName, int iconResId, android.widget.ImageView iconView, Context context) {
        try {
            // 跨包加载图标
            Context engineContext = context.createPackageContext(engineName, 0);
            Drawable icon = ResourcesCompat.getDrawable(engineContext.getResources(), iconResId, engineContext.getTheme());
            iconView.setImageDrawable(icon);
            iconView.setVisibility(android.view.View.VISIBLE);
        } catch (Exception e) {
            iconView.setVisibility(android.view.View.GONE);
            Log.d(TAG, "无法加载引擎图标: " + engineName);
        }
    }
    
    /**
     * 获取默认引擎名称
     * 
     * @param tts TTS实例
     * @return 默认引擎包名，如果获取失败返回null
     */
    public static String getDefaultEngineName(TextToSpeech tts) {
        if (tts == null) {
            return null;
        }
        
        try {
            return tts.getDefaultEngine();
        } catch (Exception e) {
            Log.e(TAG, "获取默认引擎失败", e);
            return null;
        }
    }
    
    /**
     * 获取所有引擎并排序
     * 
     * @param tts TTS实例
     * @return 排序后的引擎列表，如果获取失败返回空列表
     */
    public static List<TextToSpeech.EngineInfo> getSortedEngines(TextToSpeech tts) {
        if (tts == null) {
            return new java.util.ArrayList<>();
        }
        
        try {
            List<TextToSpeech.EngineInfo> engines = tts.getEngines();
            String defaultEngine = getDefaultEngineName(tts);
            return TtsLanguageVoiceHelper.sortEnginesByDefault(engines, defaultEngine);
        } catch (Exception e) {
            Log.e(TAG, "获取排序引擎列表失败", e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 获取默认语言
     * 
     * @param tts TTS实例
     * @return 默认语言Locale，如果获取失败返回null
     */
    public static java.util.Locale getDefaultLanguage(TextToSpeech tts) {
        if (tts == null) {
            return null;
        }
        
        try {
            android.speech.tts.Voice defaultVoice = tts.getDefaultVoice();
            if (defaultVoice != null) {
                return defaultVoice.getLocale();
            }
        } catch (Exception e) {
            Log.e(TAG, "获取默认语言失败", e);
        }
        
        return null;
    }
    
    /**
     * 获取可用语言和发音人
     * 
     * @param tts TTS实例
     * @return 包含语言和发音人的数组，如果获取失败返回null
     */
    public static Object[] getAvailableLanguagesAndVoices(TextToSpeech tts) {
        if (tts == null) {
            return null;
        }
        
        try {
            java.util.Set<java.util.Locale> languages = tts.getAvailableLanguages();
            java.util.Set<android.speech.tts.Voice> voices = tts.getVoices();
            return new Object[]{languages, voices};
        } catch (Exception e) {
            Log.e(TAG, "获取可用语言和发音人失败", e);
        }
        
        return null;
    }
} 
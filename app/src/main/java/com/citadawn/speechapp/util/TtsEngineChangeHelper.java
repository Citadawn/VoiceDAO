package com.citadawn.speechapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.util.Log;

/**
 * TTS引擎变化检测助手工具类
 * 用于检测系统TTS引擎是否发生变化，并提供相应处理方法
 */
public class TtsEngineChangeHelper {
    
    // region 静态常量
    
    private static final String TAG = "TtsEngineChangeHelper";
    private static final String PREFS_NAME = Constants.PREFS_NAME;
    private static final String KEY_LAST_TTS_ENGINE = "last_tts_engine";
    
    // endregion
    
    // region 静态工具方法
    
    /**
     * 检测TTS引擎是否发生变化
     * 
     * @param context 上下文
     * @param tts TTS实例，用于获取当前引擎信息
     * @return 如果引擎发生变化返回true，否则返回false
     */
    public static boolean hasEngineChanged(Context context, TextToSpeech tts) {
        if (tts == null) {
            Log.w(TAG, "TTS instance is null, cannot check engine change");
            return false;
        }
        
        String currentEngine = getCurrentTtsEngine(tts);
        String lastEngine = getLastTtsEngine(context);
        
        Log.d(TAG, "Current TTS engine: " + currentEngine);
        Log.d(TAG, "Last recorded TTS engine: " + lastEngine);
        
        // 如果是第一次检查（lastEngine为空），则记录当前引擎但不视为变化
        if (lastEngine == null || lastEngine.isEmpty()) {
            saveCurrentTtsEngine(context, currentEngine);
            return false;
        }
        
        // 检查引擎是否发生变化
        boolean hasChanged = !currentEngine.equals(lastEngine);
        
        if (hasChanged) {
            Log.i(TAG, "TTS engine changed from " + lastEngine + " to " + currentEngine);
            saveCurrentTtsEngine(context, currentEngine);
        }
        
        return hasChanged;
    }
    
    /**
     * 获取当前TTS引擎信息
     * 
     * @param tts TTS实例
     * @return 当前TTS引擎的包名，如果获取失败返回"unknown"
     */
    public static String getCurrentTtsEngine(TextToSpeech tts) {
        if (tts == null) {
            return "unknown";
        }
        
        try {
            String engine = tts.getDefaultEngine();
            return engine != null ? engine : "unknown";
        } catch (Exception e) {
            Log.e(TAG, "Failed to get current TTS engine", e);
            return "unknown";
        }
    }
    
    /**
     * 保存当前TTS引擎信息到SharedPreferences
     * 
     * @param context 上下文
     * @param engine 当前TTS引擎包名
     */
    public static void saveCurrentTtsEngine(Context context, String engine) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_LAST_TTS_ENGINE, engine).apply();
            Log.d(TAG, "Saved TTS engine: " + engine);
        } catch (Exception e) {
            Log.e(TAG, "Failed to save TTS engine", e);
        }
    }
    
    /**
     * 从SharedPreferences获取上次记录的TTS引擎信息
     * 
     * @param context 上下文
     * @return 上次记录的TTS引擎包名，如果未记录返回null
     */
    public static String getLastTtsEngine(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getString(KEY_LAST_TTS_ENGINE, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get last TTS engine", e);
            return null;
        }
    }

    // endregion
    
    // region 私有辅助方法
    
    // 目前无需私有方法
    
    // endregion
}
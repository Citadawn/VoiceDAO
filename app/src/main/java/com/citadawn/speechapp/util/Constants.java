package com.citadawn.speechapp.util;

/**
 * 项目常量类
 * 统一管理项目中的魔法数字和常量值
 */
public class Constants {
    // region 文本长度相关常量

    // 已移除 MAX_TEXT_LENGTH 常量，改用 TextLengthHelper.getMaxTextLength() 动态获取

    // endregion

    // region 时间相关常量

    /** 清空按钮双击时间间隔（毫秒） */
    public static final long CLEAR_BUTTON_DOUBLE_CLICK_DELAY = 1500;
    
    /** 清空按钮文本恢复延迟（毫秒） */
    public static final long CLEAR_BUTTON_TEXT_RESTORE_DELAY = 1500;
    
    /** Toast 消息显示延迟（毫秒） */
    public static final long TOAST_MESSAGE_DELAY = 500;
    
    /** TTS 状态更新间隔（毫秒） */
    public static final long TTS_STATUS_UPDATE_INTERVAL = 300;

    // endregion

    // region 语速和音调相关常量

    /** 语速最小值 */
    public static final float SPEECH_RATE_MIN = 0.5f;
    
    /** 语速最大值 */
    public static final float SPEECH_RATE_MAX = 2.0f;
    
    /** 语速默认值 */
    public static final float SPEECH_RATE_DEFAULT = 1.0f;
    
    /** 语速步进值 */
    public static final float SPEECH_RATE_STEP = 0.01f;
    
    /** 音调最小值 */
    public static final float PITCH_MIN = 0.5f;
    
    /** 音调最大值 */
    public static final float PITCH_MAX = 2.0f;
    
    /** 音调默认值 */
    public static final float PITCH_DEFAULT = 1.0f;
    
    /** 音调步进值 */
    public static final float PITCH_STEP = 0.01f;

    // endregion

    // region SeekBar 相关常量

    /** SeekBar 最大值 */
    public static final int SEEKBAR_MAX = 15;
    
    /** SeekBar 默认进度值 */
    public static final int SEEKBAR_DEFAULT_PROGRESS = 5;

    // endregion

    // region 文件相关常量

    // endregion

    // region TTS 相关常量

    // endregion

    // region SharedPreferences 相关常量

    /** SharedPreferences 文件名 */
    public static final String PREFS_NAME = "tts_prefs";
    
    /** 保存目录 URI 键名 */
    public static final String KEY_SAVE_DIR_URI = "save_dir_uri";

    // endregion

    // region 私有构造函数防止实例化
    private Constants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
    // endregion
} 
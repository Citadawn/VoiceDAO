package com.citadawn.speechapp.util;

/**
 * 项目常量类
 * 统一管理项目中的魔法数字和常量值
 */
public class Constants {


    // region 时间相关常量

    /**
     * 清空按钮双击时间间隔（毫秒）
     */
    public static final long CLEAR_BUTTON_DOUBLE_CLICK_DELAY = 1500;

    /**
     * 清空按钮文本恢复延迟（毫秒）
     */
    public static final long CLEAR_BUTTON_TEXT_RESTORE_DELAY = 1500;

    /**
     * Toast 消息显示延迟（毫秒）
     */
    public static final long TOAST_MESSAGE_DELAY = 500;

    /**
     * TTS 状态更新间隔（毫秒）
     */
    public static final long TTS_STATUS_UPDATE_INTERVAL = 300;

    /**
     * 双击返回键退出应用的时间间隔（毫秒）
     */
    public static final long DOUBLE_BACK_EXIT_INTERVAL = 2000;

    // endregion

    // region 语速和音调相关常量

    /**
     * 语速最小值
     */
    public static final float SPEECH_RATE_MIN = 0.5f;

    /**
     * 语速最大值
     */
    public static final float SPEECH_RATE_MAX = 2.0f;

    /**
     * 语速默认值
     */
    public static final float SPEECH_RATE_DEFAULT = 1.0f;

    /**
     * 语速步进值
     */
    public static final float SPEECH_RATE_STEP = 0.01f;

    /**
     * 音调最小值
     */
    public static final float PITCH_MIN = 0.5f;

    /**
     * 音调最大值
     */
    public static final float PITCH_MAX = 2.0f;

    /**
     * 音调默认值
     */
    public static final float PITCH_DEFAULT = 1.0f;

    /**
     * 音调步进值
     */
    public static final float PITCH_STEP = 0.01f;

    // endregion

    // region SeekBar 相关常量

    /**
     * SeekBar 最大值
     */
    public static final int SEEKBAR_MAX = 15;

    /**
     * SeekBar 默认进度值
     */
    public static final int SEEKBAR_DEFAULT_PROGRESS = 5;

    /**
     * SeekBar 进度转换系数
     * 用于将语速/音调值（0.5-2.0）转换为 SeekBar 进度值（0-15）
     * 公式：progress = (value - 0.5) * 10
     */
    public static final int SEEKBAR_PROGRESS_MULTIPLIER = 10;

    // endregion

    // region 文件相关常量

    /**
     * 文件读取缓冲区大小（字节）
     * 用于文件复制等操作的缓冲区
     */
    public static final int FILE_BUFFER_SIZE = 4096;

    // endregion

    // region TTS 相关常量

    /**
     * 特性名称最大长度（全大写或全小写）
     * 超过此长度的全大写或全小写特性名称被视为无意义特性
     */
    public static final int MAX_FEATURE_NAME_LENGTH = 20;

    /**
     * 16进制字符串最大长度
     * 超过此长度的16进制字符串被视为无意义特性
     */
    public static final int MAX_HEX_STRING_LENGTH = 16;

    /**
     * UUID 最小长度
     * 用于识别UUID格式的特性字符串
     */
    public static final int UUID_MIN_LENGTH = 32;

    // endregion

    // region UI 相关常量

    /**
     * 滑动阈值（像素）
     * 用于判断用户是否在滑动，超过此阈值才认为是滑动操作
     */
    public static final float SCROLL_THRESHOLD = 15.0f;

    // endregion

    // region SharedPreferences 相关常量

    /**
     * SharedPreferences 文件名
     */
    public static final String PREFS_NAME = "tts_prefs";

    /**
     * 保存目录 URI 键名
     */
    public static final String KEY_SAVE_DIR_URI = "save_dir_uri";

    // endregion

    // region 私有构造函数防止实例化
    private Constants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
    // endregion
} 
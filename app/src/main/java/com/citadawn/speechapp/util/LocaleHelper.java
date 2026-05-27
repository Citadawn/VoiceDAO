package com.citadawn.speechapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

/**
 * 语言切换工具类
 * Language switching utility class
 */
public class LocaleHelper {
    public static final String KEY_LANGUAGE_MODE = "language_mode";
    public static final String KEY_SELECTED_LOCALE = "selected_locale";
    public static final int MODE_FOLLOW_SYSTEM = 0; // 跟随系统
    public static final int MODE_MANUAL = 1; // 手动选择
    public static final String LOCALE_ZH_CN = "zh-CN"; // 中文（简体）
    // region 常量与模式
    private static final String TAG = "LocaleHelper";
    // endregion

    // region 获取当前语言与模式

    /**
     * 获取当前应用的语言设置
     */
    public static Locale getCurrentLocale(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        int languageMode = prefs.getInt(KEY_LANGUAGE_MODE, MODE_FOLLOW_SYSTEM);
        if (languageMode == MODE_FOLLOW_SYSTEM) {
            return getSystemLocale();
        } else {
            String localeString = prefs.getString(KEY_SELECTED_LOCALE, LOCALE_ZH_CN);
            return parseLocaleString(localeString);
        }
    }

    /**
     * 获取当前语言模式
     */
    public static int getLanguageMode(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_LANGUAGE_MODE, MODE_FOLLOW_SYSTEM);
    }

    /**
     * 获取系统语言区域
     */
    public static Locale getSystemLocale() {
        return Resources.getSystem().getConfiguration().getLocales().get(0);
    }
    // endregion

    // region 设置语言与模式

    /**
     * 设置应用语言
     */
    public static void setLocale(@NonNull Context context, @NonNull Locale locale) {
        updateResources(context, locale);
    }

    /**
     * 设置语言模式
     */
    public static void setLanguageMode(@NonNull Context context, int mode, @Nullable Locale locale) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_LANGUAGE_MODE, mode);
        if (mode == MODE_MANUAL && locale != null) {
            editor.putString(KEY_SELECTED_LOCALE, localeToString(locale));
        }
        editor.apply();
        Locale targetLocale = (mode == MODE_FOLLOW_SYSTEM) ? getSystemLocale() : locale;
        setLocale(context, targetLocale);
    }
    // endregion

    // region 工具方法

    /**
     * 更新资源配置
     */
    private static void updateResources(@NonNull Context context, @NonNull Locale locale) {
        try {
            Resources resources = context.getResources();
            Configuration configuration = new Configuration(resources.getConfiguration());
            configuration.setLocales(new android.os.LocaleList(locale));
            context.createConfigurationContext(configuration);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            Log.d(TAG, "Language updated to: " + locale.getDisplayLanguage());
        } catch (Exception e) {
            Log.e(TAG, "Failed to update language", e);
        }
    }

    /**
     * 将Locale转换为字符串
     */
    @NonNull
    public static String localeToString(@Nullable Locale locale) {
        if (locale == null) {
            return LOCALE_ZH_CN;
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (country.isEmpty()) {
            return language;
        } else {
            return language + "-" + country;
        }
    }

    /**
     * 解析字符串为Locale
     */
    @NonNull
    public static Locale parseLocaleString(@Nullable String localeString) {
        if (localeString == null || localeString.isEmpty()) {
            return new Locale("zh", "CN");
        }
        String[] parts = localeString.split("-");
        if (parts.length >= 2) {
            return new Locale(parts[0], parts[1]);
        } else {
            return new Locale(parts[0]);
        }
    }

    /**
     * 检查是否为中文
     */
    public static boolean isChinese(@Nullable Locale locale) {
        return locale != null && "zh".equals(locale.getLanguage());
    }

    /**
     * 检查是否为英文
     */
    public static boolean isEnglish(@Nullable Locale locale) {
        return locale != null && "en".equals(locale.getLanguage());
    }
    // endregion
}

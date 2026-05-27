package com.citadawn.speechapp.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * TTS 语言和发音人处理工具类
 * 提供语言分组、默认发音人选择、数据排序等功能
 */
public class TtsLanguageVoiceHelper {

    // region 公开方法

    /**
     * 构建语言和发音人的映射关系
     *
     * @param languages 可用语言集合
     * @param voices    可用发音人集合
     * @return 语言到发音人列表的映射
     */
    @NonNull
    public static Map<Locale, List<Voice>> buildLanguageVoicesMap(@NonNull Set<Locale> languages, @NonNull Set<Voice> voices) {
        Map<Locale, List<Voice>> languageVoicesMap = new HashMap<>();

        // 初始化语言映射
        for (Locale locale : languages) {
            languageVoicesMap.put(locale, new ArrayList<>());
        }

        // 将发音人按语言分组
        for (Voice voice : voices) {
            Locale voiceLocale = voice.getLocale();
            List<Voice> voiceList = languageVoicesMap.get(voiceLocale);
            if (voiceList != null) {
                voiceList.add(voice);
            } else {
                // 如果发音人的语言不在语言列表中，添加该语言
                voiceList = new ArrayList<>();
                voiceList.add(voice);
                languageVoicesMap.put(voiceLocale, voiceList);
            }
        }

        return languageVoicesMap;
    }

    /**
     * 为每个语言确定默认发音人
     *
     * @param languageVoicesMap  语言到发音人的映射
     * @param globalDefaultVoice 全局默认发音人（可选）
     * @return 语言到默认发音人的映射
     */
    @NonNull
    public static Map<Locale, Voice> determineLanguageDefaultVoices(
            @NonNull Map<Locale, List<Voice>> languageVoicesMap,
            @Nullable Voice globalDefaultVoice) {

        Map<Locale, Voice> languageDefaultVoices = new HashMap<>();

        for (Map.Entry<Locale, List<Voice>> entry : languageVoicesMap.entrySet()) {
            Locale locale = entry.getKey();
            List<Voice> voiceList = entry.getValue();

            if (!voiceList.isEmpty()) {
                Voice defaultVoice;

                // 如果全局默认发音人是这个语言的，优先使用它
                if (globalDefaultVoice != null && globalDefaultVoice.getLocale().equals(locale)) {
                    defaultVoice = globalDefaultVoice;
                } else {
                    // 否则使用该语言的第一个发音人
                    defaultVoice = voiceList.get(0);
                }

                languageDefaultVoices.put(locale, defaultVoice);
            }
        }

        return languageDefaultVoices;
    }

    /**
     * 按语言名称排序语言列表
     * 会自动去除显示名称重复的语言（保留有发音人的或默认语言）
     *
     * @param locales       语言集合
     * @param defaultLocale 默认语言（将排在第一位）
     * @param context       上下文，用于获取当前界面语言
     * @param voices        可用发音人集合（用于判断哪个Locale有发音人）
     * @return 排序后且去重的语言列表
     */
    @NonNull
    public static List<Locale> sortLocalesByDisplayName(@NonNull Set<Locale> locales, Locale defaultLocale, @NonNull Context context, @Nullable Set<Voice> voices) {
        Locale currentLocale = com.citadawn.speechapp.util.LocaleHelper.getCurrentLocale(context);
        
        // 统计每个Locale的发音人数量
        Map<Locale, Integer> localeVoiceCount = new java.util.HashMap<>();
        if (voices != null) {
            for (Voice voice : voices) {
                Locale voiceLocale = voice.getLocale();
                localeVoiceCount.put(voiceLocale, localeVoiceCount.getOrDefault(voiceLocale, 0) + 1);
            }
        }
        
        // 按显示名称分组，去除重复
        Map<String, Locale> displayNameToLocale = new java.util.LinkedHashMap<>();
        
        for (Locale locale : locales) {
            String displayName = locale.getDisplayName(currentLocale);
            
            // 如果是默认语言，直接保留
            if (locale.equals(defaultLocale)) {
                displayNameToLocale.put(displayName, locale);
                continue;
            }
            
            // 如果显示名称已存在，比较两个Locale，保留更合适的
            if (displayNameToLocale.containsKey(displayName)) {
                Locale existing = displayNameToLocale.get(displayName);
                
                // 如果已存在的是默认语言，跳过当前的
                if (existing.equals(defaultLocale)) {
                    continue;
                }
                
                // 优先保留有发音人的Locale
                int existingVoiceCount = localeVoiceCount.getOrDefault(existing, 0);
                int currentVoiceCount = localeVoiceCount.getOrDefault(locale, 0);
                
                if (currentVoiceCount > existingVoiceCount) {
                    // 当前的有更多发音人，替换
                    displayNameToLocale.put(displayName, locale);
                } else if (currentVoiceCount == existingVoiceCount) {
                    // 发音人数量相同，优先保留没有script标签的Locale（如zh-CN优先于zh-Hans-CN）
                    String existingTag = existing.toLanguageTag();
                    String currentTag = locale.toLanguageTag();
                    
                    // 计算标签中的分隔符数量，数量少的更简洁
                    int existingParts = existingTag.split("-").length;
                    int currentParts = currentTag.split("-").length;
                    
                    if (currentParts < existingParts) {
                        // 当前的更简洁，替换
                        displayNameToLocale.put(displayName, locale);
                    }
                }
                // 否则保留已存在的
            } else {
                // 显示名称不存在，直接添加
                displayNameToLocale.put(displayName, locale);
            }
        }
        
        // 从去重后的Map中获取Locale列表并排序
        List<Locale> sortedLocales = new ArrayList<>(displayNameToLocale.values());
        
        Collator collator = Collator.getInstance();
        sortedLocales.sort((l1, l2) -> {
            // 默认语言始终排在最前面
            if (l1.equals(defaultLocale)) {
                return -1;
            }
            if (l2.equals(defaultLocale)) {
                return 1;
            }

            // 其他语言按显示名称排序，使用当前界面语言
            return collator.compare(l1.getDisplayName(currentLocale), l2.getDisplayName(currentLocale));
        });

        return sortedLocales;
    }
    
    /**
     * 按语言名称排序语言列表（兼容旧版本，不考虑发音人）
     *
     * @param locales       语言集合
     * @param defaultLocale 默认语言（将排在第一位）
     * @param context       上下文，用于获取当前界面语言
     * @return 排序后且去重的语言列表
     */
    @NonNull
    public static List<Locale> sortLocalesByDisplayName(@NonNull Set<Locale> locales, Locale defaultLocale, @NonNull Context context) {
        return sortLocalesByDisplayName(locales, defaultLocale, context, null);
    }

    /**
     * 按语言名称排序语言列表（无默认语言）
     *
     * @param locales 语言集合
     * @param context 上下文，用于获取当前界面语言
     * @return 排序后的语言列表
     */
    @NonNull
    public static List<Locale> sortLocalesByDisplayName(@NonNull Set<Locale> locales, @NonNull Context context) {
        List<Locale> sortedLocales = new ArrayList<>(locales);

        Collator collator = Collator.getInstance();
        Locale currentLocale = com.citadawn.speechapp.util.LocaleHelper.getCurrentLocale(context);
        sortedLocales.sort((l1, l2) ->
                collator.compare(l1.getDisplayName(currentLocale), l2.getDisplayName(currentLocale)));

        return sortedLocales;
    }


    /**
     * 为指定语言排序发音人列表
     *
     * @param voices       发音人列表
     * @param defaultVoice 默认发音人（将排在第一位）
     * @return 排序后的发音人列表
     */
    @NonNull
    public static List<Voice> sortVoicesByDefault(@NonNull List<Voice> voices, @Nullable Voice defaultVoice) {
        List<Voice> sortedVoices = new ArrayList<>(voices);

        if (defaultVoice != null) {
            sortedVoices.sort((v1, v2) -> {
                // 默认发音人排在最前面
                if (v1.equals(defaultVoice)) {
                    return -1;
                }
                if (v2.equals(defaultVoice)) {
                    return 1;
                }

                // 其他发音人按名称排序
                return v1.getName().compareTo(v2.getName());
            });
        } else {
            // 如果没有默认发音人，按名称排序
            sortedVoices.sort(Comparator.comparing(Voice::getName));
        }

        return sortedVoices;
    }

    /**
     * 获取指定语言的发音人列表
     *
     * @param voices       所有发音人集合
     * @param targetLocale 目标语言
     * @return 指定语言的发音人列表
     */
    @NonNull
    public static List<Voice> getVoicesForLanguage(@NonNull Set<Voice> voices, Locale targetLocale) {
        List<Voice> result = new ArrayList<>();

        for (Voice voice : voices) {
            if (voice.getLocale().equals(targetLocale)) {
                result.add(voice);
            }
        }

        return result;
    }

    /**
     * 按默认引擎排序TTS引擎列表
     * 默认引擎排在最前面，其他引擎按名称排序
     *
     * @param engines           TTS引擎列表
     * @param defaultEngineName 默认引擎包名
     * @return 排序后的引擎列表
     */
    @NonNull
    public static List<TextToSpeech.EngineInfo> sortEnginesByDefault(
            @NonNull List<TextToSpeech.EngineInfo> engines,
            @Nullable String defaultEngineName) {

        List<TextToSpeech.EngineInfo> sortedEngines = new ArrayList<>(engines);

        if (defaultEngineName != null) {
            sortedEngines.sort((e1, e2) -> {
                // 默认引擎始终排在最前面
                if (e1.name.equals(defaultEngineName)) {
                    return -1;
                }
                if (e2.name.equals(defaultEngineName)) {
                    return 1;
                }

                // 其他引擎按显示名称排序
                String label1 = e1.label != null ? e1.label : e1.name;
                String label2 = e2.label != null ? e2.label : e2.name;
                return label1.compareTo(label2);
            });
        } else {
            // 如果没有默认引擎，按显示名称排序
            sortedEngines.sort((e1, e2) -> {
                String label1 = e1.label != null ? e1.label : e1.name;
                String label2 = e2.label != null ? e2.label : e2.name;
                return label1.compareTo(label2);
            });
        }

        return sortedEngines;
    }

    /**
     * 清理发音人名称，去除技术标识符
     * 使用主界面的规则：去除下划线及后缀数字
     *
     * @param voiceName 原始发音人名称
     * @return 清理后的发音人名称
     */
    @NonNull
    public static String cleanVoiceName(@Nullable String voiceName) {
        if (voiceName == null) {
            return "";
        }
        // 去除下划线及后缀数字
        return voiceName.replaceAll("_[0-9]+$", "");
    }

    /**
     * 判断特性字符串是否为无意义的特性
     * 使用主界面的规则
     *
     * @param feature 特性字符串
     * @return 是否为无意义特性
     */
    public static boolean isMeaninglessFeature(@Nullable String feature) {
        if (feature == null || feature.isEmpty()) {
            return true;
        }

        // 纯英文单词
        if (feature.matches("^[A-Za-z]+$")) {
            return true;
        }
        // 纯数字
        if (feature.matches("^\\d+$")) {
            return true;
        }
        // 全大写或全小写且长度大于20
        if ((feature.equals(feature.toUpperCase()) || feature.equals(feature.toLowerCase())) && feature.length() > Constants.MAX_FEATURE_NAME_LENGTH) {
            return true;
        }
        // 全为16进制且长度大于16
        if (feature.matches("^[0-9A-Fa-f]+$") && feature.length() > Constants.MAX_HEX_STRING_LENGTH) {
            return true;
        }
        // 单字符
        if (feature.length() == 1) {
            return true;
        }
        // UUID
        return feature.matches("^[0-9a-fA-F-]{" + Constants.UUID_MIN_LENGTH + ",}$");
    }

    /**
     * 判断是否应该显示特性信息
     * 使用主界面的规则
     *
     * @param features 特性集合
     * @return 是否应该显示
     */
    public static boolean shouldShowFeatures(@Nullable Set<String> features) {
        if (features == null || features.isEmpty()) {
            return false;
        }
        for (String f : features) {
            if (!isMeaninglessFeature(f)) {
                return true;
            }
        }
        return false;
    }

    // endregion
} 
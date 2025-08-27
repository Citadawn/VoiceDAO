package com.citadawn.speechapp.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

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
    public static Map<Locale, List<Voice>> buildLanguageVoicesMap(Set<Locale> languages, Set<Voice> voices) {
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
    public static Map<Locale, Voice> determineLanguageDefaultVoices(
            Map<Locale, List<Voice>> languageVoicesMap,
            Voice globalDefaultVoice) {

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
     *
     * @param locales       语言集合
     * @param defaultLocale 默认语言（将排在第一位）
     * @param context       上下文，用于获取当前界面语言
     * @return 排序后的语言列表
     */
    public static List<Locale> sortLocalesByDisplayName(Set<Locale> locales, Locale defaultLocale, Context context) {
        List<Locale> sortedLocales = new ArrayList<>(locales);

        Collator collator = Collator.getInstance();
        Locale currentLocale = com.citadawn.speechapp.util.LocaleHelper.getCurrentLocale(context);
        sortedLocales.sort((l1, l2) -> {
            // 默认语言始终排在最前面
            if (l1.equals(defaultLocale)) return -1;
            if (l2.equals(defaultLocale)) return 1;

            // 其他语言按显示名称排序，使用当前界面语言
            return collator.compare(l1.getDisplayName(currentLocale), l2.getDisplayName(currentLocale));
        });

        return sortedLocales;
    }

    /**
     * 按语言名称排序语言列表（无默认语言）
     *
     * @param locales 语言集合
     * @param context 上下文，用于获取当前界面语言
     * @return 排序后的语言列表
     */
    public static List<Locale> sortLocalesByDisplayName(Set<Locale> locales, Context context) {
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
    public static List<Voice> sortVoicesByDefault(List<Voice> voices, Voice defaultVoice) {
        List<Voice> sortedVoices = new ArrayList<>(voices);

        if (defaultVoice != null) {
            sortedVoices.sort((v1, v2) -> {
                // 默认发音人排在最前面
                if (v1.equals(defaultVoice)) return -1;
                if (v2.equals(defaultVoice)) return 1;

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
    public static List<Voice> getVoicesForLanguage(Set<Voice> voices, Locale targetLocale) {
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
    public static List<TextToSpeech.EngineInfo> sortEnginesByDefault(
            List<TextToSpeech.EngineInfo> engines,
            String defaultEngineName) {

        List<TextToSpeech.EngineInfo> sortedEngines = new ArrayList<>(engines);

        if (defaultEngineName != null) {
            sortedEngines.sort((e1, e2) -> {
                // 默认引擎始终排在最前面
                if (e1.name.equals(defaultEngineName)) return -1;
                if (e2.name.equals(defaultEngineName)) return 1;

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
    public static String cleanVoiceName(String voiceName) {
        if (voiceName == null) return "";
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
    public static boolean isMeaninglessFeature(String feature) {
        if (feature == null || feature.isEmpty()) return true;

        // 纯英文单词
        if (feature.matches("^[A-Za-z]+$"))
            return true;
        // 纯数字
        if (feature.matches("^\\d+$"))
            return true;
        // 全大写或全小写且长度大于20
        if ((feature.equals(feature.toUpperCase()) || feature.equals(feature.toLowerCase())) && feature.length() > 20)
            return true;
        // 全为16进制且长度大于16
        if (feature.matches("^[0-9A-Fa-f]+$") && feature.length() > 16)
            return true;
        // 单字符
        if (feature.length() == 1)
            return true;
        // UUID
        return feature.matches("^[0-9a-fA-F-]{32,}$");
    }

    /**
     * 判断是否应该显示特性信息
     * 使用主界面的规则
     *
     * @param features 特性集合
     * @return 是否应该显示
     */
    public static boolean shouldShowFeatures(Set<String> features) {
        if (features == null || features.isEmpty())
            return false;
        for (String f : features) {
            if (!isMeaninglessFeature(f))
                return true;
        }
        return false;
    }

    // endregion
} 
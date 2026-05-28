package com.citadawn.speechapp.util;

import android.speech.tts.Voice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * TTS 引擎 {@link Locale} 的界面显示名解析。
 * 部分引擎使用非 BCP47 语言码（如 chn 表示中文），{@link Locale#getDisplayName(Locale)}
 * 会按 ISO 639-2 误解析（如 chn → 奇努克混合语）。
 */
public final class TtsLocaleDisplayHelper {

    /**
     * 引擎常用语言码 → BCP47 语言标签（仅用于显示名，不改变 TTS 实际 Locale）。
     */
    private static final Map<String, String> LANGUAGE_TAG_ALIASES;

    static {
        Map<String, String> aliases = new HashMap<>();
        aliases.put("chn", "zh");
        LANGUAGE_TAG_ALIASES = Collections.unmodifiableMap(aliases);
    }

    private TtsLocaleDisplayHelper() {
    }

    @NonNull
    public static String getDisplayName(@NonNull Locale locale, @NonNull Locale appLocale) {
        return getDisplayName(locale, appLocale, null);
    }

    @NonNull
    public static String getDisplayName(@NonNull Locale locale, @NonNull Locale appLocale,
                                       @Nullable Iterable<Voice> voicesForLocale) {
        return resolveDisplayLocale(locale, voicesForLocale).getDisplayName(appLocale);
    }

    @NonNull
    public static Locale resolveDisplayLocale(@NonNull Locale locale,
                                              @Nullable Iterable<Voice> voicesForLocale) {
        String lang = locale.getLanguage();
        if (lang != null && !lang.isEmpty()) {
            String aliasTag = LANGUAGE_TAG_ALIASES.get(lang.toLowerCase(Locale.ROOT));
            if (aliasTag != null) {
                return mergeLocaleFields(Locale.forLanguageTag(aliasTag), locale);
            }
        }
        if (shouldInferFromVoices(lang)) {
            Locale inferred = inferLocaleFromVoices(voicesForLocale);
            if (inferred != null) {
                return mergeLocaleFields(inferred, locale);
            }
        }
        return locale;
    }

    @NonNull
    public static List<Voice> voicesForLocale(@Nullable Set<Voice> voices, @NonNull Locale locale) {
        if (voices == null || voices.isEmpty()) {
            return Collections.emptyList();
        }
        List<Voice> result = new ArrayList<>();
        for (Voice voice : voices) {
            if (locale.equals(voice.getLocale())) {
                result.add(voice);
            }
        }
        return result;
    }

    private static boolean shouldInferFromVoices(@Nullable String language) {
        return language != null && language.length() == 3;
    }

    @Nullable
    private static Locale inferLocaleFromVoices(@Nullable Iterable<Voice> voicesForLocale) {
        if (voicesForLocale == null) {
            return null;
        }
        for (Voice voice : voicesForLocale) {
            String name = voice.getName();
            if (name == null || name.isEmpty()) {
                continue;
            }
            if (containsAny(name, "中文", "汉语", "普通话", "国语", "Chinese", "Mandarin")) {
                return Locale.CHINESE;
            }
            if (containsAny(name, "粤语", "Cantonese", "廣東話", "广东话")) {
                return Locale.forLanguageTag("yue");
            }
            if (containsAny(name, "英文", "English")) {
                return Locale.ENGLISH;
            }
            if (containsAny(name, "日文", "日语", "日本語", "Japanese")) {
                return Locale.JAPANESE;
            }
            if (containsAny(name, "韩文", "韩语", "한국어", "Korean")) {
                return Locale.KOREAN;
            }
        }
        return null;
    }

    private static boolean containsAny(@NonNull String haystack, @NonNull String... needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private static Locale mergeLocaleFields(@NonNull Locale displayBase, @NonNull Locale original) {
        String country = original.getCountry();
        String variant = original.getVariant();
        if (country == null || country.isEmpty()) {
            if (variant == null || variant.isEmpty()) {
                return displayBase;
            }
            return new Locale.Builder()
                    .setLanguage(displayBase.getLanguage())
                    .setVariant(variant)
                    .build();
        }
        Locale.Builder builder = new Locale.Builder().setLanguage(displayBase.getLanguage()).setRegion(country);
        if (variant != null && !variant.isEmpty()) {
            builder.setVariant(variant);
        }
        return builder.build();
    }
}

package com.citadawn.speechapp.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.citadawn.speechapp.R;

/**
 * 主界面「语音合成状态」文案与颜色解析。
 */
public final class TtsSpeakStatusHelper {

    public static final String UTTERANCE_SPEAK = "tts_speak";
    public static final String UTTERANCE_SAVE = "tts_save";
    public static final String UTTERANCE_SAVE_COPY = "tts_save_copy";

    /** 与 {@link TextToSpeech} 错误码一致（部分常量未在 compileSdk 公开）。 */
    private static final int ERROR_NOT_INITIALIZED = -9;
    private static final int ERROR_LANGUAGE_NOT_SUPPORTED = -10;
    private static final int ERROR_VOICE_NOT_FOUND = -11;

    public enum WorkState {
        IDLE,
        PREPARING_SPEAK,
        PREPARING_SAVE,
        SPEAKING,
        SYNTHESIZING,
        COPYING,
        STOPPED_BY_USER,
        STOPPED_INTERRUPTED,
        ERROR
    }

    private TtsSpeakStatusHelper() {
    }

    public static void bindStatus(@NonNull TextView statusView, @NonNull Context context,
                                  boolean engineReady, @NonNull WorkState state,
                                  int errorCode, @Nullable String errorUtteranceId) {
        StatusPresentation presentation = resolve(context, engineReady, state, errorCode, errorUtteranceId);
        if (presentation.formatArgs != null && presentation.formatArgs.length > 0) {
            statusView.setText(context.getString(presentation.textResId, presentation.formatArgs));
        } else {
            statusView.setText(presentation.textResId);
        }
        statusView.setTextColor(ContextCompat.getColor(context, presentation.colorResId));
    }

    public static boolean isStopEnabled(@NonNull WorkState state) {
        return state == WorkState.SPEAKING
                || state == WorkState.SYNTHESIZING
                || state == WorkState.PREPARING_SPEAK
                || state == WorkState.PREPARING_SAVE;
    }

    public static boolean blocksSpeakAndSave(@NonNull WorkState state, boolean isSavingAudio) {
        return isSavingAudio
                || state == WorkState.SPEAKING
                || state == WorkState.SYNTHESIZING
                || state == WorkState.COPYING
                || state == WorkState.PREPARING_SPEAK
                || state == WorkState.PREPARING_SAVE;
    }

    /** 朗读/保存进行中的状态需周期性刷新按钮与状态文案；空闲时不轮询。 */
    public static boolean needsStatusPolling(@NonNull WorkState state, boolean isSavingAudio) {
        return blocksSpeakAndSave(state, isSavingAudio);
    }

    @NonNull
    public static String getErrorDetail(@NonNull Context context, int errorCode) {
        return context.getString(getErrorDetailRes(errorCode));
    }

    @StringRes
    public static int getErrorDetailRes(int errorCode) {
        return switch (errorCode) {
            case TextToSpeech.ERROR_NETWORK_TIMEOUT -> R.string.tts_error_network_timeout;
            case TextToSpeech.ERROR_NETWORK -> R.string.tts_error_network;
            case TextToSpeech.ERROR_INVALID_REQUEST -> R.string.tts_error_invalid_request;
            case ERROR_NOT_INITIALIZED -> R.string.tts_error_not_initialized;
            case TextToSpeech.ERROR_OUTPUT -> R.string.tts_error_output;
            case TextToSpeech.ERROR_SERVICE -> R.string.tts_error_service;
            case TextToSpeech.ERROR_SYNTHESIS -> R.string.tts_error_synthesis;
            case ERROR_LANGUAGE_NOT_SUPPORTED -> R.string.tts_error_language_not_supported;
            case ERROR_VOICE_NOT_FOUND -> R.string.tts_error_voice_not_found;
            default -> R.string.tts_error_unknown;
        };
    }

    @NonNull
    private static StatusPresentation resolve(@NonNull Context context, boolean engineReady,
                                            @NonNull WorkState state, int errorCode,
                                            @Nullable String errorUtteranceId) {
        if (!engineReady) {
            return new StatusPresentation(R.string.tts_status_engine_not_ready, null,
                    R.color.btn_disabled_bg);
        }
        return switch (state) {
            case PREPARING_SPEAK -> new StatusPresentation(R.string.tts_status_preparing_speak, null,
                    R.color.accent_warning);
            case PREPARING_SAVE -> new StatusPresentation(R.string.tts_status_preparing_save, null,
                    R.color.accent_warning);
            case SPEAKING -> new StatusPresentation(R.string.tts_status_speaking, null,
                    R.color.accent_warning);
            case SYNTHESIZING -> new StatusPresentation(R.string.tts_status_synthesizing, null,
                    R.color.tts_support_variant);
            case COPYING -> new StatusPresentation(R.string.tts_status_copying, null,
                    R.color.tts_support_variant);
            case STOPPED_BY_USER -> new StatusPresentation(R.string.tts_status_stopped_user, null,
                    R.color.tts_support_partial);
            case STOPPED_INTERRUPTED -> new StatusPresentation(R.string.tts_status_stopped_interrupted, null,
                    R.color.tts_support_partial);
            case ERROR -> errorPresentation(context, errorCode, errorUtteranceId);
            default -> new StatusPresentation(R.string.tts_status_idle, null, R.color.tts_support_full);
        };
    }

    @NonNull
    private static StatusPresentation errorPresentation(@NonNull Context context, int errorCode,
                                                        @Nullable String errorUtteranceId) {
        if (UTTERANCE_SAVE_COPY.equals(errorUtteranceId)) {
            return new StatusPresentation(R.string.tts_status_error_copy, null, R.color.btn_warning_bg);
        }
        String detail = getErrorDetail(context, errorCode);
        if (UTTERANCE_SAVE.equals(errorUtteranceId)) {
            return new StatusPresentation(R.string.tts_status_error_save, new Object[]{detail},
                    R.color.btn_warning_bg);
        }
        if (UTTERANCE_SPEAK.equals(errorUtteranceId)) {
            return new StatusPresentation(R.string.tts_status_error_speak, new Object[]{detail},
                    R.color.btn_warning_bg);
        }
        return new StatusPresentation(R.string.tts_status_error_generic, new Object[]{detail},
                R.color.btn_warning_bg);
    }

    private static final class StatusPresentation {
        @StringRes
        final int textResId;
        @Nullable
        final Object[] formatArgs;
        @ColorRes
        final int colorResId;

        StatusPresentation(@StringRes int textResId, @Nullable Object[] formatArgs,
                           @ColorRes int colorResId) {
            this.textResId = textResId;
            this.formatArgs = formatArgs;
            this.colorResId = colorResId;
        }
    }
}

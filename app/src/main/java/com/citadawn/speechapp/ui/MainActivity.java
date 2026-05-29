package com.citadawn.speechapp.ui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.documentfile.provider.DocumentFile;

import com.citadawn.speechapp.R;
import com.citadawn.speechapp.ui.test.TestCase;
import com.citadawn.speechapp.ui.test.DebugModeUi;
import com.citadawn.speechapp.ui.test.TestManager;
import com.citadawn.speechapp.ui.test.TestModeDialog;
import com.citadawn.speechapp.util.ButtonTextHelper;
import com.citadawn.speechapp.util.ClearButtonHelper;
import com.citadawn.speechapp.util.Constants;
import com.citadawn.speechapp.util.DialogHelper;
import com.citadawn.speechapp.util.InfoIconHelper;
import com.citadawn.speechapp.util.InfoIconPositionHelper;
import com.citadawn.speechapp.util.LocaleHelper;
import com.citadawn.speechapp.util.SeekBarHelper;
import com.citadawn.speechapp.util.SystemBarsHelper;
import com.citadawn.speechapp.util.TextLengthHelper;
import com.citadawn.speechapp.util.ToastHelper;
import com.citadawn.speechapp.util.TtsEngineChangeHelper;
import com.citadawn.speechapp.util.TtsEngineHelper;
import com.citadawn.speechapp.util.TtsLanguageVoiceHelper;
import com.citadawn.speechapp.util.TtsLocaleDisplayHelper;
import com.citadawn.speechapp.util.TtsSpeakStatusHelper;
import com.citadawn.speechapp.util.ViewHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

// region 内部适配器类

/**
 * 语言选择适配器
 */
class LanguageAdapter extends BaseAdapter {
    // region 成员变量

    private final List<Locale> locales;
    private final LayoutInflater inflater;
    private final Context context;
    private final Locale defaultLocale;
    private final List<String> displayNames;
    private int selectedPosition = 0; // 保存当前选中位置

    // endregion

    // region 构造方法

    public LanguageAdapter(@NonNull Context context, @NonNull List<Locale> locales,
                           @NonNull List<String> displayNames, @Nullable Locale defaultLocale) {
        this.context = context;
        this.locales = locales;
        this.inflater = LayoutInflater.from(context);
        this.defaultLocale = defaultLocale;
        this.displayNames = displayNames;
    }

    // endregion

    // region 公开方法

    /**
     * 记录选中项；下拉高亮在下次展开时由 {@link #getDropDownView} 读取，无需全量 notify。
     */
    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
    }

    // endregion

    // region 适配器核心方法

    @Override
    public int getCount() {
        return locales.size();
    }

    @Override
    public Object getItem(int position) {
        return locales.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent, false);
    }

    @NonNull
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = createView(position, convertView, parent, true);
        LinearLayout layoutRoot = view.findViewById(R.id.layoutRoot);
        if (position == selectedPosition) {
            layoutRoot.setBackgroundResource(R.color.tts_spinner_selected_bg);
        } else {
            layoutRoot.setBackgroundResource(R.color.tts_spinner_normal_bg);
        }
        return view;
    }

    // endregion

    // region 私有辅助方法

    /**
     * 创建语言选择项的视图
     *
     * @param position    位置
     * @param convertView 复用的视图
     * @param parent      父容器
     * @param isDropdown  是否为下拉视图
     * @return 创建的视图
     */
    @NonNull
    private View createView(int position, View convertView, ViewGroup parent, boolean isDropdown) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.spinner_item_language, parent, false);
        }
        TextView tv = view.findViewById(R.id.tvLanguageName);
        tv.setText(displayNames.get(position));
        // 可选：下拉项背景色区分
        if (isDropdown) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.pure_white));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
        }
        return view;
    }

    // endregion
}

/**
 * 发音人选择适配器
 * 支持显示发音人特性（features）
 */
class VoiceAdapter extends BaseAdapter {
    // region 成员变量

    private static final Pattern FEATURE_PURE_ALPHA = Pattern.compile("^[A-Za-z]+$");
    private static final Pattern FEATURE_PURE_DIGIT = Pattern.compile("^\\d+$");
    private static final Pattern FEATURE_HEX = Pattern.compile("^[0-9A-Fa-f]+$");
    private static final Pattern FEATURE_UUID = Pattern.compile("^[0-9a-fA-F-]{8,}$");
    private static final Pattern VOICE_NAME_SUFFIX = Pattern.compile("_[0-9]+$");

    private final List<Voice> voices;
    private final LayoutInflater inflater;
    private final Context context;
    private Voice defaultVoice;
    private int selectedPosition = 0;

    // endregion

    // region 构造方法

    public VoiceAdapter(Context context, List<Voice> voices, Voice defaultVoice) {
        this.context = context;
        this.voices = voices;
        this.defaultVoice = defaultVoice;
        this.inflater = LayoutInflater.from(context);
    }

    // endregion

    // region 公开方法

    /** 记录选中项；下拉高亮在下次展开时生效，避免 notifyDataSetChanged 全量刷新。 */
    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
    }

    /**
     * 语言切换后刷新列表：{@link #voices} 与外部 {@code voiceList} 为同一引用，仅更新默认项并通知。
     */
    public void refreshAfterVoiceListChange(@Nullable Voice localeDefaultVoice) {
        this.defaultVoice = localeDefaultVoice;
        this.selectedPosition = 0;
        notifyDataSetChanged();
    }

    // endregion

    // region 适配器核心方法

    @Override
    public int getCount() {
        return voices.size();
    }

    @Override
    public Object getItem(int position) {
        return voices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent, false);
    }

    @NonNull
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = createView(position, convertView, parent, true);
        LinearLayout layoutRoot = view.findViewById(R.id.layoutRoot);
        if (position == selectedPosition) {
            layoutRoot.setBackgroundResource(R.color.tts_spinner_selected_bg);
        } else {
            layoutRoot.setBackgroundResource(R.color.tts_spinner_normal_bg);
        }
        return view;
    }

    // endregion

    // region 私有辅助方法

    /**
     * 判断特性字符串是否为无意义的特性
     *
     * @param feature 特性字符串
     * @return 是否为无意义特性
     */
    private boolean isMeaninglessFeature(@NonNull String feature) {
        if (FEATURE_PURE_ALPHA.matcher(feature).matches()) {
            return true;
        }
        if (FEATURE_PURE_DIGIT.matcher(feature).matches()) {
            return true;
        }
        if ((feature.equals(feature.toUpperCase()) || feature.equals(feature.toLowerCase()))
                && feature.length() > Constants.MAX_FEATURE_NAME_LENGTH) {
            return true;
        }
        if (FEATURE_HEX.matcher(feature).matches() && feature.length() > Constants.MAX_HEX_STRING_LENGTH) {
            return true;
        }
        if (feature.length() == 1) {
            return true;
        }
        return FEATURE_UUID.matcher(feature).matches();
    }

    /**
     * 判断是否应该显示特性信息
     *
     * @param features 特性集合
     * @return 是否应该显示
     */
    private boolean shouldShowFeatures(@Nullable Set<String> features) {
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

    /**
     * 创建发音人选择项的视图
     *
     * @param position    位置
     * @param convertView 复用的视图
     * @param parent      父容器
     * @return 创建的视图
     */
    @NonNull
    private View createView(int position, View convertView, ViewGroup parent, boolean isDropdown) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.spinner_item_voice, parent, false);
        }

        Voice voice = voices.get(position);
        TextView tvVoiceName = view.findViewById(R.id.tvVoiceName);
        TextView tvVoiceFeatures = view.findViewById(R.id.tvVoiceFeatures);
        LinearLayout layoutRoot = view.findViewById(R.id.layoutRoot);
        int rowHeightPx = context.getResources().getDimensionPixelSize(R.dimen.dp_48);

        // 设置发音人名称
        String voiceName = voice.getName();
        voiceName = VOICE_NAME_SUFFIX.matcher(voiceName).replaceAll("");
        // 检查是否是默认发音人
        boolean isDefault = (voice.equals(defaultVoice));
        if (isDefault) {
            voiceName += context.getString(R.string.default_value);
        }
        tvVoiceName.setText(voiceName);

        // 设置 features（如果存在且有意义）
        Set<String> features = voice.getFeatures();
        if (shouldShowFeatures(features)) {
            String featuresText = "[" + String.join(", ", features) + "]";
            tvVoiceFeatures.setText(featuresText);
            tvVoiceFeatures.setVisibility(View.VISIBLE);
        } else {
            tvVoiceFeatures.setVisibility(View.GONE);
        }

        ViewGroup.LayoutParams rootLp = layoutRoot.getLayoutParams();
        if (rootLp == null) {
            rootLp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, rowHeightPx);
        } else if (isDropdown && tvVoiceFeatures.getVisibility() == View.VISIBLE) {
            rootLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutRoot.setMinimumHeight(rowHeightPx);
        } else {
            rootLp.height = rowHeightPx;
            layoutRoot.setMinimumHeight(0);
        }
        layoutRoot.setLayoutParams(rootLp);

        return view;
    }

    // endregion
}

// endregion

/**
 * 主界面活动类
 * 提供文本转语音的核心功能，包括朗读、保存音频、语言选择等
 */
public class MainActivity extends AppCompatActivity {

    // region TTS 相关变量

    private static final String PREFS_NAME = Constants.PREFS_NAME;
    private static final String KEY_SAVE_DIR_URI = Constants.KEY_SAVE_DIR_URI;
    private final HashMap<Locale, Voice> languageDefaultVoices = new HashMap<>(); // 每个语言的默认发音人
    private final ArrayList<Locale> localeList = new ArrayList<>();
    private final ArrayList<Voice> voiceList = new ArrayList<>();
    private final Handler ttsStatusHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService ttsCatalogExecutor = Executors.newSingleThreadExecutor();
    @Nullable
    private Runnable ttsStatusRunnable;
    private Map<Locale, List<Voice>> cachedVoicesByLocale = Collections.emptyMap();
    private TextToSpeech tts;
    private boolean isTtsReady = false;
    @Nullable
    private Locale currentLocale = null; // 当前语言，将通过TTS API动态获取

    // endregion

    // region UI 控件变量
    @Nullable
    private Locale defaultLocale = null; // 默认语言，将通过TTS API获取
    @Nullable
    private Voice globalDefaultVoice = null; // 全局默认发音人
    @Nullable
    private Voice currentVoice = null;
    @Nullable
    private String cachedTtsEngineDisplayName = null;
    private boolean isLangSpinnerInit = false;
    private boolean isVoiceSpinnerInit = false;
    /**
     * 程序化更新发音人 Spinner 时为 true，避免触发 onItemSelected 重复设置 TTS 与调试区刷新。
     */
    private boolean suppressVoiceSpinnerCallback = false;
    private ManualScrollEditText editText;
    private androidx.core.widget.NestedScrollView nestedScrollView;
    private View focusSink;
    private float dismissFocusTouchStartX;
    private float dismissFocusTouchStartY;
    private boolean dismissFocusTouchTracking;
    private boolean dismissFocusTouchStartedInEditText;
    private boolean focusDismissScheduledForGesture;
    private Button btnSpeak;
    private SeekBar seekBarSpeed, seekBarPitch;
    private Button btnClear;
    private Button btnStop;
    private Button btnSaveAudio;
    private TextView textSpeechRateValue, textPitchValue;
    private Button btnSpeedReset, btnPitchReset;
    private Spinner spinnerLanguage, spinnerVoice;
    private Button btnLangVoiceReset;
    private Button btnSetTtsInfoDir;
    private Button btnOutputTtsInfoNow;
    private Button btnFillTestText;

    // endregion

    // region 状态和配置变量
    private TextView tvTtsEngineStatus, tvAudioSaveDir, tvTtsSpeakStatus, tvSelectedTestCases, tvTtsEngineInfo;
    private TextView tvTtsInfoSaveDir;
    private TextView tvAudioSaveDirLabel, tvTtsInfoSaveDirLabel;
    private LinearLayout layoutAudioSaveDirBlock, layoutAudioSaveDirValueRow;
    private LinearLayout layoutStatusDebugSection;
    private LinearLayout layoutTtsInfoDir, layoutTtsInfoSaveDirValueRow;
    private LinearLayout layoutCurrentVoiceDebug;
    private LinearLayout layoutSelectedTestCases;
    private TextView tvCurrentVoiceDebugLabel;
    private TextView tvCurrentVoiceDebug;
    private ImageButton btnCopySaveDir;
    private ImageButton btnCopyTtsInfoDir;
    private ImageButton btnCopyCurrentVoice;
    private ImageView ivTtsEngineIcon;
    private Button btnCancelSave;
    private float speechRate = 1.0f;
    private float pitch = 1.0f;
    @Nullable
    private String pendingAudioText = null;
    @Nullable
    private Uri saveDirUri = null;
    @Nullable
    private Uri ttsInfoDirUri = null;
    // 替换设置保存目录为registerForActivityResult
    @SuppressLint("WrongConstant")
    private final ActivityResultLauncher<Intent> openDirLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        final int takeFlags = result.getData().getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        try {
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                            saveDirUri = uri;
                            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                            editor.putString(KEY_SAVE_DIR_URI, uri.toString());
                            editor.apply();
                            ToastHelper.showShort(this, R.string.toast_save_dir_set_success);
                            updateStatusInfo();
                        } catch (Exception e) {
                            Log.e("MainActivity", "Exception", e);
                            ToastHelper.showShort(this, R.string.toast_save_dir_set_fail);
                        }
                    }
                }
            });
    
    // TTS信息目录选择器
    @SuppressLint("WrongConstant")
    private final ActivityResultLauncher<Intent> openTtsInfoDirLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        final int takeFlags = result.getData().getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        try {
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                            ttsInfoDirUri = uri;
                            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                            editor.putString("tts_info_dir_uri", uri.toString());
                            editor.apply();
                            ToastHelper.showShort(this, R.string.toast_tts_info_dir_set_success);
                            updateStatusInfo();
                        } catch (Exception e) {
                            Log.e("MainActivity", "Exception", e);
                            ToastHelper.showShort(this, R.string.toast_tts_info_dir_set_fail);
                        }
                    }
                }
            });
    
    private boolean isSavingAudio = false;

    // endregion

    // region 枚举和常量
    @Nullable
    private File tempAudioFile = null;
    @NonNull
    private String currentAudioFileName = "tts_output.wav";

    // endregion

    // region 状态变量

    private static final int DEBUG_MODE_REQUIRED_TAPS = 7;
    private static final long DEBUG_MODE_TAP_INTERVAL_MS = 2000L;
    private static final int DEBUG_MODE_TAP_HINT_FROM = 4;

    private int debugModeTitleTapCount = 0;
    private long lastDebugModeTitleTapAt = 0L;

    @NonNull
    private volatile TtsSpeakStatusHelper.WorkState ttsWorkState = TtsSpeakStatusHelper.WorkState.IDLE;
    private volatile int lastTtsErrorCode = TextToSpeech.ERROR;
    @Nullable
    private volatile String lastErrorUtteranceId;
    /** 用户停止或新任务提交时递增，用于忽略迟到的 TTS 回调。 */
    private int utteranceGeneration = 0;
    @NonNull
    private final java.util.Map<String, Integer> utteranceGenerationById = new java.util.HashMap<>();
    private ActivityResultLauncher<Intent> editorLauncher;
    private long lastBackPressedTime = 0;

    /**
     * 活动创建时初始化UI和TTS引擎
     */
    // endregion

    // region 生命周期方法
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 应用用户选择的语言设置 Apply user selected language setting
        LocaleHelper.setLocale(this, LocaleHelper.getCurrentLocale(this));

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // 始终浅色
        super.onCreate(savedInstanceState);
        SystemBarsHelper.enable(this);
        setContentView(R.layout.activity_main);

        // 设置返回键处理
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 主界面双击返回键退出逻辑
                long now = System.currentTimeMillis();
                if (now - lastBackPressedTime < Constants.DOUBLE_BACK_EXIT_INTERVAL) {
                    TestManager.getInstance().resetAll();
                    finishAffinity();
                } else {
                    // 第一次按返回键，记录时间并提示
                    lastBackPressedTime = now;
                    ToastHelper.showShort(MainActivity.this, R.string.toast_double_back_exit);
                }
            }
        });

        // 初始化调试项的国际化文本
        initializeTestCases();

        // 设置自定义Toolbar为ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        SystemBarsHelper.applyToolbarTopInsets(toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_more_vert_white_24dp));

        // 更新 Toolbar 标题，支持国际化；并绑定调试模式入口（连续点击标题）
        updateToolbarTitle();

        SystemBarsHelper.applyContentMarginInsets(findViewById(R.id.rootContainer));

        editText = findViewById(R.id.editText);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        focusSink = findViewById(R.id.focusSink);

        nestedScrollView.setOnScrollChangeListener(
                (androidx.core.widget.NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (scrollY != oldScrollY && editText != null && editText.hasFocus()) {
                        clearEditTextFocusAndHideKeyboard();
                    }
                });
        btnSpeak = findViewById(R.id.btnSpeak);
        seekBarSpeed = findViewById(R.id.seekBarSpeed);
        seekBarPitch = findViewById(R.id.seekBarPitch);
        tvTtsEngineStatus = findViewById(R.id.tvTtsEngineStatus);
        tvAudioSaveDir = findViewById(R.id.tvAudioSaveDir);
        tvAudioSaveDirLabel = findViewById(R.id.tvAudioSaveDirLabel);
        layoutAudioSaveDirBlock = findViewById(R.id.layoutAudioSaveDirBlock);
        layoutAudioSaveDirValueRow = findViewById(R.id.layoutAudioSaveDirValueRow);
        tvTtsSpeakStatus = findViewById(R.id.tvTtsSpeakStatus);
        tvSelectedTestCases = findViewById(R.id.tvSelectedTestCases);
        tvTtsEngineInfo = findViewById(R.id.tvTtsEngineInfo);
        ivTtsEngineIcon = findViewById(R.id.ivTtsEngineIcon);
        
        // 初始化TTS信息目录相关控件（必须在 updateStatusInfo() 之前）
        tvTtsInfoSaveDir = findViewById(R.id.tvTtsInfoSaveDir);
        tvTtsInfoSaveDirLabel = findViewById(R.id.tvTtsInfoSaveDirLabel);
        layoutStatusDebugSection = findViewById(R.id.layoutStatusDebugSection);
        layoutTtsInfoDir = findViewById(R.id.layoutTtsInfoDir);
        layoutTtsInfoSaveDirValueRow = findViewById(R.id.layoutTtsInfoSaveDirValueRow);
        layoutCurrentVoiceDebug = findViewById(R.id.layoutCurrentVoiceDebug);
        layoutSelectedTestCases = findViewById(R.id.layoutSelectedTestCases);
        tvCurrentVoiceDebugLabel = findViewById(R.id.tvCurrentVoiceDebugLabel);
        tvCurrentVoiceDebug = findViewById(R.id.tvCurrentVoiceDebug);
        btnCopySaveDir = findViewById(R.id.btnCopySaveDir);
        btnCopyTtsInfoDir = findViewById(R.id.btnCopyTtsInfoDir);
        btnCopyCurrentVoice = findViewById(R.id.btnCopyCurrentVoice);
        setupStatusCopyButtons();
        
        btnStop = findViewById(R.id.btnStop);
        btnSaveAudio = findViewById(R.id.btnSaveAudio);
        // TTS未初始化时按钮不可用
        btnSpeak.setEnabled(false);
        btnStop.setEnabled(false);
        btnSaveAudio.setEnabled(false);
        updateStatusInfo();
        btnClear = findViewById(R.id.btnClear);
        // 设置主界面清空按钮逻辑（复用工具类）
        ClearButtonHelper.setupClearButton(btnClear, editText);

        // 为所有按钮设置自动文本大小调整
        setupAllButtonsAutoTextSize();
        btnStop.setOnClickListener(v -> {
            if (tts == null || !isTtsReady) {
                return;
            }
            cancelAllUtterances();
            tts.stop();
            if (isSavingAudio) {
                if (tempAudioFile != null && tempAudioFile.exists() && !tempAudioFile.delete()) {
                    Log.w("MainActivity", "临时音频文件删除失败: " + tempAudioFile.getAbsolutePath());
                }
                isSavingAudio = false;
                btnCancelSave.setEnabled(false);
            }
            setTtsWorkState(TtsSpeakStatusHelper.WorkState.STOPPED_BY_USER);
            updateSpeakAndSaveButtons();
        });

        // SAF文件选择器回调
        registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null && pendingAudioText != null) {
                            synthesizeTextToUri(pendingAudioText, uri);
                        }
                    }
                    pendingAudioText = null;
                });
        btnSaveAudio.setOnClickListener(v -> {
            if (!isTtsReady) {
                ToastHelper.showShort(this, R.string.status_not_ready);
                return;
            }
            // 不再输出TTS信息
            String text = editText.getText().toString().trim();
            if (text.isEmpty()) {
                ToastHelper.showShort(this, R.string.hint_input_text);
                return;
            }
            if (saveDirUri == null) {
                ToastHelper.showShort(this, R.string.toast_no_save_dir);
                return;
            }
            if (isSavingAudio) {
                ToastHelper.showShort(this, R.string.toast_saving_audio);
                return;
            }
            btnSpeak.setEnabled(false);
            if (TextLengthHelper.isTextTooLong(text)) {
                int maxLength = TextLengthHelper.getMaxTextLength();
                String message = getString(R.string.toast_text_exceeds_limit, maxLength);
                ToastHelper.showShort(this, message);
                return;
            }
            showFileNameInputDialogAndSave(text);
        });
        Button btnSetSaveDir = findViewById(R.id.btnSetSaveDir);
        btnCancelSave = findViewById(R.id.btnCancelSave);
        btnCancelSave.setEnabled(false);
        
        btnSetTtsInfoDir = findViewById(R.id.btnSetTtsInfoDir);
        btnOutputTtsInfoNow = findViewById(R.id.btnOutputTtsInfoNow);
        btnFillTestText = findViewById(R.id.btnFillTestText);
        btnFillTestText.setOnClickListener(v -> fillTestTextFromDebugSample());
        updateTtsInfoDirButtonVisibility();
        updateFillTestTextButtonVisibility();
        if (TestManager.getInstance().isTestMode()) {
            invalidateOptionsMenu();
        }
        
        // 读取保存目录Uri
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String uriStr = prefs.getString(KEY_SAVE_DIR_URI, null);
        if (uriStr != null) {
            saveDirUri = Uri.parse(uriStr);
        }
        
        // 读取TTS信息目录Uri
        String ttsInfoUriStr = prefs.getString("tts_info_dir_uri", null);
        if (ttsInfoUriStr != null) {
            ttsInfoDirUri = Uri.parse(ttsInfoUriStr);
        }
        
        btnSetSaveDir.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    | Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            openDirLauncher.launch(intent);
        });
        
        btnSetTtsInfoDir.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    | Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            openTtsInfoDirLauncher.launch(intent);
        });
        
        btnOutputTtsInfoNow.setOnClickListener(v -> exportTtsEngineDebugInfo());
        
        btnCancelSave.setOnClickListener(v -> confirmCancelSave());

        textSpeechRateValue = findViewById(R.id.textSpeechRateValue);
        textPitchValue = findViewById(R.id.textPitchValue);
        View btnSpeedMinus = findViewById(R.id.btnSpeedMinus);
        View btnSpeedPlus = findViewById(R.id.btnSpeedPlus);
        View btnPitchMinus = findViewById(R.id.btnPitchMinus);
        View btnPitchPlus = findViewById(R.id.btnPitchPlus);
        btnSpeedReset = findViewById(R.id.btnSpeedReset);
        btnPitchReset = findViewById(R.id.btnPitchReset);

        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        spinnerVoice = findViewById(R.id.spinnerVoice);
        btnLangVoiceReset = findViewById(R.id.btnLangVoiceReset);

        // 设置信息图标
        setupInfoIcons();
        setupInfoIconPositions();

        ttsStatusRunnable = new Runnable() {
            @Override
            public void run() {
                refreshTtsSpeakStatusDisplay();
                updateSpeakAndSaveButtons();
                if (TtsSpeakStatusHelper.needsStatusPolling(ttsWorkState, isSavingAudio)) {
                    ttsStatusHandler.postDelayed(this, Constants.TTS_STATUS_UPDATE_INTERVAL);
                }
            }
        };

        // 语速调节
        seekBarSpeed.setMax(Constants.SEEKBAR_MAX); // 0.5~2.0，步进0.1
        seekBarSpeed.setProgress(Constants.SEEKBAR_DEFAULT_PROGRESS); // 默认1.0
        // 在语速相关变化后调用
        SeekBarHelper.setSeekBarListener(seekBarSpeed,
                (progress) -> {
                    float value = 0.5f + progress * 0.1f;
                    value = Math.round(value * 10f) / 10f; // 保留一位小数
                    textSpeechRateValue.setText(String.format(Locale.US, "%.2f", value));
                    speechRate = value;
                    updateSpeedPitchResetButtons();
                });
        btnSpeedMinus.setOnClickListener(v -> {
            float value = Float.parseFloat(textSpeechRateValue.getText().toString());
            value -= Constants.SPEECH_RATE_STEP;
            if (value < Constants.SPEECH_RATE_MIN) {
                value = Constants.SPEECH_RATE_MIN;
            }
            value = Math.round(value * 100f) / 100f; // 保留两位小数
            textSpeechRateValue.setText(String.format(Locale.US, "%.2f", value));
            speechRate = value;
            int progress = Math.round((value - Constants.SPEECH_RATE_MIN) / 0.1f);
            seekBarSpeed.setProgress(progress);
            if (tts != null && isTtsReady) {
                tts.setSpeechRate(speechRate);
            }
            updateSpeedPitchResetButtons();
        });
        btnSpeedPlus.setOnClickListener(v -> {
            float value = Float.parseFloat(textSpeechRateValue.getText().toString());
            value += Constants.SPEECH_RATE_STEP;
            if (value > Constants.SPEECH_RATE_MAX) {
                value = Constants.SPEECH_RATE_MAX;
            }
            value = Math.round(value * 100f) / 100f;
            textSpeechRateValue.setText(String.format(Locale.US, "%.2f", value));
            speechRate = value;
            int progress = Math.round((value - Constants.SPEECH_RATE_MIN) / 0.1f);
            seekBarSpeed.setProgress(progress);
            if (tts != null && isTtsReady) {
                tts.setSpeechRate(speechRate);
            }
            updateSpeedPitchResetButtons();
        });
        btnSpeedReset.setOnClickListener(v -> {
            seekBarSpeed.setProgress(Constants.SEEKBAR_DEFAULT_PROGRESS);
            textSpeechRateValue.setText(getString(R.string.default_speed_value));
            speechRate = Constants.SPEECH_RATE_DEFAULT;
            if (tts != null && isTtsReady) {
                tts.setSpeechRate(speechRate);
            }
            updateSpeedPitchResetButtons();
        });

        // 音调调节
        seekBarPitch.setMax(Constants.SEEKBAR_MAX);
        seekBarPitch.setProgress(Constants.SEEKBAR_DEFAULT_PROGRESS);
        // 在音调相关变化后调用
        SeekBarHelper.setSeekBarListener(seekBarPitch,
                (progress) -> {
                    float value = 0.5f + progress * 0.1f;
                    value = Math.round(value * 10f) / 10f; // 保留一位小数
                    textPitchValue.setText(String.format(Locale.US, "%.2f", value));
                    pitch = value;
                    updateSpeedPitchResetButtons();
                });
        btnPitchMinus.setOnClickListener(v -> {
            float value = Float.parseFloat(textPitchValue.getText().toString());
            value -= Constants.PITCH_STEP;
            if (value < Constants.PITCH_MIN) {
                value = Constants.PITCH_MIN;
            }
            value = Math.round(value * 100f) / 100f;
            textPitchValue.setText(String.format(Locale.US, "%.2f", value));
            pitch = value;
            int progress = Math.round((value - Constants.PITCH_MIN) / 0.1f);
            seekBarPitch.setProgress(progress);
            if (tts != null && isTtsReady) {
                tts.setPitch(pitch);
            }
            updateSpeedPitchResetButtons();
        });
        btnPitchPlus.setOnClickListener(v -> {
            float value = Float.parseFloat(textPitchValue.getText().toString());
            value += Constants.PITCH_STEP;
            if (value > Constants.PITCH_MAX) {
                value = Constants.PITCH_MAX;
            }
            value = Math.round(value * 100f) / 100f;
            textPitchValue.setText(String.format(Locale.US, "%.2f", value));
            pitch = value;
            int progress = Math.round((value - Constants.PITCH_MIN) / 0.1f);
            seekBarPitch.setProgress(progress);
            if (tts != null && isTtsReady) {
                tts.setPitch(pitch);
            }
            updateSpeedPitchResetButtons();
        });
        btnPitchReset.setOnClickListener(v -> {
            seekBarPitch.setProgress(Constants.SEEKBAR_DEFAULT_PROGRESS);
            textPitchValue.setText(getString(R.string.default_pitch_value));
            pitch = Constants.PITCH_DEFAULT;
            if (tts != null && isTtsReady) {
                tts.setPitch(pitch);
            }
            updateSpeedPitchResetButtons();
        });

        // 语言和发音人设置
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
                if (!isLangSpinnerInit) {
                    return;
                }
                Locale selected = localeList.get(position);
                currentLocale = selected;
                if (parent.getAdapter() instanceof LanguageAdapter languageAdapter) {
                    languageAdapter.setSelectedPosition(position);
                }
                // 延后 TTS 与发音人列表更新，避免与语言下拉收起动画争抢主线程
                parent.post(() -> {
                    if (!isLangSpinnerInit || tts == null) {
                        return;
                    }
                    tts.setLanguage(selected);
                    updateVoiceList(selected, false);
                    updateLangVoiceResetButton();
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerVoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
                if (!isVoiceSpinnerInit || suppressVoiceSpinnerCallback) {
                    return;
                }
                Voice selected = voiceList.get(position);
                applyTtsVoice(selected);
                updateLangVoiceResetButton();
                if (parent.getAdapter() instanceof VoiceAdapter voiceAdapter) {
                    voiceAdapter.setSelectedPosition(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        btnLangVoiceReset.setOnClickListener(v -> {
            if (defaultLocale != null) {
                int idx = localeList.indexOf(defaultLocale);
                if (idx >= 0) {
                    spinnerLanguage.setSelection(idx);
                }
                tts.setLanguage(defaultLocale);
                updateVoiceList(defaultLocale, true);
                updateLangVoiceResetButton();
            }
        });

        tts = new TextToSpeech(this, status -> {

            if (status == TextToSpeech.SUCCESS) {
                // 获取默认语言和全局默认发音人
                // API 21+ 使用 getDefaultVoice().getLocale() 获取默认语言
                Voice defaultVoice = tts.getDefaultVoice();
                if (defaultVoice != null) {
                    defaultLocale = defaultVoice.getLocale();
                    this.globalDefaultVoice = defaultVoice;
                } else {
                    // 如果获取不到默认发音人，使用系统默认语言
                    defaultLocale = Locale.getDefault();
                }
                currentLocale = defaultLocale; // 使用默认语言作为当前语言

                tts.setLanguage(currentLocale);
                tts.setSpeechRate(speechRate);
                tts.setPitch(pitch);
                setupTtsProgressListener();
                isTtsReady = true;
                btnSpeak.setEnabled(true);
                btnStop.setEnabled(true);
                btnSaveAudio.setEnabled(true);
                tvTtsEngineStatus.setText(getString(R.string.status_ready));
                // 更新状态信息显示区域，包含系统语言提示
                updateStatusInfo();

                loadTtsCatalogAndApplyUi(null);
            } else {
                btnSpeak.setEnabled(false);
                btnStop.setEnabled(false);
                btnSaveAudio.setEnabled(false);
                updateStatusInfo();
            }
        });

        btnSpeak.setOnClickListener(v -> {
            if (!isTtsReady) {
                ToastHelper.showShort(this, R.string.status_not_ready);
                return;
            }
            String text = editText.getText().toString().trim();
            if (text.isEmpty()) {
                ToastHelper.showShort(this, R.string.hint_input_text);
                return;
            }
            btnSaveAudio.setEnabled(false);
            if (TextLengthHelper.isTextTooLong(text)) {
                int maxLength = TextLengthHelper.getMaxTextLength();
                String message = getString(R.string.toast_text_exceeds_limit, maxLength);
                ToastHelper.showShort(this, message);
                return;
            }
            ToastHelper.showShort(this, R.string.toast_read_task_submitted);
            beginUtterance(TtsSpeakStatusHelper.UTTERANCE_SPEAK);
            setTtsWorkState(TtsSpeakStatusHelper.WorkState.PREPARING_SPEAK);
            tts.setSpeechRate(speechRate);
            tts.setPitch(pitch);
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, TtsSpeakStatusHelper.UTTERANCE_SPEAK);
            int result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, TtsSpeakStatusHelper.UTTERANCE_SPEAK);
            checkSpeakSubmitResult(result);
        });

        editorLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String text = result.getData().getStringExtra(TextEditorActivity.EXTRA_TEXT);
                        if (text != null) {
                            editText.setText(text);
                        }
                    }
                });

        Button btnOpenEditor = findViewById(R.id.btnOpenEditor);
        btnOpenEditor.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TextEditorActivity.class);
            intent.putExtra(TextEditorActivity.EXTRA_TEXT, editText.getText().toString());
            editorLauncher.launch(intent);
        });
        // 输入框内容变化时动态启用/禁用清空按钮
        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {
                btnClear.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
        // 初始化时也判断一次
        btnClear.setEnabled(!editText.getText().toString().isEmpty());
        // 初始化时也调用一次
        updateResetButtons();
    }

    // endregion

    // endregion

    // region 生命周期

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        final int action = event.getActionMasked();
        boolean shouldScheduleClearFocus = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                dismissFocusTouchStartX = event.getRawX();
                dismissFocusTouchStartY = event.getRawY();
                dismissFocusTouchTracking = true;
                dismissFocusTouchStartedInEditText = isTouchInsideEditText(event);
                focusDismissScheduledForGesture = false;
                if (editText != null && editText.hasFocus() && !dismissFocusTouchStartedInEditText) {
                    shouldScheduleClearFocus = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (dismissFocusTouchTracking && editText != null && editText.hasFocus()
                        && !dismissFocusTouchStartedInEditText) {
                    float deltaX = Math.abs(event.getRawX() - dismissFocusTouchStartX);
                    float deltaY = Math.abs(event.getRawY() - dismissFocusTouchStartY);
                    if (deltaX > Constants.SCROLL_THRESHOLD || deltaY > Constants.SCROLL_THRESHOLD) {
                        shouldScheduleClearFocus = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (dismissFocusTouchTracking && editText != null && editText.hasFocus()
                        && !dismissFocusTouchStartedInEditText) {
                    float deltaX = Math.abs(event.getRawX() - dismissFocusTouchStartX);
                    float deltaY = Math.abs(event.getRawY() - dismissFocusTouchStartY);
                    if (deltaX <= Constants.SCROLL_THRESHOLD && deltaY <= Constants.SCROLL_THRESHOLD) {
                        shouldScheduleClearFocus = true;
                    }
                }
                dismissFocusTouchTracking = false;
                dismissFocusTouchStartedInEditText = false;
                break;
            default:
                break;
        }

        boolean handled = super.dispatchTouchEvent(event);

        if (shouldScheduleClearFocus) {
            scheduleClearEditFocusIfNeeded();
        }
        return handled;
    }

    /**
     * 在触摸分发完成后再失焦，避免同步 clearFocus 打断分发导致 SeekBar 等控件误高亮。
     */
    private void scheduleClearEditFocusIfNeeded() {
        if (editText == null || !editText.hasFocus() || focusDismissScheduledForGesture) {
            return;
        }
        focusDismissScheduledForGesture = true;
        editText.post(() -> {
            focusDismissScheduledForGesture = false;
            clearEditTextFocusAndHideKeyboard();
        });
    }

    private boolean isTouchInsideEditText(@NonNull MotionEvent event) {
        if (editText == null) {
            return false;
        }
        int[] location = new int[2];
        editText.getLocationOnScreen(location);
        float x = event.getRawX();
        float y = event.getRawY();
        return x >= location[0]
                && x < location[0] + editText.getWidth()
                && y >= location[1]
                && y < location[1] + editText.getHeight();
    }

    private void clearEditTextFocusAndHideKeyboard() {
        if (editText == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && editText.getWindowToken() != null) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
        if (editText.hasFocus()) {
            editText.clearFocus();
        }
        // 触摸模式下须临时允许目标 View 获取焦点，否则 clearFocus 后焦点会留在 EditText
        if (focusSink != null) {
            boolean sinkWasFocusableInTouchMode = focusSink.isFocusableInTouchMode();
            focusSink.setFocusableInTouchMode(true);
            focusSink.requestFocus();
            focusSink.setFocusableInTouchMode(sinkWasFocusableInTouchMode);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshTtsSpeakStatusDisplay();
        updateSpeakAndSaveButtons();
        if (TtsSpeakStatusHelper.needsStatusPolling(ttsWorkState, isSavingAudio)) {
            ensureTtsStatusPolling();
        }
    }

    @Override
    protected void onStop() {
        if (ttsStatusRunnable != null) {
            ttsStatusHandler.removeCallbacks(ttsStatusRunnable);
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 检测TTS引擎是否发生变化，如果发生变化则重新初始化
        checkAndHandleTtsEngineChange();
        SystemBarsHelper.reapply(this);
    }

    // endregion

    // region 生命周期方法

    @Override
    protected void onDestroy() {
        ToastHelper.cancel();
        ttsStatusHandler.removeCallbacksAndMessages(null);
        if (layoutCurrentVoiceDebug != null) {
            layoutCurrentVoiceDebug.removeCallbacks(currentVoiceDebugBlockRunnable);
        }
        ttsCatalogExecutor.shutdownNow();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem exitDebugModeItem = menu.findItem(R.id.action_exit_debug_mode);
        bindExitDebugModeMenuItem(exitDebugModeItem);
        scheduleBindDebugModeTitleTap();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {
        bindExitDebugModeMenuItem(menu.findItem(R.id.action_exit_debug_mode));
        return super.onPrepareOptionsMenu(menu);
    }

    private void bindExitDebugModeMenuItem(@Nullable MenuItem exitDebugModeItem) {
        if (exitDebugModeItem == null) {
            return;
        }
        boolean inDebugMode = TestManager.getInstance().isTestMode();
        exitDebugModeItem.setVisible(inDebugMode);
        exitDebugModeItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        if (inDebugMode) {
            DebugModeUi.applyExitDebugModeMenuItemStyle(this, exitDebugModeItem);
        } else {
            exitDebugModeItem.setTitle(R.string.test_mode_exit);
        }
    }

    // endregion

    // region 菜单相关方法

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_exit_debug_mode) {
            exitDebugMode();
            return true;
        } else if (id == R.id.action_tts_settings) {
            try {
                Intent intent = new Intent("com.android.settings.TTS_SETTINGS");
                startActivity(intent);
            } catch (Exception e) {
                ToastHelper.showShort(this, R.string.toast_cannot_open_tts_settings);
            }
            return true;
        } else if (id == R.id.action_tts_browser) {
            TtsBrowserActivity.start(this);
            return true;
        } else if (id == R.id.action_language) {
            showLanguageSelectionDialog();
            return true;

        } else if (id == R.id.action_about) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_about)
                    .setMessage(R.string.dialog_message_about)
                    .setPositiveButton(R.string.dialog_button_ok, null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateToolbarTitle() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        boolean debugMode = TestManager.getInstance().isTestMode();
        String title = getString(R.string.app_name);
        if (debugMode) {
            title += getString(R.string.test_mode_toolbar_suffix);
        }
        toolbar.setTitle(title);
        scheduleBindDebugModeTitleTap();
    }

    private void applyMainToolbarTitleAppearance(@NonNull Toolbar toolbar, boolean debugMode) {
        toolbar.setTitleTextColor(debugMode
                ? DebugModeUi.accentColor(this)
                : ContextCompat.getColor(this, R.color.pure_white));
        TextView titleView = findToolbarTitleTextView(toolbar);
        if (titleView == null) {
            return;
        }
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.toolbar_title_text_size));
        TextViewCompat.setAutoSizeTextTypeWithDefaults(titleView,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
        titleView.setSingleLine(true);
        titleView.setEllipsize(TextUtils.TruncateAt.END);
    }

    @Nullable
    private TextView findToolbarTitleTextView(@NonNull Toolbar toolbar) {
        View titleView = toolbar.findViewById(androidx.appcompat.R.id.action_bar_title);
        if (titleView instanceof TextView textView) {
            return textView;
        }
        String appName = getString(R.string.app_name);
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof TextView textView) {
                CharSequence text = textView.getText();
                if (text != null && text.toString().startsWith(appName)) {
                    return textView;
                }
            }
        }
        return null;
    }

    private void scheduleBindDebugModeTitleTap() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }
        boolean debugMode = TestManager.getInstance().isTestMode();
        toolbar.post(() -> {
            applyMainToolbarTitleAppearance(toolbar, debugMode);
            bindDebugModeTitleTapTarget(toolbar);
        });
    }

    /**
     * 为 Toolbar 标题绑定连续点击入口（连续点击应用名称区域 7 次打开调试面板；调试模式开启时同样有效）。
     */
    private void bindDebugModeTitleTapTarget(@NonNull Toolbar toolbar) {
        TextView titleView = findToolbarTitleTextView(toolbar);
        if (titleView != null) {
            titleView.setClickable(true);
            titleView.setFocusable(true);
            titleView.setOnClickListener(this::onDebugModeTitleTap);
        }
    }

    private void onDebugModeTitleTap(@NonNull View titleView) {
        DebugModeUi.playTitleTapFeedback(titleView);
        handleDebugModeTitleTap();
    }

    private void handleDebugModeTitleTap() {
        long now = System.currentTimeMillis();
        if (now - lastDebugModeTitleTapAt > DEBUG_MODE_TAP_INTERVAL_MS) {
            debugModeTitleTapCount = 0;
            ToastHelper.cancel();
        }
        lastDebugModeTitleTapAt = now;
        debugModeTitleTapCount++;

        if (debugModeTitleTapCount >= DEBUG_MODE_REQUIRED_TAPS) {
            debugModeTitleTapCount = 0;
            ToastHelper.cancel();
            openDebugModeDialog();
            return;
        }

        if (debugModeTitleTapCount >= DEBUG_MODE_TAP_HINT_FROM) {
            int remaining = DEBUG_MODE_REQUIRED_TAPS - debugModeTitleTapCount;
            ToastHelper.showOrUpdateShort(this,
                    getString(R.string.debug_mode_tap_remaining, remaining));
        }
    }

    private void exitDebugMode() {
        TestManager.getInstance().resetAll();
        ToastHelper.cancel();
        updateToolbarTitle();
        updateStatusInfo();
        updateTtsInfoDirButtonVisibility();
        updateFillTestTextButtonVisibility();
        invalidateOptionsMenu();
        scheduleBindDebugModeTitleTap();
        ToastHelper.showShort(this, R.string.test_mode_exit_toast);
    }

    private void openDebugModeDialog() {
        boolean wasInDebugMode = TestManager.getInstance().isTestMode();
        TestModeDialog dialog = new TestModeDialog(this, TestManager.getInstance().getTestCases(), selected -> {
            boolean anySelected = false;
            for (TestCase tc : selected) {
                if (tc.selected) {
                    anySelected = true;
                    break;
                }
            }
            if (anySelected) {
                TestManager.getInstance().setTestMode(true);
                ToastHelper.showShort(this, R.string.test_mode_title);
            } else {
                TestManager.getInstance().resetAll();
                ToastHelper.showShort(this, wasInDebugMode
                        ? R.string.test_mode_exit_toast
                        : R.string.test_mode_btn_cancel);
            }
            ToastHelper.cancel();
            updateToolbarTitle();
            updateStatusInfo();
            updateTtsInfoDirButtonVisibility();
            updateFillTestTextButtonVisibility();
            invalidateOptionsMenu();
            scheduleBindDebugModeTitleTap();
        });
        dialog.show();
    }

    private boolean isDebugItemSelected(@NonNull String id) {
        if (!TestManager.getInstance().isTestMode()) {
            return false;
        }
        for (TestCase tc : TestManager.getInstance().getSelectedTestCases()) {
            if (tc.selected && id.equals(tc.id)) {
                return true;
            }
        }
        return false;
    }

    private void fillTestTextFromDebugSample() {
        String sample = getString(R.string.debug_test_text_sample);
        int maxLength = TextLengthHelper.getMaxTextLength();
        if (sample.length() > maxLength) {
            sample = sample.substring(0, maxLength);
        }
        editText.setText(sample);
        editText.setSelection(sample.length());
        btnClear.setEnabled(!sample.isEmpty());
        ToastHelper.showShort(this, R.string.toast_test_text_filled);
    }

    // endregion

    // region 公开方法

    /**
     * 将文本合成为音频文件并保存到指定URI
     *
     * @param text 要合成的文本
     * @param uri  保存音频的目标URI
     */
    private void synthesizeTextToUri(@NonNull String text, @NonNull Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11+
            try {
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
                if (pfd != null) {
                    Bundle params = new Bundle();
                    params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
                    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_save");
                    tts.setLanguage(currentLocale);
                    tts.setSpeechRate(speechRate);
                    tts.setPitch(pitch);
                    tts.synthesizeToFile(text, params, pfd, "tts_save");
                    pfd.close();
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Exception", e);
                ToastHelper.showShort(this, R.string.message_save_failed, e.getMessage());
            }
        } else {
            File tempWav = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "tts_temp.wav");
            Bundle ttsParams = new Bundle();
            ttsParams.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
            ttsParams.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_save");
            tts.setLanguage(currentLocale);
            tts.setSpeechRate(speechRate);
            tts.setPitch(pitch);
            tts.synthesizeToFile(text, ttsParams, tempWav, "tts_save");
        }
    }

    // endregion

    // region TTS 相关方法

    /**
     * 根据选择的语言更新发音人列表
     *
     * @param locale         选择的语言
     * @param resetToDefault 是否重置为默认发音人
     */
    private void updateVoiceList(Locale locale, boolean resetToDefault) {
        voiceList.clear();

        List<Voice> voicesForLocale = cachedVoicesByLocale.get(locale);
        if (voicesForLocale != null) {
            voiceList.addAll(voicesForLocale);
        }

        // 获取当前语言的默认发音人
        Voice currentLangDefaultVoice = languageDefaultVoices.get(locale);

        // 使用工具类排序发音人（默认发音人排在最前面）
        List<Voice> sortedVoices = TtsLanguageVoiceHelper.sortVoicesByDefault(voiceList, currentLangDefaultVoice);
        voiceList.clear();
        voiceList.addAll(sortedVoices);

        // 重新创建适配器以更新默认发音人信息
        if (spinnerVoice.getAdapter() instanceof VoiceAdapter voiceAdapter) {
            voiceAdapter.refreshAfterVoiceListChange(currentLangDefaultVoice);
        } else {
            spinnerVoice.setAdapter(new VoiceAdapter(this, voiceList, currentLangDefaultVoice));
        }

        Voice voiceToSet = null;
        if (resetToDefault && currentLangDefaultVoice != null) {
            voiceToSet = currentLangDefaultVoice;
        } else if (!voiceList.isEmpty()) {
            voiceToSet = voiceList.get(0);
        }

        isVoiceSpinnerInit = true;
        suppressVoiceSpinnerCallback = true;
        try {
            if (voiceToSet != null) {
                spinnerVoice.setSelection(0);
                applyTtsVoice(voiceToSet);
            } else {
                currentVoice = null;
                scheduleCurrentVoiceDebugBlockUpdate();
            }
        } finally {
            suppressVoiceSpinnerCallback = false;
        }
    }

    private static final class TtsCatalogData {
        private final List<Locale> sortedLocales;
        private final Map<Locale, List<Voice>> voicesByLocale;
        private final List<String> languageDisplayNames;
        private final Map<Locale, Voice> languageDefaultVoices;

        private TtsCatalogData(@NonNull List<Locale> sortedLocales,
                                 @NonNull Map<Locale, List<Voice>> voicesByLocale,
                                 @NonNull List<String> languageDisplayNames,
                                 @NonNull Map<Locale, Voice> languageDefaultVoices) {
            this.sortedLocales = sortedLocales;
            this.voicesByLocale = voicesByLocale;
            this.languageDisplayNames = languageDisplayNames;
            this.languageDefaultVoices = languageDefaultVoices;
        }
    }

    private static final class TtsCatalogRawData {
        @NonNull
        final Set<Locale> locales;
        @NonNull
        final Set<Voice> voices;

        TtsCatalogRawData(@NonNull Set<Locale> locales, @NonNull Set<Voice> voices) {
            this.locales = locales;
            this.voices = voices;
        }
    }

    @NonNull
    private TtsCatalogData buildTtsCatalogFromRaw(@NonNull TtsCatalogRawData raw) {
        Map<Locale, List<Voice>> voicesByLocale = TtsLanguageVoiceHelper.indexVoicesByLocale(raw.voices);
        List<Locale> sortedLocales = TtsLanguageVoiceHelper.sortLocalesByDisplayName(
                raw.locales, defaultLocale, this, voicesByLocale);
        List<String> languageDisplayNames = TtsLanguageVoiceHelper.buildLanguageDisplayNames(
                this, sortedLocales, voicesByLocale, defaultLocale);
        Map<Locale, Voice> defaults = TtsLanguageVoiceHelper.determineLanguageDefaultVoices(
                TtsLanguageVoiceHelper.buildLanguageVoicesMap(raw.locales, raw.voices), globalDefaultVoice);
        return new TtsCatalogData(sortedLocales, voicesByLocale, languageDisplayNames, defaults);
    }

    private void applyTtsCatalog(@NonNull TtsCatalogData catalog) {
        cachedVoicesByLocale = catalog.voicesByLocale;
        languageDefaultVoices.clear();
        languageDefaultVoices.putAll(catalog.languageDefaultVoices);
        localeList.clear();
        localeList.addAll(catalog.sortedLocales);

        LanguageAdapter languageAdapter = new LanguageAdapter(
                this, localeList, catalog.languageDisplayNames, defaultLocale);
        spinnerLanguage.setAdapter(languageAdapter);
        isLangSpinnerInit = true;

        int languageIndex = currentLocale != null ? localeList.indexOf(currentLocale) : 0;
        if (languageIndex < 0) {
            languageIndex = 0;
        }
        spinnerLanguage.setSelection(languageIndex);
        languageAdapter.setSelectedPosition(languageIndex);

        Locale localeForVoices = localeList.isEmpty()
                ? currentLocale
                : localeList.get(languageIndex);
        if (localeForVoices != null) {
            updateVoiceList(localeForVoices, true);
        }
        refreshTtsEngineDisplayInfo();
    }

    private void loadTtsCatalogAndApplyUi(@Nullable Runnable afterApply) {
        ttsCatalogExecutor.execute(() -> {
            TtsCatalogRawData raw;
            try {
                if (tts == null) {
                    return;
                }
                Set<Locale> locales = tts.getAvailableLanguages();
                Set<Voice> voices = tts.getVoices();
                if (locales == null) {
                    locales = Collections.emptySet();
                }
                if (voices == null) {
                    voices = Collections.emptySet();
                }
                raw = new TtsCatalogRawData(locales, voices);
            } catch (Exception e) {
                Log.e("MainActivity", "Failed to load TTS catalog", e);
                return;
            }
            runOnUiThread(() -> {
                if (isFinishing() || tts == null) {
                    return;
                }
                applyTtsCatalog(buildTtsCatalogFromRaw(raw));
                if (afterApply != null) {
                    afterApply.run();
                }
            });
        });
    }

    private void finishRestoreUserSettingsAfterCatalog(@Nullable Locale savedLocale, @Nullable Voice savedVoice) {
        Locale targetLocale = savedLocale;
        if (targetLocale == null || !localeList.contains(targetLocale)) {
            targetLocale = defaultLocale;
        }
        currentLocale = targetLocale;

        int languageIndex = localeList.indexOf(targetLocale);
        if (languageIndex >= 0) {
            spinnerLanguage.setSelection(languageIndex);
            if (spinnerLanguage.getAdapter() instanceof LanguageAdapter languageAdapter) {
                languageAdapter.setSelectedPosition(languageIndex);
            }
        }

        tts.setLanguage(targetLocale);
        updateVoiceList(targetLocale, false);

        if (savedVoice != null && voiceList.contains(savedVoice)) {
            int voiceIndex = voiceList.indexOf(savedVoice);
            if (voiceIndex >= 0) {
                spinnerVoice.setSelection(voiceIndex);
                applyTtsVoice(savedVoice);
                if (spinnerVoice.getAdapter() instanceof VoiceAdapter voiceAdapter) {
                    voiceAdapter.setSelectedPosition(voiceIndex);
                }
            }
        }

        isTtsReady = true;
        btnSpeak.setEnabled(true);
        btnStop.setEnabled(true);
        btnSaveAudio.setEnabled(true);
        tvTtsEngineStatus.setText(getString(R.string.status_ready));
        refreshTtsEngineDisplayInfo();
        updateStatusInfo();
        updateResetButtons();
        Log.i("MainActivity", "TTS reinitialized successfully with restored settings");
    }

    /**
     * 更新界面状态信息显示
     * 包括TTS引擎状态、保存目录、朗读状态等
     */
    private void updateStatusInfo() {
        // TTS引擎状态
        if (isTtsReady) {
            tvTtsEngineStatus.setText(getString(R.string.status_ready));
        } else {
            tvTtsEngineStatus.setText(getString(R.string.status_not_ready));
        }
        // 音频保存目录
        if (saveDirUri != null) {
            bindSaveDirectoryDisplay(layoutAudioSaveDirBlock, tvAudioSaveDirLabel, layoutAudioSaveDirValueRow,
                    tvAudioSaveDir, btnCopySaveDir, getReadablePathFromUri(saveDirUri));
        } else {
            bindSaveDirectoryDisplay(layoutAudioSaveDirBlock, tvAudioSaveDirLabel, layoutAudioSaveDirValueRow,
                    tvAudioSaveDir, btnCopySaveDir, getString(R.string.not_set));
        }
        
        // TTS信息保存目录（仅调试模式下显示）
        if (TestManager.getInstance().isTestMode()) {
            boolean showTtsInfoDir = isDebugItemSelected("log_tts_voices");

            if (showTtsInfoDir) {
                layoutTtsInfoDir.setVisibility(View.VISIBLE);
                String ttsInfoDirPath = getTtsInfoSaveDirDisplayPath();
                bindSaveDirectoryDisplay(layoutTtsInfoDir, tvTtsInfoSaveDirLabel, layoutTtsInfoSaveDirValueRow,
                        tvTtsInfoSaveDir, btnCopyTtsInfoDir, ttsInfoDirPath);
                DebugModeUi.applyAccentText(tvTtsInfoSaveDirLabel, tvTtsInfoSaveDir);
            } else {
                layoutTtsInfoDir.setVisibility(View.GONE);
            }
        } else {
            layoutTtsInfoDir.setVisibility(View.GONE);
        }

        updateCurrentVoiceDebugBlock();

        if (cachedTtsEngineDisplayName == null) {
            refreshTtsEngineDisplayInfo();
        }
        if (tvTtsEngineInfo != null) {
            tvTtsEngineInfo.setText(cachedTtsEngineDisplayName != null
                    ? cachedTtsEngineDisplayName
                    : getString(R.string.tts_engine_unknown));
        }
        refreshTtsSpeakStatusDisplay();
        if (tvSelectedTestCases != null && layoutSelectedTestCases != null) {
            if (TestManager.getInstance().isTestMode()) {
                List<String> selected = TestManager.getInstance().getSelectedTestCases().stream().map(tc -> tc.name)
                        .collect(java.util.stream.Collectors.toList());
                if (selected.isEmpty()) {
                    tvSelectedTestCases.setText(getString(R.string.none));
                } else {
                    tvSelectedTestCases.setText(android.text.TextUtils.join("、", selected));
                }
                layoutSelectedTestCases.setVisibility(View.VISIBLE);
            } else {
                tvSelectedTestCases.setText("");
                layoutSelectedTestCases.setVisibility(View.GONE);
            }
        }
        updateStatusDebugSectionVisibility();
    }

    private void updateStatusDebugSectionVisibility() {
        if (layoutStatusDebugSection == null) {
            return;
        }
        boolean anyChildVisible = (layoutTtsInfoDir != null && layoutTtsInfoDir.getVisibility() == View.VISIBLE)
                || (layoutCurrentVoiceDebug != null && layoutCurrentVoiceDebug.getVisibility() == View.VISIBLE)
                || (layoutSelectedTestCases != null && layoutSelectedTestCases.getVisibility() == View.VISIBLE);
        layoutStatusDebugSection.setVisibility(anyChildVisible ? View.VISIBLE : View.GONE);
    }

    private final Runnable currentVoiceDebugBlockRunnable = this::applyCurrentVoiceDebugBlock;

    private void scheduleCurrentVoiceDebugBlockUpdate() {
        if (layoutCurrentVoiceDebug == null) {
            return;
        }
        if (!TestManager.getInstance().isTestMode() || !isDebugItemSelected("show_current_voice")) {
            return;
        }
        layoutCurrentVoiceDebug.removeCallbacks(currentVoiceDebugBlockRunnable);
        layoutCurrentVoiceDebug.post(currentVoiceDebugBlockRunnable);
    }

    private void updateCurrentVoiceDebugBlock() {
        applyCurrentVoiceDebugBlock();
    }

    private void applyCurrentVoiceDebugBlock() {
        if (layoutCurrentVoiceDebug == null || tvCurrentVoiceDebug == null) {
            return;
        }
        if (!isDebugItemSelected("show_current_voice")) {
            layoutCurrentVoiceDebug.setVisibility(View.GONE);
            updateStatusDebugSectionVisibility();
            return;
        }
        layoutCurrentVoiceDebug.setVisibility(View.VISIBLE);
        DebugModeUi.applyAccentText(tvCurrentVoiceDebugLabel, tvCurrentVoiceDebug);
        if (!isTtsReady || tts == null) {
            tvCurrentVoiceDebug.setText(R.string.voice_debug_tts_not_ready);
            if (btnCopyCurrentVoice != null) {
                btnCopyCurrentVoice.setVisibility(View.GONE);
            }
            return;
        }
        Voice voice = currentVoice;
        if (voice == null && tts != null) {
            voice = tts.getVoice();
            currentVoice = voice;
        }
        if (voice == null) {
            tvCurrentVoiceDebug.setText(R.string.voice_debug_get_voice_null);
            if (btnCopyCurrentVoice != null) {
                btnCopyCurrentVoice.setVisibility(View.GONE);
            }
            return;
        }
        tvCurrentVoiceDebug.setText(voice.toString());
        if (btnCopyCurrentVoice != null) {
            btnCopyCurrentVoice.setVisibility(View.VISIBLE);
        }
        updateStatusDebugSectionVisibility();
    }

    /**
     * 更新TTS信息目录按钮的可见性
     * 仅在启用「输出 TTS 引擎信息」调试项时显示
     */
    private void updateTtsInfoDirButtonVisibility() {
        if (btnSetTtsInfoDir == null || btnOutputTtsInfoNow == null) {
            return;
        }
        
        boolean showTtsInfoDirButton = isDebugItemSelected("log_tts_voices");

        btnSetTtsInfoDir.setVisibility(showTtsInfoDirButton ? View.VISIBLE : View.GONE);
        btnOutputTtsInfoNow.setVisibility(showTtsInfoDirButton ? View.VISIBLE : View.GONE);
        if (showTtsInfoDirButton) {
            DebugModeUi.styleDebugButton(btnSetTtsInfoDir);
            DebugModeUi.styleDebugButton(btnOutputTtsInfoNow);
        }
    }

    /**
     * 更新「一键填入测试文本」按钮的可见性
     * 仅在启用对应调试项时显示
     */
    private void updateFillTestTextButtonVisibility() {
        if (btnFillTestText == null) {
            return;
        }
        boolean show = isDebugItemSelected("fill_test_text");
        btnFillTestText.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            DebugModeUi.styleDebugButton(btnFillTestText);
        }
    }

    // endregion

    // region UI 相关方法

    private void showFileNameInputDialogAndSave(String text) {
        // 自动生成默认文件名
        String defaultName = "tts_"
                + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new java.util.Date());

        DialogHelper.showInputDialog(this, R.string.input_audio_filename, defaultName,
                name -> {
                    if (name.isEmpty()) {
                        name = defaultName;
                    }
                    if (!name.endsWith(".wav")) {
                        name += ".wav";
                    }
                    currentAudioFileName = name;
                    ToastHelper.showShort(this, R.string.toast_save_task_submitted);
                    startSaveAudio(text);
                }, null);
    }

    private void startSaveAudio(String text) {
        isSavingAudio = true;
        btnSaveAudio.setEnabled(false);
        btnCancelSave.setEnabled(true);
        tempAudioFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), currentAudioFileName);
        beginUtterance(TtsSpeakStatusHelper.UTTERANCE_SAVE);
        setTtsWorkState(TtsSpeakStatusHelper.WorkState.PREPARING_SAVE);
        Bundle ttsParams = new Bundle();
        ttsParams.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
        ttsParams.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, TtsSpeakStatusHelper.UTTERANCE_SAVE);
        tts.setLanguage(currentLocale);
        tts.setSpeechRate(speechRate);
        tts.setPitch(pitch);
        int result = tts.synthesizeToFile(text, ttsParams, tempAudioFile, TtsSpeakStatusHelper.UTTERANCE_SAVE);
        if (!checkSaveSubmitResult(result)) {
            ToastHelper.showShort(this, R.string.toast_save_audio_synth_fail);
        }
    }

    // endregion

    // region 文件操作相关方法

    // 在TTS合成完成回调onDone/onError中处理拷贝和清理
    private boolean copyTempToSaveDir() {
        try {
            DocumentFile dir = DocumentFile.fromTreeUri(getApplicationContext(), saveDirUri);
            if (dir == null || !dir.canWrite()) {
                return false;
            }
            // 先删除同名文件
            DocumentFile old = dir.findFile(currentAudioFileName);
            if (old != null) {
                boolean deleted = old.delete();
                if (!deleted) {
                    Log.w("MainActivity", "旧音频文件删除失败: " + old.getUri());
                }
            }
            DocumentFile newFile = dir.createFile("audio/wav", currentAudioFileName);
            if (newFile == null) {
                return false;
            }
            try (OutputStream os = getContentResolver().openOutputStream(newFile.getUri());
                 FileInputStream fis = new FileInputStream(tempAudioFile)) {
                if (os == null) {
                    return false;
                }
                byte[] buf = new byte[Constants.FILE_BUFFER_SIZE];
                int len;
                while ((len = fis.read(buf)) > 0) {
                    os.write(buf, 0, len);
                }
                os.flush();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void confirmCancelSave() {
        DialogHelper.showConfirmCancelDialog(this, R.string.dialog_message_cancel_save, this::cancelSaveAudio);
    }

    private void cancelSaveAudio() {
        if (isSavingAudio) {
            cancelAllUtterances();
            if (tts != null) {
                tts.stop();
            }
            if (tempAudioFile != null && tempAudioFile.exists()) {
                boolean deleted = tempAudioFile.delete();
                if (!deleted) {
                    Log.w("MainActivity", "临时音频文件删除失败: " + tempAudioFile.getAbsolutePath());
                }
            }
            if (saveDirUri != null) {
                DocumentFile dir = DocumentFile.fromTreeUri(this, saveDirUri);
                if (dir != null) {
                    DocumentFile file = dir.findFile(currentAudioFileName);
                    if (file != null) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            Log.w("MainActivity", "音频文件删除失败: " + file.getUri());
                        }
                    }
                }
            }
            isSavingAudio = false;
            btnSaveAudio.setEnabled(true);
            btnCancelSave.setEnabled(false);
            updateSpeakAndSaveButtons();
            cancelAllUtterances();
            ToastHelper.showShort(this, R.string.toast_cancel_save_success);
            setTtsWorkState(TtsSpeakStatusHelper.WorkState.IDLE);
            updateStatusInfo();
        }
    }

    private void updateSpeedPitchResetButtons() {
        btnSpeedReset.setEnabled(Math.abs(speechRate - 1.0f) > 0.001f);
        btnPitchReset.setEnabled(Math.abs(pitch - 1.0f) > 0.001f);
    }

    private void updateLangVoiceResetButton() {
        boolean isLangDefault = currentLocale != null && currentLocale.equals(defaultLocale);
        Voice defaultVoice = languageDefaultVoices.get(currentLocale);
        Voice voiceForCompare = resolveCurrentVoiceForUi();
        boolean isVoiceDefault = voiceForCompare != null && voiceForCompare.equals(defaultVoice);
        btnLangVoiceReset.setEnabled(!(isLangDefault && isVoiceDefault));
    }

    private void applyTtsVoice(@Nullable Voice voice) {
        currentVoice = voice;
        if (tts != null && voice != null) {
            tts.setVoice(voice);
        }
        scheduleCurrentVoiceDebugBlockUpdate();
    }

    @Nullable
    private Voice resolveCurrentVoiceForUi() {
        if (currentVoice != null) {
            return currentVoice;
        }
        if (isVoiceSpinnerInit && !voiceList.isEmpty()) {
            int pos = spinnerVoice.getSelectedItemPosition();
            if (pos >= 0 && pos < voiceList.size()) {
                return voiceList.get(pos);
            }
        }
        return null;
    }

    private void refreshTtsEngineDisplayInfo() {
        cachedTtsEngineDisplayName = TtsEngineHelper.getTtsEngineInfo(tts, this, ivTtsEngineIcon);
        if (tvTtsEngineInfo != null && cachedTtsEngineDisplayName != null) {
            tvTtsEngineInfo.setText(cachedTtsEngineDisplayName);
        }
    }

    private void ensureTtsStatusPolling() {
        if (ttsStatusRunnable != null) {
            ttsStatusHandler.removeCallbacks(ttsStatusRunnable);
            ttsStatusHandler.post(ttsStatusRunnable);
        }
    }

    private void updateResetButtons() {
        updateSpeedPitchResetButtons();
        updateLangVoiceResetButton();
    }

    // 新增：根据TTS状态和isSavingAudio更新朗读和保存按钮的可用性
    private void updateSpeakAndSaveButtons() {
        if (ttsWorkState == TtsSpeakStatusHelper.WorkState.SPEAKING) {
            btnSaveAudio.setEnabled(false);
            btnSpeak.setEnabled(true);
        } else if (TtsSpeakStatusHelper.blocksSpeakAndSave(ttsWorkState, isSavingAudio)) {
            btnSpeak.setEnabled(false);
            btnSaveAudio.setEnabled(false);
        } else {
            btnSpeak.setEnabled(isTtsReady);
            btnSaveAudio.setEnabled(isTtsReady);
        }
    }

    // endregion

    // region 工具方法

    // 将SAF Uri转为可读路径，仅主存储primary支持
    @NonNull
    private String getReadablePathFromUri(@Nullable Uri uri) {
        if (uri == null) {
            return "";
        }
        String uriStr = uri.toString();
        if (uriStr.startsWith("content://com.android.externalstorage.documents/tree/primary%3A")) {
            String subPath = uriStr.substring(uriStr.indexOf("%3A") + 3);
            try {
                // 解码URL编码的字符，特别是中文字符
                String decodedPath;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    decodedPath = java.net.URLDecoder.decode(subPath, StandardCharsets.UTF_8);
                } else {
                    decodedPath = java.net.URLDecoder.decode(subPath, StandardCharsets.UTF_8);
                }
                return "/storage/emulated/0/" + decodedPath.replace("%2F", "/");
            } catch (Exception e) {
                // 如果解码失败，返回原始路径
                return "/storage/emulated/0/" + subPath.replace("%2F", "/");
            }
        }
        return uriStr;
    }

    /**
     * 为所有按钮设置自动文本大小调整
     */
    private void setupAllButtonsAutoTextSize() {
        // 主功能按钮
        if (btnSpeak != null) {
            ButtonTextHelper.setupAutoTextSize(btnSpeak);
        }
        if (btnStop != null) {
            ButtonTextHelper.setupAutoTextSize(btnStop);
        }
        if (btnSaveAudio != null) {
            ButtonTextHelper.setupAutoTextSize(btnSaveAudio);
        }
        if (btnCancelSave != null) {
            ButtonTextHelper.setupAutoTextSize(btnCancelSave);
        }

        // 设置按钮
        Button btnSetSaveDir = findViewById(R.id.btnSetSaveDir);
        if (btnSetSaveDir != null) {
            ButtonTextHelper.setupAutoTextSize(btnSetSaveDir);
        }
        if (btnSpeedReset != null) {
            ButtonTextHelper.setupAutoTextSize(btnSpeedReset);
        }
        if (btnPitchReset != null) {
            ButtonTextHelper.setupAutoTextSize(btnPitchReset);
        }
        if (btnLangVoiceReset != null) {
            ButtonTextHelper.setupAutoTextSize(btnLangVoiceReset);
        }

        // 编辑器按钮
        Button btnOpenEditor = findViewById(R.id.btnOpenEditor);
        if (btnOpenEditor != null) {
            ButtonTextHelper.setupAutoTextSize(btnOpenEditor);
        }
    }

    /**
     * 设置所有信息图标
     */
    private void setupInfoIcons() {
        // 使用批量设置方法，更简洁高效
        InfoIconHelper.setupInfoIcons(this,
                new Object[]{findViewById(R.id.ivLangSupportInfo), R.string.tts_support_info_title,
                        R.string.tts_support_info_content},
                new Object[]{findViewById(R.id.ivVoiceSupportInfo), R.string.voice_info_title,
                        R.string.voice_info_content},
                new Object[]{findViewById(R.id.ivSpeedInfo), R.string.speed_info_title, R.string.speed_info_content},
                new Object[]{findViewById(R.id.ivPitchInfo), R.string.pitch_info_title,
                        R.string.pitch_info_content});
    }

    /**
     * 设置信息图标的动态位置（右上角上标对齐）
     */
    private void setupInfoIconPositions() {
        findViewById(android.R.id.content).post(() -> {
            InfoIconPositionHelper.setIconPosition(findViewById(R.id.ivSpeedInfo),
                    findViewById(R.id.tvSpeedLabel));
            InfoIconPositionHelper.setIconPosition(findViewById(R.id.ivPitchInfo),
                    findViewById(R.id.tvPitchLabel));
            InfoIconPositionHelper.setIconPosition(findViewById(R.id.ivLangSupportInfo),
                    findViewById(R.id.tvLanguageLabel));
            InfoIconPositionHelper.setIconPosition(findViewById(R.id.ivVoiceSupportInfo),
                    findViewById(R.id.tvVoiceLabel));
        });
    }

    /**
     * 初始化调试项的国际化文本
     */
    private void initializeTestCases() {
        List<TestCase> testCases = TestManager.getInstance().getTestCases();
        for (TestCase tc : testCases) {
            if ("log_tts_voices".equals(tc.id)) {
                tc.name = getString(R.string.test_case_log_tts_voices);
                tc.description = getString(R.string.test_case_log_tts_voices_desc);
            } else if ("fill_test_text".equals(tc.id)) {
                tc.name = getString(R.string.test_case_fill_test_text);
                tc.description = getString(R.string.test_case_fill_test_text_desc);
            } else if ("show_current_voice".equals(tc.id)) {
                tc.name = getString(R.string.test_case_show_current_voice);
                tc.description = getString(R.string.test_case_show_current_voice_desc);
            }
        }
    }

    private void showLanguageSelectionDialog() {
        String[] languages = {
                getString(R.string.language_chinese),
                getString(R.string.language_english)
        };

        Locale currentLocale = LocaleHelper.getCurrentLocale(this);
        int checkedItem = LocaleHelper.isChinese(currentLocale) ? 0 : 1;

        Locale zhCn = LocaleHelper.parseLocaleString(LocaleHelper.LOCALE_ZH_CN);
        Locale enUs = LocaleHelper.parseLocaleString(LocaleHelper.LOCALE_EN_US);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_language)
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    Locale newLocale = which == 0 ? zhCn : enUs;
                    if (!newLocale.equals(currentLocale)) {
                        LocaleHelper.setLanguageMode(this, LocaleHelper.MODE_MANUAL, newLocale);
                        ToastHelper.showShort(this, R.string.toast_language_changed);
                        recreate();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .show();
    }

    private void setupStatusCopyButtons() {
        applyStatusCopyButtonLayout(btnCopySaveDir);
        applyStatusCopyButtonLayout(btnCopyTtsInfoDir);
        applyStatusCopyButtonLayout(btnCopyCurrentVoice);
        if (btnCopySaveDir != null) {
            btnCopySaveDir.setOnClickListener(v -> {
                if (saveDirUri == null) {
                    return;
                }
                copyToClipboard("audio_save_dir", getReadablePathFromUri(saveDirUri), true);
            });
        }
        if (btnCopyTtsInfoDir != null) {
            btnCopyTtsInfoDir.setOnClickListener(v ->
                    copyToClipboard("tts_info_dir", getTtsInfoSaveDirDisplayPath(), true));
        }
        if (btnCopyCurrentVoice != null) {
            btnCopyCurrentVoice.setOnClickListener(v -> {
                Voice voice = resolveCurrentVoiceForUi();
                if (voice == null) {
                    if (!isTtsReady || tts == null) {
                        return;
                    }
                    voice = tts.getVoice();
                }
                if (voice == null) {
                    return;
                }
                copyToClipboard("current_voice", voice.toString(), false);
            });
        }
    }

    private void copyToClipboard(@NonNull String label, @NonNull String text, boolean pathToast) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return;
        }
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
        ToastHelper.showShort(this, pathToast ? R.string.toast_path_copied : R.string.toast_text_copied);
    }

    /**
     * 绑定状态信息中的保存目录展示：值为「未设置」时标题与值同一行；有路径时路径独占一行并换行。
     */
    private void bindSaveDirectoryDisplay(LinearLayout block, TextView label, LinearLayout valueRow,
            TextView valueView, @Nullable ImageButton copyButton, String displayText) {
        String notSet = getString(R.string.not_set);
        boolean isNotSet = notSet.equals(displayText);
        int valueRowTopMargin = (int) (4 * getResources().getDisplayMetrics().density);

        valueView.setText(displayText);
        if (copyButton != null) {
            copyButton.setVisibility(isNotSet ? View.GONE : View.VISIBLE);
        }

        if (isNotSet) {
            block.setOrientation(LinearLayout.HORIZONTAL);
            block.setGravity(android.view.Gravity.CENTER_VERTICAL);
            label.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            rowLp.topMargin = 0;
            valueRow.setLayoutParams(rowLp);
            valueView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        } else {
            block.setOrientation(LinearLayout.VERTICAL);
            label.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.topMargin = valueRowTopMargin;
            valueRow.setLayoutParams(rowLp);
            LinearLayout.LayoutParams valueLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            valueView.setLayoutParams(valueLp);
            if (copyButton != null) {
                applyStatusCopyButtonLayout(copyButton);
            }
        }
    }

    private void applyStatusCopyButtonLayout(@Nullable ImageButton copyButton) {
        if (copyButton == null) {
            return;
        }
        int size = getResources().getDimensionPixelSize(R.dimen.status_info_copy_button_size);
        int marginStart = getResources().getDimensionPixelSize(R.dimen.dp_4);
        LinearLayout.LayoutParams copyLp = new LinearLayout.LayoutParams(size, size);
        copyLp.gravity = android.view.Gravity.TOP;
        copyLp.setMarginStart(marginStart);
        copyButton.setLayoutParams(copyLp);
        copyButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    /**
     * 返回 TTS 信息文件的保存目录路径（用于状态信息展示）。
     * 已设置自定义目录时返回该目录；否则返回应用外部存储默认目录。
     */
    private String getTtsInfoSaveDirDisplayPath() {
        if (ttsInfoDirUri != null) {
            return getReadablePathFromUri(ttsInfoDirUri);
        }
        File defaultDir = getExternalFilesDir(null);
        if (defaultDir != null) {
            return defaultDir.getAbsolutePath();
        }
        return getString(R.string.not_set);
    }

    /**
     * 导出 TTS 引擎诊断信息：写入 tts_info_*.txt（语言与发音人清单）与 tts_locales_*.txt（Locale 字段详情）。
     */
    private void exportTtsEngineDebugInfo() {
        if (tts == null) {
            ToastHelper.showShort(this, R.string.test_tts_not_ready);
            return;
        }

        final Context appContext = getApplicationContext();
        ttsCatalogExecutor.execute(() -> {
            boolean infoSaved = false;
            boolean localesSaved = false;
            try {
                infoSaved = writeTtsInfoExportFile(appContext);
                localesSaved = writeTtsLocalesExportFile(appContext);
            } catch (Exception e) {
                Log.e("MainActivity", "exportTtsEngineDebugInfo error", e);
                runOnUiThread(() -> ToastHelper.showShort(this,
                        getString(R.string.test_save_failed) + ": " + e.getMessage()));
                return;
            }

            boolean finalInfoSaved = infoSaved;
            boolean finalLocalesSaved = localesSaved;
            runOnUiThread(() -> {
                updateStatusInfo();
                if (finalInfoSaved && finalLocalesSaved) {
                    ToastHelper.showShort(this, R.string.test_save_engine_debug_both);
                } else if (finalInfoSaved) {
                    ToastHelper.showShort(this, R.string.test_save_engine_debug_info_only);
                } else if (finalLocalesSaved) {
                    ToastHelper.showShort(this, R.string.test_save_engine_debug_locales_only);
                } else {
                    ToastHelper.showShort(this, R.string.test_save_failed);
                }
            });
        });
    }

    /**
     * 构建并保存 tts_info_[引擎名称].txt（语言、发音人及默认项标注）。
     */
    private boolean writeTtsInfoExportFile(@NonNull Context context) {
        Locale defaultLang = tts.getDefaultVoice() != null ? tts.getDefaultVoice().getLocale()
                : Locale.getDefault();
        Voice defaultVoice = tts.getDefaultVoice();

        Set<Locale> availableLanguages = tts.getAvailableLanguages();
        Set<Voice> voices = tts.getVoices();

        Map<Locale, List<Voice>> localeVoices = new HashMap<>();
        if (voices != null) {
            for (Voice v : voices) {
                Locale l = v.getLocale();
                localeVoices.computeIfAbsent(l, k -> new ArrayList<>()).add(v);
            }
        }

        StringBuilder output = new StringBuilder();
        output.append(context.getString(R.string.test_header_separator)).append("\n");

        Locale appLocaleForInfo = LocaleHelper.getCurrentLocale(context);
        String defaultLangDisplay = defaultLang != null
                ? TtsLocaleDisplayHelper.getDisplayName(defaultLang, appLocaleForInfo,
                TtsLocaleDisplayHelper.voicesForLocale(voices, defaultLang)) + " ("
                + defaultLang.toLanguageTag() + ")"
                : "null";
        output.append(context.getString(R.string.test_default_language)).append(": ").append(defaultLangDisplay)
                .append("\n");

        String defaultVoiceDisplay = defaultVoice != null ? defaultVoice.toString() : "null";
        output.append(context.getString(R.string.test_default_voice)).append(": ").append(defaultVoiceDisplay)
                .append("\n\n");

        List<Locale> sortedLocales = TtsLanguageVoiceHelper.sortLocalesByDisplayName(
                availableLanguages, defaultLang, context, voices);

        for (Locale locale : sortedLocales) {
            String displayName = TtsLocaleDisplayHelper.getDisplayName(locale, appLocaleForInfo,
                    TtsLocaleDisplayHelper.voicesForLocale(voices, locale));
            String languageTag = locale.toLanguageTag();
            output.append(context.getString(R.string.test_language_label)).append(" ").append(displayName)
                    .append(" (").append(languageTag).append(")");
            if (locale.equals(defaultLang)) {
                output.append("  <== ").append(context.getString(R.string.test_default_marker));
            }
            output.append("\n");

            List<Voice> vlist = localeVoices.get(locale);
            Voice langDefaultVoice = languageDefaultVoices.get(locale);
            if (vlist != null) {
                for (Voice v : vlist) {
                    output.append("    - ").append(context.getString(R.string.test_voice_label)).append(": ")
                            .append(v.toString());
                    if (defaultVoice != null && v.getName().equals(defaultVoice.getName())) {
                        output.append("  <== ").append(context.getString(R.string.test_default_marker));
                    }
                    if (langDefaultVoice != null && v.getName().equals(langDefaultVoice.getName())) {
                        output.append("  <== ").append(context.getString(R.string.test_default_for_language));
                    }
                    output.append("\n");
                }
            }
            output.append("\n");
        }
        output.append(context.getString(R.string.test_footer_separator)).append("\n");

        return saveTtsInfoTextToFile(context, getSanitizedTtsEngineFileName("tts_info_"), output.toString());
    }

    /**
     * 构建并保存 tts_locales_[引擎名称].txt（getAvailableLanguages 各 Locale 的方法字段）。
     */
    private boolean writeTtsLocalesExportFile(@NonNull Context context) {
        Locale defaultLang = tts.getDefaultVoice() != null ? tts.getDefaultVoice().getLocale()
                : Locale.getDefault();
        Set<Locale> availableLanguages = tts.getAvailableLanguages();
        Set<Locale> safeLanguages = availableLanguages != null ? availableLanguages : Collections.emptySet();

        java.util.Comparator<Locale> localeTagOrder = (a, b) ->
                a.toLanguageTag().compareToIgnoreCase(b.toLanguageTag());

        List<Locale> sortedAvailable = new ArrayList<>(safeLanguages);
        sortedAvailable.sort(localeTagOrder);

        Locale appLocale = LocaleHelper.getCurrentLocale(context);
        StringBuilder output = new StringBuilder();
        output.append(context.getString(R.string.test_locales_header)).append("\n");
        output.append(context.getString(R.string.test_locales_app_locale_note, appLocale.toLanguageTag()))
                .append("\n\n");

        Set<Voice> voices = tts.getVoices();
        appendLocaleSection(context, output, context.getString(R.string.test_locales_section_available),
                sortedAvailable, defaultLang, appLocale, voices);
        output.append(context.getString(R.string.test_locales_footer)).append("\n");

        return saveTtsInfoTextToFile(context, getSanitizedTtsEngineFileName("tts_locales_"), output.toString());
    }

    /**
     * 将文本保存到 TTS 信息目录（需在后台线程调用）。
     */
    private boolean saveTtsInfoTextToFile(@NonNull Context context, String fileName, String content) {
        if (ttsInfoDirUri != null) {
            try {
                DocumentFile ttsInfoDir = DocumentFile.fromTreeUri(context, ttsInfoDirUri);
                if (ttsInfoDir == null || !ttsInfoDir.exists()) {
                    return false;
                }

                DocumentFile existingFile = ttsInfoDir.findFile(fileName);
                if (existingFile != null) {
                    existingFile.delete();
                }

                DocumentFile newFile = ttsInfoDir.createFile("text/plain", fileName);
                if (newFile == null) {
                    return false;
                }

                try (java.io.OutputStream os = getContentResolver().openOutputStream(newFile.getUri());
                     java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                    writer.write(content);
                }
                return true;
            } catch (Exception e) {
                Log.e("MainActivity", "Failed to save to custom TTS info directory", e);
                return false;
            }
        }

        File outputDir = getExternalFilesDir(null);
        if (outputDir == null) {
            return false;
        }

        File outputFile = new File(outputDir, fileName);
        try (java.io.FileWriter writer = new java.io.FileWriter(outputFile, false)) {
            writer.write(content);
            return true;
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to save TTS info file", e);
            return false;
        }
    }

    private String getSanitizedTtsEngineFileName(String prefix) {
        String engineName = tts != null ? tts.getDefaultEngine() : null;
        if (engineName == null || engineName.isEmpty()) {
            engineName = "unknown";
        }
        engineName = engineName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return prefix + engineName + ".txt";
    }

    private void appendLocaleSection(@NonNull Context context, StringBuilder output, String sectionTitle,
                                     List<Locale> locales, Locale defaultLang, Locale appLocale,
                                     @Nullable Set<Voice> voices) {
        output.append(sectionTitle).append("\n");
        output.append(context.getString(R.string.test_locales_count, locales.size())).append("\n\n");
        int index = 1;
        for (Locale locale : locales) {
            appendLocaleDetails(context, output, locale, defaultLang, appLocale, voices, index++);
        }
        output.append("\n");
    }

    /**
     * 写出 Locale 的公开实例方法返回值；行格式统一为「属性 (方法): 值」。
     * display* 的 Locale 参数为 {@link LocaleHelper#getCurrentLocale(Context)}，与界面语言一致。
     */
    private void appendLocaleDetails(@NonNull Context context, StringBuilder output, Locale locale, Locale defaultLang,
                                     Locale appLocale, @Nullable Set<Voice> voices, int index) {
        output.append("[").append(index).append("]");
        if (defaultLang != null && locale.equals(defaultLang)) {
            output.append("  <== ").append(context.getString(R.string.test_default_marker));
        }
        output.append("\n");

        appendLocaleMethodLine(output, "string", "toString", locale.toString());
        appendLocaleMethodLine(output, "languageTag", "toLanguageTag", locale.toLanguageTag());
        appendLocaleMethodLine(output, "language", "getLanguage", locale.getLanguage());
        appendLocaleMethodLine(output, "country", "getCountry", locale.getCountry());
        appendLocaleMethodLine(output, "variant", "getVariant", locale.getVariant());
        appendLocaleMethodLine(output, "script", "getScript", locale.getScript());
        appendLocaleMethodLine(output, "displayName", "getDisplayName(Locale)", locale.getDisplayName(appLocale));
        String displayNameTts = TtsLocaleDisplayHelper.getDisplayName(locale, appLocale,
                TtsLocaleDisplayHelper.voicesForLocale(voices, locale));
        if (!displayNameTts.equals(locale.getDisplayName(appLocale))) {
            appendLocaleMethodLine(output, "displayNameTts", "TtsLocaleDisplayHelper.getDisplayName",
                    displayNameTts);
        }
        appendLocaleMethodLine(output, "displayLanguage", "getDisplayLanguage(Locale)",
                locale.getDisplayLanguage(appLocale));
        appendLocaleMethodLine(output, "displayCountry", "getDisplayCountry(Locale)",
                locale.getDisplayCountry(appLocale));
        appendLocaleMethodLine(output, "displayScript", "getDisplayScript(Locale)",
                locale.getDisplayScript(appLocale));
        appendLocaleMethodLine(output, "displayVariant", "getDisplayVariant(Locale)",
                locale.getDisplayVariant(appLocale));

        appendLocaleIso3Methods(output, locale);
        appendLocaleBcp47Extensions(output, locale);
        appendLocaleUnicodeExtensions(output, locale);
        appendLocaleStripExtensionsIfDifferent(output, locale);
        appendLocaleMethodLine(output, "hashCode", "hashCode", String.valueOf(locale.hashCode()));
        output.append("\n");
    }

    private void appendLocaleIso3Methods(StringBuilder output, Locale locale) {
        try {
            appendLocaleMethodLine(output, "iso3Language", "getISO3Language", locale.getISO3Language());
        } catch (java.util.MissingResourceException e) {
            appendLocaleMethodLine(output, "iso3Language", "getISO3Language", missingResourceLabel(e));
        }
        try {
            appendLocaleMethodLine(output, "iso3Country", "getISO3Country", locale.getISO3Country());
        } catch (java.util.MissingResourceException e) {
            appendLocaleMethodLine(output, "iso3Country", "getISO3Country", missingResourceLabel(e));
        }
    }

    private void appendLocaleBcp47Extensions(StringBuilder output, Locale locale) {
        Set<Character> keys = locale.getExtensionKeys();
        if (keys == null || keys.isEmpty()) {
            appendLocaleMethodLine(output, "extensionKeys", "getExtensionKeys", "");
            return;
        }
        for (char key : keys) {
            appendLocaleMethodLine(output, "extension", "getExtension('" + key + "')",
                    locale.getExtension(key));
        }
    }

    private void appendLocaleUnicodeExtensions(StringBuilder output, Locale locale) {
        Set<String> attributes = locale.getUnicodeLocaleAttributes();
        appendLocaleMethodLine(output, "unicodeLocaleAttributes", "getUnicodeLocaleAttributes",
                attributes == null || attributes.isEmpty() ? "" : TextUtils.join(", ", attributes));

        Set<String> keys = locale.getUnicodeLocaleKeys();
        if (keys == null || keys.isEmpty()) {
            appendLocaleMethodLine(output, "unicodeLocaleKeys", "getUnicodeLocaleKeys", "");
            return;
        }
        for (String key : keys) {
            String type = locale.getUnicodeLocaleType(key);
            appendLocaleMethodLine(output, "unicodeLocaleType", "getUnicodeLocaleType(\"" + key + "\")",
                    type == null ? "" : type);
        }
    }

    private void appendLocaleStripExtensionsIfDifferent(StringBuilder output, Locale locale) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        Locale stripped = locale.stripExtensions();
        String strippedTag = stripped.toLanguageTag();
        if (!strippedTag.equals(locale.toLanguageTag())) {
            appendLocaleMethodLine(output, "languageTag", "stripExtensions().toLanguageTag", strippedTag);
        }
        String strippedString = stripped.toString();
        if (!strippedString.equals(locale.toString()) && !strippedString.equals(strippedTag)) {
            appendLocaleMethodLine(output, "string", "stripExtensions().toString", strippedString);
        }
    }

    @NonNull
    private static String missingResourceLabel(@NonNull java.util.MissingResourceException e) {
        return "(" + e.getClass().getSimpleName() + ")";
    }

    private void appendLocaleMethodLine(StringBuilder output, String property, String method, String value) {
        output.append("    ").append(property).append(" (").append(method).append("): ")
                .append(value != null ? value : "").append("\n");
    }

    /**
     * 检测并处理TTS引擎变化
     * 如果检测到TTS引擎发生变化，会重新初始化TTS并保持用户当前设置
     */
    private void checkAndHandleTtsEngineChange() {
        // 如果TTS还未初始化，跳过检测
        if (tts == null || !isTtsReady) {
            return;
        }

        // 检测TTS引擎是否发生变化
        if (TtsEngineChangeHelper.hasEngineChanged(this, tts)) {
            // 显示提示信息
            ToastHelper.showShort(this, R.string.toast_tts_engine_changed);

            // 重新初始化TTS引擎
            reinitializeTts();
        }
    }

    // endregion

    // region 私有辅助方法

    /**
     * 重新初始化TTS引擎
     * 保持用户当前的语速、音调、语言和发音人设置
     */
    private void reinitializeTts() {
        // 保存当前用户设置
        float currentSpeechRate = speechRate;
        float currentPitch = pitch;
        Locale currentSelectedLocale = currentLocale;
        Voice currentSelectedVoice = null;

        // 尝试获取当前选中的发音人
        try {
            if (tts != null) {
                currentSelectedVoice = tts.getVoice();
            }
        } catch (Exception e) {
            Log.w("MainActivity", "Failed to get current voice before reinitializing TTS", e);
        }

        // 关闭现有TTS实例
        if (tts != null) {
            try {
                tts.stop();
                tts.shutdown();
            } catch (Exception e) {
                Log.w("MainActivity", "Error shutting down TTS", e);
            }
        }
        cachedTtsEngineDisplayName = null;
        currentVoice = null;

        // 重置状态
        isTtsReady = false;
        btnSpeak.setEnabled(false);
        btnStop.setEnabled(false);
        btnSaveAudio.setEnabled(false);
        tvTtsEngineStatus.setText(getString(R.string.status_not_ready));

        // 重新初始化TTS
        final Locale savedLocale = currentSelectedLocale;
        final Voice savedVoice = currentSelectedVoice;
        final float savedSpeechRate = currentSpeechRate;
        final float savedPitch = currentPitch;

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // TTS初始化成功，恢复用户设置
                restoreUserSettingsAfterReinit(savedLocale, savedVoice, savedSpeechRate, savedPitch);
            } else {
                // TTS初始化失败
                Log.e("MainActivity", "TTS reinitialization failed with status: " + status);
                isTtsReady = false;
                updateStatusInfo();
            }
        });
    }

    /**
     * TTS重新初始化后恢复用户设置
     *
     * @param savedLocale     保存的语言设置
     * @param savedVoice      保存的发音人设置
     * @param savedSpeechRate 保存的语速设置
     * @param savedPitch      保存的音调设置
     */
    private void restoreUserSettingsAfterReinit(Locale savedLocale, @Nullable Voice savedVoice,
                                                float savedSpeechRate, float savedPitch) {
        try {
            // 获取新的默认语言和发音人
            Voice defaultVoice = tts.getDefaultVoice();
            if (defaultVoice != null) {
                defaultLocale = defaultVoice.getLocale();
                globalDefaultVoice = defaultVoice;
            } else {
                defaultLocale = Locale.getDefault();
            }

            // 设置TTS进度监听器（重用现有代码结构）
            setupTtsProgressListener();

            speechRate = savedSpeechRate;
            pitch = savedPitch;
            tts.setSpeechRate(speechRate);
            tts.setPitch(pitch);

            seekBarSpeed.setProgress((int) ((speechRate - Constants.SPEECH_RATE_MIN) * Constants.SEEKBAR_PROGRESS_MULTIPLIER));
            seekBarPitch.setProgress((int) ((pitch - Constants.PITCH_MIN) * Constants.SEEKBAR_PROGRESS_MULTIPLIER));
            textSpeechRateValue.setText(String.format(Locale.US, "%.2f", speechRate));
            textPitchValue.setText(String.format(Locale.US, "%.2f", pitch));

            final Locale savedLocaleFinal = savedLocale;
            final Voice savedVoiceFinal = savedVoice;
            loadTtsCatalogAndApplyUi(() -> finishRestoreUserSettingsAfterCatalog(savedLocaleFinal, savedVoiceFinal));

        } catch (Exception e) {
            Log.e("MainActivity", "Error restoring settings after TTS reinitialization", e);
            // 如果恢复设置失败，至少确保TTS基本可用
            isTtsReady = true;
            btnSpeak.setEnabled(true);
            btnStop.setEnabled(true);
            btnSaveAudio.setEnabled(true);
            tvTtsEngineStatus.setText(getString(R.string.status_ready));
            updateStatusInfo();
        }
    }

    private void beginUtterance(@NonNull String utteranceId) {
        utteranceGenerationById.put(utteranceId, ++utteranceGeneration);
    }

    private void cancelAllUtterances() {
        utteranceGeneration++;
        utteranceGenerationById.clear();
    }

    private boolean isUtteranceCurrent(@NonNull String utteranceId) {
        Integer generation = utteranceGenerationById.get(utteranceId);
        return generation != null && generation == utteranceGeneration;
    }

    private void refreshTtsSpeakStatusDisplay() {
        if (tvTtsSpeakStatus == null) {
            return;
        }
        TtsSpeakStatusHelper.bindStatus(tvTtsSpeakStatus, this, isTtsReady, ttsWorkState,
                lastTtsErrorCode, lastErrorUtteranceId);
        if (btnStop != null) {
            btnStop.setEnabled(TtsSpeakStatusHelper.isStopEnabled(ttsWorkState));
        }
    }

    private void setTtsWorkState(@NonNull TtsSpeakStatusHelper.WorkState state) {
        ttsWorkState = state;
        if (state != TtsSpeakStatusHelper.WorkState.ERROR) {
            lastTtsErrorCode = 0;
            lastErrorUtteranceId = null;
        }
        refreshTtsSpeakStatusDisplay();
        if (TtsSpeakStatusHelper.needsStatusPolling(state, isSavingAudio)) {
            ensureTtsStatusPolling();
        }
    }

    private void setTtsSpeakError(@NonNull String utteranceId, int errorCode) {
        lastTtsErrorCode = errorCode;
        lastErrorUtteranceId = utteranceId;
        ttsWorkState = TtsSpeakStatusHelper.WorkState.ERROR;
        refreshTtsSpeakStatusDisplay();
    }

    private void handleUtteranceStarted(@NonNull String utteranceId) {
        runOnUiThread(() -> {
            if (!isUtteranceCurrent(utteranceId)) {
                return;
            }
            if (TtsSpeakStatusHelper.UTTERANCE_SPEAK.equals(utteranceId)) {
                setTtsWorkState(TtsSpeakStatusHelper.WorkState.SPEAKING);
            } else if (TtsSpeakStatusHelper.UTTERANCE_SAVE.equals(utteranceId)) {
                setTtsWorkState(TtsSpeakStatusHelper.WorkState.SYNTHESIZING);
            }
            updateSpeakAndSaveButtons();
        });
    }

    private void handleUtteranceDone(@NonNull String utteranceId) {
        runOnUiThread(() -> {
            if (!isUtteranceCurrent(utteranceId)) {
                if (TtsSpeakStatusHelper.UTTERANCE_SPEAK.equals(utteranceId)
                        && ttsWorkState == TtsSpeakStatusHelper.WorkState.STOPPED_BY_USER) {
                    setTtsWorkState(TtsSpeakStatusHelper.WorkState.IDLE);
                    updateSpeakAndSaveButtons();
                }
                return;
            }
            if (TtsSpeakStatusHelper.UTTERANCE_SPEAK.equals(utteranceId)) {
                setTtsWorkState(TtsSpeakStatusHelper.WorkState.IDLE);
                ToastHelper.showShort(MainActivity.this, R.string.toast_tts_speak_done);
                updateSpeakAndSaveButtons();
            } else if (TtsSpeakStatusHelper.UTTERANCE_SAVE.equals(utteranceId)) {
                finishSaveAfterSynthesis();
            }
        });
    }

    private void finishSaveAfterSynthesis() {
        if (saveDirUri == null || tempAudioFile == null || !tempAudioFile.exists()) {
            setTtsSpeakError(TtsSpeakStatusHelper.UTTERANCE_SAVE_COPY, TextToSpeech.ERROR_OUTPUT);
            cleanupSaveAfterFailure();
            ToastHelper.showShort(this, R.string.toast_save_audio_write_fail);
            return;
        }
        setTtsWorkState(TtsSpeakStatusHelper.WorkState.COPYING);
        ttsCatalogExecutor.execute(() -> {
            boolean ok = copyTempToSaveDir();
            runOnUiThread(() -> {
                if (isFinishing()) {
                    return;
                }
                if (ok) {
                    ToastHelper.showShort(this, R.string.toast_save_audio_success);
                    setTtsWorkState(TtsSpeakStatusHelper.WorkState.IDLE);
                } else {
                    setTtsSpeakError(TtsSpeakStatusHelper.UTTERANCE_SAVE_COPY, TextToSpeech.ERROR_OUTPUT);
                    ToastHelper.showShort(this, R.string.toast_save_audio_write_fail);
                }
                if (tempAudioFile != null && tempAudioFile.exists() && !tempAudioFile.delete()) {
                    Log.w("MainActivity", "临时音频文件删除失败: " + tempAudioFile.getAbsolutePath());
                }
                endSaveAudioFlow();
            });
        });
    }

    private void endSaveAudioFlow() {
        isSavingAudio = false;
        btnSaveAudio.setEnabled(true);
        btnCancelSave.setEnabled(false);
        updateSpeakAndSaveButtons();
    }

    private void cleanupSaveAfterFailure() {
        endSaveAudioFlow();
    }

    private void handleUtteranceError(@NonNull String utteranceId, int errorCode) {
        runOnUiThread(() -> {
            if (!isUtteranceCurrent(utteranceId)) {
                return;
            }
            setTtsSpeakError(utteranceId, errorCode);
            if (TtsSpeakStatusHelper.UTTERANCE_SPEAK.equals(utteranceId)) {
                ToastHelper.showShort(MainActivity.this, R.string.toast_tts_speak_error);
                updateSpeakAndSaveButtons();
            } else if (TtsSpeakStatusHelper.UTTERANCE_SAVE.equals(utteranceId)) {
                if (tempAudioFile != null && tempAudioFile.exists() && !tempAudioFile.delete()) {
                    Log.w("MainActivity", "临时音频文件删除失败: " + tempAudioFile.getAbsolutePath());
                }
                ToastHelper.showShort(MainActivity.this, R.string.toast_save_audio_synth_fail);
                cleanupSaveAfterFailure();
            }
        });
    }

    private void handleUtteranceStop(@NonNull String utteranceId, boolean interrupted) {
        if (!interrupted) {
            return;
        }
        runOnUiThread(() -> {
            if (!isUtteranceCurrent(utteranceId)) {
                return;
            }
            if (TtsSpeakStatusHelper.UTTERANCE_SAVE.equals(utteranceId)) {
                if (tempAudioFile != null && tempAudioFile.exists() && !tempAudioFile.delete()) {
                    Log.w("MainActivity", "临时音频文件删除失败: " + tempAudioFile.getAbsolutePath());
                }
                cleanupSaveAfterFailure();
            }
            if (ttsWorkState != TtsSpeakStatusHelper.WorkState.STOPPED_BY_USER) {
                setTtsWorkState(TtsSpeakStatusHelper.WorkState.STOPPED_INTERRUPTED);
            }
            updateSpeakAndSaveButtons();
        });
    }

    private boolean checkSpeakSubmitResult(int result) {
        if (result == TextToSpeech.SUCCESS) {
            return true;
        }
        cancelAllUtterances();
        setTtsSpeakError(TtsSpeakStatusHelper.UTTERANCE_SPEAK, result);
        updateSpeakAndSaveButtons();
        return false;
    }

    private boolean checkSaveSubmitResult(int result) {
        if (result == TextToSpeech.SUCCESS) {
            return true;
        }
        cancelAllUtterances();
        setTtsSpeakError(TtsSpeakStatusHelper.UTTERANCE_SAVE, result);
        isSavingAudio = false;
        btnSaveAudio.setEnabled(true);
        btnCancelSave.setEnabled(false);
        updateSpeakAndSaveButtons();
        return false;
    }

    /**
     * 设置 TTS 朗读与保存进度监听（初始化与引擎切换后调用）。
     */
    private void setupTtsProgressListener() {
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                handleUtteranceStarted(utteranceId);
            }

            @Override
            public void onDone(String utteranceId) {
                handleUtteranceDone(utteranceId);
            }

            @Override
            public void onStop(String utteranceId, boolean interrupted) {
                handleUtteranceStop(utteranceId, interrupted);
            }

            @Override
            public void onError(String utteranceId) {
                onError(utteranceId, TextToSpeech.ERROR);
            }

            @Override
            public void onError(String utteranceId, int errorCode) {
                handleUtteranceError(utteranceId, errorCode);
            }
        });
    }

    // endregion
}
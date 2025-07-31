package com.citadawn.speechapp.ui;

import android.annotation.SuppressLint;
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
import android.util.Log;
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

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;

import com.citadawn.speechapp.R;
import com.citadawn.speechapp.ui.test.TestCase;
import com.citadawn.speechapp.ui.test.TestManager;
import com.citadawn.speechapp.ui.test.TestModeDialog;
import com.citadawn.speechapp.util.ButtonTextHelper;
import com.citadawn.speechapp.util.ClearButtonHelper;
import com.citadawn.speechapp.util.Constants;
import com.citadawn.speechapp.util.DelayedTaskHelper;
import com.citadawn.speechapp.util.DialogHelper;
import com.citadawn.speechapp.util.InfoIconHelper;
import com.citadawn.speechapp.util.InfoIconPositionHelper;
import com.citadawn.speechapp.util.LocaleHelper;
import com.citadawn.speechapp.util.SeekBarHelper;
import com.citadawn.speechapp.util.TextLengthHelper;
import com.citadawn.speechapp.util.ToastHelper;
import com.citadawn.speechapp.util.ViewHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// 自定义Adapter用于语言下拉列表，左侧竖条区分支持度
class LanguageAdapter extends BaseAdapter {
    private final List<Locale> locales;
    private final LayoutInflater inflater;
    private final TextToSpeech tts;
    private final Context context;
    private final Locale defaultLocale;
    private int selectedPosition = 0; // 保存当前选中位置

    public LanguageAdapter(Context context, List<Locale> locales, TextToSpeech tts, Locale defaultLocale) {
        this.context = context;
        this.locales = locales;
        this.inflater = LayoutInflater.from(context);
        this.tts = tts;
        this.defaultLocale = defaultLocale;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent, false);
    }

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

    private int getSupportColorRes(int support) {
        if (support == TextToSpeech.LANG_AVAILABLE) {
            return R.color.tts_support_full;
        } else if (support == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
            return R.color.tts_support_partial;
        } else if (support == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
            return R.color.tts_support_variant;
        } else if (support == TextToSpeech.LANG_MISSING_DATA) {
            return R.color.tts_support_missing_data;
        } else {
            return R.color.tts_support_none;
        }
    }

    private View createView(int position, View convertView, ViewGroup parent, boolean isDropdown) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.spinner_item_language, parent, false);
        }
        Locale locale = locales.get(position);
        TextView tv = view.findViewById(R.id.tvLanguageName);
        View bar = view.findViewById(R.id.viewSupportBar);
        String name = locale.getDisplayName();
        if (locale.equals(defaultLocale)) {
            name += context.getString(R.string.default_value);
        }
        tv.setText(name);
        int support = tts.isLanguageAvailable(locale);
        int colorRes = getSupportColorRes(support);
        bar.setBackgroundResource(colorRes);
        // 可选：下拉项背景色区分
        if (isDropdown) {
            view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }
        return view;
    }
}

// 自定义发音人适配器，支持显示 features
class VoiceAdapter extends BaseAdapter {
    private final List<Voice> voices;
    private final LayoutInflater inflater;
    private final Context context;
    private final Voice defaultVoice;
    private int selectedPosition = 0;

    public VoiceAdapter(Context context, List<Voice> voices, Voice defaultVoice) {
        this.context = context;
        this.voices = voices;
        this.defaultVoice = defaultVoice;
        this.inflater = LayoutInflater.from(context);
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = createView(position, convertView, parent);
        LinearLayout layoutRoot = view.findViewById(R.id.layoutRoot);
        if (position == selectedPosition) {
            layoutRoot.setBackgroundResource(R.color.tts_spinner_selected_bg);
        } else {
            layoutRoot.setBackgroundResource(R.color.tts_spinner_normal_bg);
        }
        return view;
    }

    private boolean isMeaninglessFeature(String feature) {
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

    private boolean shouldShowFeatures(Set<String> features) {
        if (features == null || features.isEmpty())
            return false;
        for (String f : features) {
            if (!isMeaninglessFeature(f))
                return true;
        }
        return false;
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.spinner_item_voice, parent, false);
        }

        Voice voice = voices.get(position);
        TextView tvVoiceName = view.findViewById(R.id.tvVoiceName);
        TextView tvVoiceFeatures = view.findViewById(R.id.tvVoiceFeatures);

        // 设置发音人名称
        String voiceName = voice.getName();
        // 去除下划线及后缀数字
        voiceName = voiceName.replaceAll("_[0-9]+$", "");
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

        return view;
    }
}

public class MainActivity extends AppCompatActivity {
    // region 成员变量
    private TextToSpeech tts;
    private EditText editText;
    private Button btnSpeak;
    private SeekBar seekBarSpeed, seekBarPitch;
    private boolean isTtsReady = false;
    private float speechRate = 1.0f;
    private float pitch = 1.0f;
    private Locale currentLocale = null; // 当前语言，将通过TTS API动态获取
    private Button btnClear;
    private Button btnStop;
    private Button btnSaveAudio;
    private String pendingAudioText = null;
    private TextView textSpeechRateValue, textPitchValue;
    private Button btnSpeedReset, btnPitchReset;
    private TextView tvSpeedSetResult, tvPitchSetResult;
    private Spinner spinnerLanguage, spinnerVoice;
    private Button btnLangVoiceReset;
    private Locale defaultLocale = null; // 默认语言，将通过TTS API获取
    private Voice globalDefaultVoice = null; // 全局默认发音人
    private final HashMap<Locale, Voice> languageDefaultVoices = new HashMap<>(); // 每个语言的默认发音人
    private final ArrayList<Locale> localeList = new ArrayList<>();
    private final ArrayList<Voice> voiceList = new ArrayList<>();
    private boolean isLangSpinnerInit = false;
    private boolean isVoiceSpinnerInit = false;
    private TextView tvTtsEngineStatus, tvAudioSaveDir, tvTtsSpeakStatus, tvSelectedTestCases;
    private ImageButton btnCopySaveDir;
    private final Handler ttsStatusHandler = new Handler(Looper.getMainLooper());
    private static final String PREFS_NAME = Constants.PREFS_NAME;
    private static final String KEY_SAVE_DIR_URI = Constants.KEY_SAVE_DIR_URI;
    private Uri saveDirUri = null;
    private Button btnCancelSave;
    private boolean isSavingAudio = false;
    private File tempAudioFile = null;
    private String currentAudioFileName = "tts_output.wav";

    // 当前TTS状态
    private enum TtsWorkState {
        IDLE, // 空闲
        SPEAKING, // 正在朗读
        SAVING // 正在保存音频
    }

    private volatile TtsWorkState ttsWorkState = TtsWorkState.IDLE;

    // 新增：TTS任务准备状态
    private enum PendingTtsAction {
        NONE, PENDING_SPEAK, PENDING_SAVE
    }

    private volatile PendingTtsAction pendingTtsAction = PendingTtsAction.NONE;

    private ActivityResultLauncher<Intent> editorLauncher;

    private long lastBackPressedTime = 0;
    private static final int DOUBLE_BACK_EXIT_INTERVAL = 2000; // 2秒

    // endregion

    // region 生命周期

    /**
     * 活动创建时初始化UI和TTS引擎
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 启动时自动退出测试模式
        TestManager.getInstance().resetAll();
        // 应用用户选择的语言设置 Apply user selected language setting
        LocaleHelper.setLocale(this, LocaleHelper.getCurrentLocale(this));

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // 始终浅色
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 设置返回键处理
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 如果有弹窗或对话框，优先让其处理返回键
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    return;
                }
                long now = System.currentTimeMillis();
                if (now - lastBackPressedTime < DOUBLE_BACK_EXIT_INTERVAL) {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed(); // 退出App
                } else {
                    lastBackPressedTime = now;
                    ToastHelper.showShort(MainActivity.this, R.string.toast_double_back_exit);
                }
            }
        });

        // 初始化测试用例的国际化文本
        initializeTestCases();

        // 设置自定义Toolbar为ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_more_vert_white_24dp));

        // 动态设置statusBarSpacer高度为状态栏高度
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editText = findViewById(R.id.editText);
        View rootView = findViewById(R.id.main);
        androidx.core.widget.NestedScrollView scrollView = findViewById(R.id.nestedScrollView);

        // 设置点击空白处收起键盘并让EditText失去焦点
        rootView.setOnClickListener(v -> {
            if (editText.hasFocus()) {
                editText.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        });

        // 添加触摸监听器到NestedScrollView，检测滑动时让输入框失去焦点
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            private float startY = 0;
            private float startX = 0;
            private static final float SCROLL_THRESHOLD = 15; // 滑动阈值，单位像素
            private boolean isScrolling = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        startX = event.getX();
                        isScrolling = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float currentY = event.getY();
                        float currentX = event.getX();
                        float deltaY = Math.abs(currentY - startY);
                        float deltaX = Math.abs(currentX - startX);

                        // 如果滑动距离超过阈值，标记为滑动状态
                        if (deltaY > SCROLL_THRESHOLD || deltaX > SCROLL_THRESHOLD) {
                            isScrolling = true;
                            // 如果输入框有焦点，让输入框失去焦点
                            if (editText.hasFocus()) {
                                editText.clearFocus();
                                InputMethodManager imm = (InputMethodManager) getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        // 如果不是滑动，则作为点击处理
                        if (!isScrolling) {
                            if (editText.hasFocus()) {
                                editText.clearFocus();
                                InputMethodManager imm = (InputMethodManager) getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                            }
                        }
                        break;
                }
                return false; // 不消费触摸事件，让其他控件也能响应
            }
        });
        btnSpeak = findViewById(R.id.btnSpeak);
        seekBarSpeed = findViewById(R.id.seekBarSpeed);
        seekBarPitch = findViewById(R.id.seekBarPitch);
        tvTtsEngineStatus = findViewById(R.id.tvTtsEngineStatus);
        tvAudioSaveDir = findViewById(R.id.tvAudioSaveDir);
        tvTtsSpeakStatus = findViewById(R.id.tvTtsSpeakStatus);
        tvSelectedTestCases = findViewById(R.id.tvSelectedTestCases);
        btnCopySaveDir = findViewById(R.id.btnCopySaveDir);
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
        btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(v -> {
            if (tts != null && isTtsReady) {
                tts.stop();
                ttsWorkState = TtsWorkState.IDLE;
                tvTtsSpeakStatus.setText(getString(R.string.tts_idle));
                tvTtsSpeakStatus
                        .setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_green_dark));
            }
        });

        btnSaveAudio = findViewById(R.id.btnSaveAudio);
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
        // 读取保存目录Uri
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String uriStr = prefs.getString(KEY_SAVE_DIR_URI, null);
        if (uriStr != null) {
            saveDirUri = Uri.parse(uriStr);
        }
        btnSetSaveDir.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    | Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            openDirLauncher.launch(intent);
        });
        btnCancelSave.setOnClickListener(v -> confirmCancelSave());

        textSpeechRateValue = findViewById(R.id.textSpeechRateValue);
        textPitchValue = findViewById(R.id.textPitchValue);
        TextView btnSpeedMinus = findViewById(R.id.btnSpeedMinus);
        TextView btnSpeedPlus = findViewById(R.id.btnSpeedPlus);
        TextView btnPitchMinus = findViewById(R.id.btnPitchMinus);
        TextView btnPitchPlus = findViewById(R.id.btnPitchPlus);
        btnSpeedReset = findViewById(R.id.btnSpeedReset);
        btnPitchReset = findViewById(R.id.btnPitchReset);
        tvSpeedSetResult = findViewById(R.id.tvSpeedSetResult);
        tvPitchSetResult = findViewById(R.id.tvPitchSetResult);

        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        spinnerVoice = findViewById(R.id.spinnerVoice);
        btnLangVoiceReset = findViewById(R.id.btnLangVoiceReset);

        // 设置信息图标
        setupInfoIcons();

        // 设置信息图标的动态位置
        setupInfoIconPositions();
        // 启动定时任务，定期检查TTS状态
        // 优先显示准备状态
        Runnable ttsStatusRunnable = new Runnable() {
            @Override
            public void run() {
                // 优先显示准备状态
                if (pendingTtsAction == PendingTtsAction.PENDING_SPEAK) {
                    tvTtsSpeakStatus.setText(getString(R.string.tts_prepare_read));
                    tvTtsSpeakStatus
                            .setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_orange_dark));
                } else if (pendingTtsAction == PendingTtsAction.PENDING_SAVE) {
                    tvTtsSpeakStatus.setText(getString(R.string.tts_prepare_save));
                    tvTtsSpeakStatus
                            .setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_orange_dark));
                } else if (ttsWorkState == TtsWorkState.SPEAKING) {
                    tvTtsSpeakStatus.setText(getString(R.string.tts_reading));
                    tvTtsSpeakStatus
                            .setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_dark));
                } else if (ttsWorkState == TtsWorkState.SAVING) {
                    tvTtsSpeakStatus.setText(getString(R.string.tts_saving));
                    tvTtsSpeakStatus
                            .setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_blue_dark));
                } else {
                    tvTtsSpeakStatus.setText(getString(R.string.tts_idle));
                    tvTtsSpeakStatus
                            .setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_green_dark));
                }
                btnStop.setEnabled(ttsWorkState == TtsWorkState.SPEAKING);
                updateSpeakAndSaveButtons();
                ttsStatusHandler.postDelayed(this, Constants.TTS_STATUS_UPDATE_INTERVAL);
            }
        };
        ttsStatusHandler.post(ttsStatusRunnable);
        // 初始化语言和发音人Spinner
        spinnerLanguage.setAdapter(new LanguageAdapter(this, localeList, tts, defaultLocale));
        // 发音人Spinner使用自定义适配器
        spinnerVoice.setAdapter(new VoiceAdapter(this, voiceList, null));

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
                    updateResetButtons();

                    // 测试模式下执行测试
                    testSpeedSettingFailure();
                });
        btnSpeedMinus.setOnClickListener(v -> {
            float value = Float.parseFloat(textSpeechRateValue.getText().toString());
            value -= Constants.SPEECH_RATE_STEP;
            if (value < Constants.SPEECH_RATE_MIN)
                value = Constants.SPEECH_RATE_MIN;
            value = Math.round(value * 100f) / 100f; // 保留两位小数
            textSpeechRateValue.setText(String.format(Locale.US, "%.2f", value));
            speechRate = value;
            int progress = Math.round((value - Constants.SPEECH_RATE_MIN) / 0.1f);
            seekBarSpeed.setProgress(progress);
            if (tts != null && isTtsReady) {
                int result = tts.setSpeechRate(speechRate);
                if (result != TextToSpeech.SUCCESS) {
                    tvSpeedSetResult.setText(R.string.message_speed_set_failed);
                    DelayedTaskHelper.clearTextDelayed(tvSpeedSetResult, Constants.TOAST_MESSAGE_DELAY);
                }
            }
            updateResetButtons();

            // 测试模式下执行测试
            testSpeedSettingFailure();
        });
        btnSpeedPlus.setOnClickListener(v -> {
            float value = Float.parseFloat(textSpeechRateValue.getText().toString());
            value += Constants.SPEECH_RATE_STEP;
            if (value > Constants.SPEECH_RATE_MAX)
                value = Constants.SPEECH_RATE_MAX;
            value = Math.round(value * 100f) / 100f;
            textSpeechRateValue.setText(String.format(Locale.US, "%.2f", value));
            speechRate = value;
            int progress = Math.round((value - Constants.SPEECH_RATE_MIN) / 0.1f);
            seekBarSpeed.setProgress(progress);
            if (tts != null && isTtsReady) {
                int result = tts.setSpeechRate(speechRate);
                if (result != TextToSpeech.SUCCESS) {
                    tvSpeedSetResult.setText(R.string.message_speed_set_failed);
                    DelayedTaskHelper.clearTextDelayed(tvSpeedSetResult, Constants.TOAST_MESSAGE_DELAY);
                }
            }
            updateResetButtons();

            // 测试模式下执行测试
            testSpeedSettingFailure();
        });
        btnSpeedReset.setOnClickListener(v -> {
            seekBarSpeed.setProgress(Constants.SEEKBAR_DEFAULT_PROGRESS);
            textSpeechRateValue.setText(getString(R.string.default_speed_value));
            speechRate = Constants.SPEECH_RATE_DEFAULT;
            if (tts != null && isTtsReady) {
                int result = tts.setSpeechRate(speechRate);
                if (result != TextToSpeech.SUCCESS) {
                    tvSpeedSetResult.setText(R.string.message_speed_set_failed);
                    DelayedTaskHelper.clearTextDelayed(tvSpeedSetResult, Constants.TOAST_MESSAGE_DELAY);
                }
            }
            updateResetButtons();

            // 测试模式下执行测试
            testSpeedSettingFailure();
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
                    updateResetButtons();

                    // 测试模式下执行测试
                    testPitchSettingFailure();
                });
        btnPitchMinus.setOnClickListener(v -> {
            float value = Float.parseFloat(textPitchValue.getText().toString());
            value -= Constants.PITCH_STEP;
            if (value < Constants.PITCH_MIN)
                value = Constants.PITCH_MIN;
            value = Math.round(value * 100f) / 100f;
            textPitchValue.setText(String.format(Locale.US, "%.2f", value));
            pitch = value;
            int progress = Math.round((value - Constants.PITCH_MIN) / 0.1f);
            seekBarPitch.setProgress(progress);
            if (tts != null && isTtsReady) {
                int result = tts.setPitch(pitch);
                if (result != TextToSpeech.SUCCESS) {
                    tvPitchSetResult.setText(R.string.message_pitch_set_failed);
                    DelayedTaskHelper.clearTextDelayed(tvPitchSetResult, Constants.TOAST_MESSAGE_DELAY);
                }
            }
            updateResetButtons();

            // 测试模式下执行测试
            testPitchSettingFailure();
        });
        btnPitchPlus.setOnClickListener(v -> {
            float value = Float.parseFloat(textPitchValue.getText().toString());
            value += Constants.PITCH_STEP;
            if (value > Constants.PITCH_MAX)
                value = Constants.PITCH_MAX;
            value = Math.round(value * 100f) / 100f;
            textPitchValue.setText(String.format(Locale.US, "%.2f", value));
            pitch = value;
            int progress = Math.round((value - Constants.PITCH_MIN) / 0.1f);
            seekBarPitch.setProgress(progress);
            if (tts != null && isTtsReady) {
                int result = tts.setPitch(pitch);
                if (result != TextToSpeech.SUCCESS) {
                    tvPitchSetResult.setText(R.string.message_pitch_set_failed);
                    DelayedTaskHelper.clearTextDelayed(tvPitchSetResult, Constants.TOAST_MESSAGE_DELAY);
                }
            }
            updateResetButtons();

            // 测试模式下执行测试
            testPitchSettingFailure();
        });
        btnPitchReset.setOnClickListener(v -> {
            seekBarPitch.setProgress(Constants.SEEKBAR_DEFAULT_PROGRESS);
            textPitchValue.setText(getString(R.string.default_pitch_value));
            pitch = Constants.PITCH_DEFAULT;
            if (tts != null && isTtsReady) {
                int result = tts.setPitch(pitch);
                if (result != TextToSpeech.SUCCESS) {
                    tvPitchSetResult.setText(R.string.message_pitch_set_failed);
                    DelayedTaskHelper.clearTextDelayed(tvPitchSetResult, Constants.TOAST_MESSAGE_DELAY);
                }
            }
            updateResetButtons();

            // 测试模式下执行测试
            testPitchSettingFailure();
        });

        // 语言和发音人设置
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isLangSpinnerInit)
                    return;
                Locale selected = localeList.get(position);
                currentLocale = selected;
                tts.setLanguage(selected);
                updateVoiceList(selected, false);
                updateResetButtons();
                // 更新高亮位置
                if (parent.getAdapter() instanceof LanguageAdapter) {
                    ((LanguageAdapter) parent.getAdapter()).setSelectedPosition(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerVoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isVoiceSpinnerInit)
                    return;
                Voice selected = voiceList.get(position);
                tts.setVoice(selected);
                updateResetButtons();
                // 更新高亮位置
                if (parent.getAdapter() instanceof VoiceAdapter) {
                    ((VoiceAdapter) parent.getAdapter()).setSelectedPosition(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        btnLangVoiceReset.setOnClickListener(v -> {
            if (defaultLocale != null) {
                int idx = localeList.indexOf(defaultLocale);
                if (idx >= 0)
                    spinnerLanguage.setSelection(idx);
                tts.setLanguage(defaultLocale);
                updateVoiceList(defaultLocale, true);
                updateResetButtons();
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
                // 设置TTS任务进度监听（包括朗读和音频保存）
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        if ("tts_speak".equals(utteranceId)) {
                            ttsWorkState = TtsWorkState.SPEAKING;
                            pendingTtsAction = PendingTtsAction.NONE;
                        } else if ("tts_save".equals(utteranceId)) {
                            ttsWorkState = TtsWorkState.SAVING;
                            pendingTtsAction = PendingTtsAction.NONE;
                        }
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        if ("tts_speak".equals(utteranceId)) {
                            ttsWorkState = TtsWorkState.IDLE;
                            runOnUiThread(() -> {
                                ToastHelper.showShort(MainActivity.this, R.string.toast_tts_speak_done);
                                updateSpeakAndSaveButtons();
                            });
                        } else if ("tts_save".equals(utteranceId)) {
                            runOnUiThread(() -> {
                                if (saveDirUri != null && tempAudioFile != null && tempAudioFile.exists()) {
                                    boolean ok = copyTempToSaveDir();
                                    if (ok) {
                                        ToastHelper.showShort(MainActivity.this, R.string.toast_save_audio_success);
                                    } else {
                                        ToastHelper.showShort(MainActivity.this, R.string.toast_save_audio_write_fail);
                                    }
                                    if (!tempAudioFile.delete()) {
                                        Log.w("MainActivity", "临时音频文件删除失败: " + tempAudioFile.getAbsolutePath());
                                    }
                                }
                                isSavingAudio = false;
                                btnSaveAudio.setEnabled(true);
                                btnCancelSave.setEnabled(false);
                                updateSpeakAndSaveButtons();
                            });
                            ttsWorkState = TtsWorkState.IDLE;
                        }
                    }

                    @Override
                    public void onError(String utteranceId) {
                        // 兼容旧API，调用新API处理
                        onError(utteranceId, android.speech.tts.TextToSpeech.ERROR);
                    }

                    @Override
                    public void onError(String utteranceId, int errorCode) {
                        ttsWorkState = TtsWorkState.IDLE;
                        pendingTtsAction = PendingTtsAction.NONE;
                        if ("tts_speak".equals(utteranceId)) {
                            runOnUiThread(() -> {
                                ToastHelper.showShort(MainActivity.this, R.string.toast_tts_speak_error);
                                updateSpeakAndSaveButtons();
                            });
                        } else if ("tts_save".equals(utteranceId)) {
                            runOnUiThread(() -> {
                                if (tempAudioFile != null && tempAudioFile.exists()) {
                                    boolean deleted = tempAudioFile.delete();
                                    if (!deleted) {
                                        Log.w("MainActivity", "临时音频文件删除失败: " + tempAudioFile.getAbsolutePath());
                                    }
                                }
                                ToastHelper.showShort(MainActivity.this, R.string.toast_save_audio_synth_fail);
                                isSavingAudio = false;
                                btnSaveAudio.setEnabled(true);
                                btnCancelSave.setEnabled(false);
                                updateSpeakAndSaveButtons();
                            });
                        }
                    }
                });
                isTtsReady = true;
                btnSpeak.setEnabled(true);
                btnStop.setEnabled(true);
                btnSaveAudio.setEnabled(true);
                tvTtsEngineStatus.setText(getString(R.string.status_ready));
                // 更新状态信息显示区域，包含系统语言提示
                updateStatusInfo();

                // 获取可用语言
                {
                    Set<Locale> locales = tts.getAvailableLanguages();
                    localeList.clear();
                    // 按语言名称排序（默认语言排在最前面）
                    Collator collator = Collator.getInstance(Locale.CHINESE);
                    localeList.addAll(locales);
                    localeList.sort((l1, l2) -> {
                        // 默认语言始终排在最前面
                        if (l1.equals(defaultLocale))
                            return -1;
                        if (l2.equals(defaultLocale))
                            return 1;
                        // 其他语言按拼音排序
                        return collator.compare(l1.getDisplayName(), l2.getDisplayName());
                    });
                    spinnerLanguage
                            .setAdapter(new LanguageAdapter(this, localeList, tts, defaultLocale));
                    isLangSpinnerInit = true;
                    spinnerLanguage.setSelection(0);
                }
                // 初始化每个语言的默认发音人
                initializeLanguageDefaultVoices();
                // 获取可用发音人
                updateVoiceList(currentLocale, true);

                // 输出语言列表和默认发音人
                Set<Locale> locales = tts.getAvailableLanguages();
                ArrayList<Locale> sortedLocales = new ArrayList<>(locales);
                // 默认语言排在最前面，其余按拼音/本地化排序
                Collator collator = Collator.getInstance(Locale.CHINESE);
                sortedLocales.sort((l1, l2) -> {
                    if (l1.equals(defaultLocale))
                        return -1;
                    if (l2.equals(defaultLocale))
                        return 1;
                    return collator.compare(l1.getDisplayName(), l2.getDisplayName());
                });
                StringBuilder sb = new StringBuilder();
                sb.append(getString(R.string.language_list));
                sb.append("\n");
                for (Locale locale : sortedLocales) {
                    String display = locale.getDisplayName() + " (" + locale.toLanguageTag() + ")";
                    Voice defVoice = languageDefaultVoices.get(locale);
                    if (locale.equals(defaultLocale)) {
                        sb.append(display).append(getString(R.string.default_voice));
                    } else {
                        sb.append(display).append(getString(R.string.default_voice_of));
                    }
                    sb.append(defVoice != null ? defVoice.getName() : getString(R.string.none)).append("\n");
                }
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
            // 只有在真正朗读前才输出TTS信息
            if (TestManager.getInstance().isTestMode()) {
                boolean logVoices = false;
                for (TestCase tc : TestManager.getInstance().getSelectedTestCases()) {
                    if ("log_tts_voices".equals(tc.id)) {
                        logVoices = true;
                        break;
                    }
                }
                if (logVoices)
                    logTtsVoices();
            }
            ToastHelper.showShort(this, R.string.toast_read_task_submitted);
            pendingTtsAction = PendingTtsAction.PENDING_SPEAK;
            tts.setSpeechRate(speechRate);
            tts.setPitch(pitch);
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_speak");
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "tts_speak");
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
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setEnabled(!s.toString().isEmpty());
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

    @Override
    protected void onDestroy() {
        ttsStatusHandler.removeCallbacksAndMessages(null);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem testModeItem = menu.findItem(R.id.action_test_mode);
        if (TestManager.getInstance().isTestMode()) {
            testModeItem.setTitle(R.string.test_mode_exit);
        } else {
            testModeItem.setTitle(R.string.menu_test_mode);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_test_mode) {
            if (TestManager.getInstance().isTestMode()) {
                // 退出测试模式
                TestManager.getInstance().resetAll();
                updateToolbarTitle();
                updateStatusInfo();
                invalidateOptionsMenu(); // 刷新菜单
                ToastHelper.showShort(this, R.string.test_mode_exit_toast);
            } else {
                // 打开测试模式Dialog（原有逻辑）
                TestModeDialog dialog = new TestModeDialog(this, TestManager.getInstance().getTestCases(), selected -> {
                    boolean anySelected = false;
                    for (TestCase tc : selected) {
                        if (tc.selected) {
                            anySelected = true;
                            break;
                        }
                    }
                    TestManager.getInstance().setTestMode(anySelected);
                    updateToolbarTitle();
                    updateStatusInfo();
                    invalidateOptionsMenu();
                    if (anySelected) {
                        ToastHelper.showShort(this, R.string.test_mode_title);
                    } else {
                        ToastHelper.showShort(this, R.string.test_mode_btn_cancel);
                    }
                });
                dialog.show();
            }
            return true;
        } else if (id == R.id.action_tts_settings) {
            try {
                Intent intent = new Intent("com.android.settings.TTS_SETTINGS");
                startActivity(intent);
            } catch (Exception e) {
                ToastHelper.showShort(this, R.string.toast_cannot_open_tts_settings);
            }
            return true;
        } else if (id == R.id.action_language) {
            showLanguageSelectionDialog();
            return true;
        } else if (id == R.id.action_info) {
            String info = getString(R.string.desc_tts_info) + "\n\n" + getString(R.string.desc_tts_length_limit);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_info)
                    .setMessage(info)
                    .setPositiveButton(R.string.dialog_button_ok, null)
                    .show();
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
        String title = getString(R.string.app_name);
        if (TestManager.getInstance().isTestMode()) {
            title += getString(R.string.test_mode_toolbar_suffix);
            toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));
        }
        toolbar.setTitle(title);
    }

    /**
     * 将文本合成为音频文件并保存到指定URI
     *
     * @param text 要合成的文本
     * @param uri  保存音频的目标URI
     */
    private void synthesizeTextToUri(String text, Uri uri) {
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

    /**
     * 初始化各语言的默认发音人信息
     * 遍历所有可用语言，获取每个语言的默认发音人
     */
    private void initializeLanguageDefaultVoices() {
        Set<Voice> voices = tts.getVoices();
        languageDefaultVoices.clear();

        // 按语言分组，为每个语言确定默认发音人
        HashMap<Locale, ArrayList<Voice>> voicesByLanguage = new HashMap<>();

        for (Voice voice : voices) {
            Locale locale = voice.getLocale();
            if (!voicesByLanguage.containsKey(locale)) {
                voicesByLanguage.put(locale, new ArrayList<>());
            }
            voicesByLanguage.computeIfAbsent(locale, k -> new ArrayList<>()).add(voice);
        }

        // 为每个语言确定默认发音人（通常是第一个）
        for (Map.Entry<Locale, ArrayList<Voice>> entry : voicesByLanguage.entrySet()) {
            Locale locale = entry.getKey();
            ArrayList<Voice> voiceList = entry.getValue();

            if (!voiceList.isEmpty()) {
                // 如果全局默认发音人是这个语言的，优先使用它
                Voice defaultVoice;
                if (globalDefaultVoice != null && globalDefaultVoice.getLocale().equals(locale)) {
                    defaultVoice = globalDefaultVoice;
                } else {
                    // 否则使用该语言的第一个发音人
                    defaultVoice = voiceList.get(0);
                }

                languageDefaultVoices.put(locale, defaultVoice);
            }
        }
    }

    /**
     * 根据选择的语言更新发音人列表
     *
     * @param locale         选择的语言
     * @param resetToDefault 是否重置为默认发音人
     */
    private void updateVoiceList(Locale locale, boolean resetToDefault) {
        Set<Voice> voices = tts.getVoices();
        voiceList.clear();

        // 先收集所有匹配语言的发音人
        for (Voice voice : voices) {
            if (voice.getLocale().equals(locale)) {
                voiceList.add(voice);
            }
        }

        // 获取当前语言的默认发音人
        Voice currentLangDefaultVoice = languageDefaultVoices.get(locale);

        // 默认发音人排在最前面
        voiceList.sort((v1, v2) -> {
            // 当前语言的默认发音人排在最前面
            if (currentLangDefaultVoice != null) {
                if (v1.equals(currentLangDefaultVoice))
                    return -1;
                if (v2.equals(currentLangDefaultVoice))
                    return 1;
            }
            return v1.getName().compareTo(v2.getName());
        });

        // 重新创建适配器以更新默认发音人信息
        VoiceAdapter adapter = new VoiceAdapter(this, voiceList, currentLangDefaultVoice);
        spinnerVoice.setAdapter(adapter);

        isVoiceSpinnerInit = true;
        if (resetToDefault && currentLangDefaultVoice != null) {
            spinnerVoice.setSelection(0); // 默认发音人现在总是在第一位
            tts.setVoice(currentLangDefaultVoice);
        } else if (!voiceList.isEmpty()) {
            spinnerVoice.setSelection(0);
            tts.setVoice(voiceList.get(0));
        }
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
            tvAudioSaveDir.setText(getReadablePathFromUri(saveDirUri));
            btnCopySaveDir.setVisibility(View.VISIBLE);
        } else {
            tvAudioSaveDir.setText(getString(R.string.not_set));
            btnCopySaveDir.setVisibility(View.GONE);
        }
        // 语音合成状态（此处只初始化，动态状态由其它逻辑控制）
        // tvTtsSpeakStatus.setText("空闲"); // 由其它逻辑动态设置
        // 新增：显示当前选择的测试项
        if (tvSelectedTestCases != null) {
            if (TestManager.getInstance().isTestMode()) {
                List<String> selected = TestManager.getInstance().getSelectedTestCases().stream().map(tc -> tc.name)
                        .collect(java.util.stream.Collectors.toList());
                if (selected.isEmpty()) {
                    tvSelectedTestCases.setText(getString(R.string.none));
                } else {
                    tvSelectedTestCases.setText(android.text.TextUtils.join("、", selected));
                }
                // 显示当前选择的测试项行
                ((android.view.View) tvSelectedTestCases.getParent()).setVisibility(android.view.View.VISIBLE);
                // 设置测试模式下的红色文字颜色
                tvSelectedTestCases.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                // 设置标题文字颜色（通过findViewById获取标题TextView）
                android.view.View parentView = (android.view.View) tvSelectedTestCases.getParent();
                if (parentView instanceof android.widget.LinearLayout) {
                    android.widget.LinearLayout linearLayout = (android.widget.LinearLayout) parentView;
                    if (linearLayout.getChildCount() > 0) {
                        android.view.View titleView = linearLayout.getChildAt(0);
                        if (titleView instanceof android.widget.TextView) {
                            ((android.widget.TextView) titleView)
                                    .setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                        }
                    }
                }
            } else {
                tvSelectedTestCases.setText("");
                // 隐藏当前选择的测试项行
                ((android.view.View) tvSelectedTestCases.getParent()).setVisibility(android.view.View.GONE);
                // 恢复默认文字颜色
                tvSelectedTestCases.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
                // 恢复标题默认颜色
                android.view.View parentView = (android.view.View) tvSelectedTestCases.getParent();
                if (parentView instanceof android.widget.LinearLayout) {
                    android.widget.LinearLayout linearLayout = (android.widget.LinearLayout) parentView;
                    if (linearLayout.getChildCount() > 0) {
                        android.view.View titleView = linearLayout.getChildAt(0);
                        if (titleView instanceof android.widget.TextView) {
                            ((android.widget.TextView) titleView)
                                    .setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
                        }
                    }
                }
            }
        }
    }

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

    private void showFileNameInputDialogAndSave(String text) {
        // 自动生成默认文件名
        String defaultName = "tts_"
                + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new java.util.Date());

        DialogHelper.showInputDialog(this, R.string.input_audio_filename, defaultName,
                name -> {
                    if (name.isEmpty())
                        name = defaultName;
                    if (!name.endsWith(".wav"))
                        name += ".wav";
                    currentAudioFileName = name;
                    ToastHelper.showShort(this, R.string.toast_save_task_submitted);
                    pendingTtsAction = PendingTtsAction.PENDING_SAVE;
                    startSaveAudio(text);
                }, null);
    }

    private void startSaveAudio(String text) {
        isSavingAudio = true;
        btnSaveAudio.setEnabled(false);
        btnCancelSave.setEnabled(true);
        tempAudioFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), currentAudioFileName);
        Bundle ttsParams = new Bundle();
        ttsParams.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
        ttsParams.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_save");
        tts.setLanguage(currentLocale);
        tts.setSpeechRate(speechRate);
        tts.setPitch(pitch);
        tts.synthesizeToFile(text, ttsParams, tempAudioFile, "tts_save");
    }

    // 在TTS合成完成回调onDone/onError中处理拷贝和清理
    private boolean copyTempToSaveDir() {
        try {
            DocumentFile dir = DocumentFile.fromTreeUri(this, saveDirUri);
            if (dir == null || !dir.canWrite())
                return false;
            // 先删除同名文件
            DocumentFile old = dir.findFile(currentAudioFileName);
            if (old != null) {
                boolean deleted = old.delete();
                if (!deleted) {
                    Log.w("MainActivity", "旧音频文件删除失败: " + old.getUri());
                }
            }
            DocumentFile newFile = dir.createFile("audio/wav", currentAudioFileName);
            if (newFile == null)
                return false;
            try (OutputStream os = getContentResolver().openOutputStream(newFile.getUri());
                    FileInputStream fis = new FileInputStream(tempAudioFile)) {
                if (os == null) {
                    return false;
                }
                byte[] buf = new byte[4096];
                int len;
                while ((len = fis.read(buf)) > 0)
                    os.write(buf, 0, len);
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
            if (tts != null)
                tts.stop();
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
            ToastHelper.showShort(this, R.string.toast_cancel_save_success);
            ttsWorkState = TtsWorkState.IDLE;
            updateStatusInfo();
        }
    }

    // 新增：重置按钮状态更新方法
    private void updateResetButtons() {
        // 语速
        btnSpeedReset.setEnabled(Math.abs(speechRate - 1.0f) > 0.001f);
        // 音调
        btnPitchReset.setEnabled(Math.abs(pitch - 1.0f) > 0.001f);
        // 语言/发音人
        boolean isLangDefault = currentLocale != null && currentLocale.equals(defaultLocale);
        Voice currentVoice;
        if (tts != null) {
            currentVoice = tts.getVoice();
        } else {
            currentVoice = null; // 确保在tts为null时也能正确处理
        }
        Voice defaultVoice = languageDefaultVoices.get(currentLocale);
        boolean isVoiceDefault = (currentVoice != null && currentVoice.equals(defaultVoice));
        btnLangVoiceReset.setEnabled(!(isLangDefault && isVoiceDefault));
    }

    // 新增：根据TTS状态和isSavingAudio更新朗读和保存按钮的可用性
    private void updateSpeakAndSaveButtons() {
        if (ttsWorkState == TtsWorkState.SPEAKING) {
            btnSaveAudio.setEnabled(false);
            btnSpeak.setEnabled(true);
        } else if (ttsWorkState == TtsWorkState.SAVING || isSavingAudio) {
            btnSpeak.setEnabled(false);
            btnSaveAudio.setEnabled(false); // 正在保存音频时始终禁用
        } else {
            btnSpeak.setEnabled(isTtsReady);
            btnSaveAudio.setEnabled(isTtsReady);
        }
    }

    // 将SAF Uri转为可读路径，仅主存储primary支持
    private String getReadablePathFromUri(Uri uri) {
        if (uri == null)
            return "";
        String uriStr = uri.toString();
        if (uriStr.startsWith("content://com.android.externalstorage.documents/tree/primary%3A")) {
            String subPath = uriStr.substring(uriStr.indexOf("%3A") + 3);
            try {
                // 解码URL编码的字符，特别是中文字符
                String decodedPath = java.net.URLDecoder.decode(subPath, "UTF-8");
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
        if (btnSpeak != null)
            ButtonTextHelper.setupAutoTextSize(btnSpeak);
        if (btnStop != null)
            ButtonTextHelper.setupAutoTextSize(btnStop);
        if (btnSaveAudio != null)
            ButtonTextHelper.setupAutoTextSize(btnSaveAudio);
        if (btnCancelSave != null)
            ButtonTextHelper.setupAutoTextSize(btnCancelSave);

        // 设置按钮
        Button btnSetSaveDir = findViewById(R.id.btnSetSaveDir);
        if (btnSetSaveDir != null)
            ButtonTextHelper.setupAutoTextSize(btnSetSaveDir);
        if (btnSpeedReset != null)
            ButtonTextHelper.setupAutoTextSize(btnSpeedReset);
        if (btnPitchReset != null)
            ButtonTextHelper.setupAutoTextSize(btnPitchReset);
        if (btnLangVoiceReset != null)
            ButtonTextHelper.setupAutoTextSize(btnLangVoiceReset);

        // 编辑器按钮
        Button btnOpenEditor = findViewById(R.id.btnOpenEditor);
        if (btnOpenEditor != null)
            ButtonTextHelper.setupAutoTextSize(btnOpenEditor);
    }

    /**
     * 设置所有信息图标
     */
    private void setupInfoIcons() {
        // 使用批量设置方法，更简洁高效
        InfoIconHelper.setupInfoIcons(this,
                new Object[] { findViewById(R.id.ivLangSupportInfo), R.string.tts_support_info_title,
                        R.string.tts_support_info_content },
                new Object[] { findViewById(R.id.ivVoiceSupportInfo), R.string.voice_info_title,
                        R.string.voice_info_content },
                new Object[] { findViewById(R.id.ivSpeedInfo), R.string.speed_info_title, R.string.speed_info_content },
                new Object[] { findViewById(R.id.ivPitchInfo), R.string.pitch_info_title,
                        R.string.pitch_info_content });

        // 设置复制路径按钮点击事件
        btnCopySaveDir.setOnClickListener(v -> {
            if (saveDirUri != null) {
                String path = getReadablePathFromUri(saveDirUri);
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(
                        Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Audio Save Directory", path);
                clipboard.setPrimaryClip(clip);
                ToastHelper.showShort(this, R.string.toast_path_copied);
            }
        });
    }

    /**
     * 设置信息图标的动态位置
     */
    private void setupInfoIconPositions() {
        // 延迟执行，确保布局完成
        findViewById(android.R.id.content).post(() -> {
            // 设置语言信息图标位置
            ImageView ivLangInfo = findViewById(R.id.ivLangSupportInfo);
            TextView tvLangLabel = ViewHelper.findTextViewByText(this, getString(R.string.label_language));
            if (ivLangInfo != null && tvLangLabel != null) {
                InfoIconPositionHelper.setIconPosition(ivLangInfo, tvLangLabel);
            }

            // 设置发音人信息图标位置
            ImageView ivVoiceInfo = findViewById(R.id.ivVoiceSupportInfo);
            TextView tvVoiceLabel = ViewHelper.findTextViewByText(this, getString(R.string.label_voice));
            if (ivVoiceInfo != null && tvVoiceLabel != null) {
                InfoIconPositionHelper.setIconPosition(ivVoiceInfo, tvVoiceLabel);
            }

            // 设置语速信息图标位置
            ImageView ivSpeedInfo = findViewById(R.id.ivSpeedInfo);
            TextView tvSpeedLabel = ViewHelper.findTextViewByText(this, getString(R.string.label_speed));
            if (ivSpeedInfo != null && tvSpeedLabel != null) {
                InfoIconPositionHelper.setIconPosition(ivSpeedInfo, tvSpeedLabel);
            }

            // 设置音调信息图标位置
            ImageView ivPitchInfo = findViewById(R.id.ivPitchInfo);
            TextView tvPitchLabel = ViewHelper.findTextViewByText(this, getString(R.string.label_pitch));
            if (ivPitchInfo != null && tvPitchLabel != null) {
                InfoIconPositionHelper.setIconPosition(ivPitchInfo, tvPitchLabel);
            }
        });
    }

    /**
     * 初始化测试用例的国际化文本
     */
    private void initializeTestCases() {
        List<TestCase> testCases = TestManager.getInstance().getTestCases();
        for (TestCase tc : testCases) {
            if ("speed_pitch_failure".equals(tc.id)) {
                tc.name = getString(R.string.test_case_speed_pitch_failure);
                tc.description = getString(R.string.test_case_speed_pitch_failure_desc);
            } else if ("log_tts_voices".equals(tc.id)) {
                tc.name = getString(R.string.test_case_log_tts_voices);
                tc.description = getString(R.string.test_case_log_tts_voices_desc);
            }
        }
    }

    /**
     * 测试语速设置失败功能
     */
    private void testSpeedSettingFailure() {
        if (!TestManager.getInstance().isTestMode())
            return;

        // 检查是否选中了相关测试项
        boolean hasSpeedPitchTest = false;
        for (TestCase tc : TestManager.getInstance().getSelectedTestCases()) {
            if ("speed_pitch_failure".equals(tc.id)) {
                hasSpeedPitchTest = true;
                break;
            }
        }

        if (!hasSpeedPitchTest)
            return;

        // 模拟语速设置失败
        tvSpeedSetResult.setText(R.string.message_speed_set_failed);
        DelayedTaskHelper.clearTextDelayed(tvSpeedSetResult, Constants.TOAST_MESSAGE_DELAY);

        // 记录测试日志
        Log.i("TTS_TEST", "=== 语速设置失败测试 ===");
        Log.i("TTS_TEST", "模拟语速设置失败，显示警告文字");
        Log.i("TTS_TEST", "警告文字将在 " + Constants.TOAST_MESSAGE_DELAY + "ms 后自动清除");
        Log.i("TTS_TEST", "=== 语速设置失败测试完成 ===");
    }

    /**
     * 测试音调设置失败功能
     */
    private void testPitchSettingFailure() {
        if (!TestManager.getInstance().isTestMode())
            return;

        // 检查是否选中了相关测试项
        boolean hasSpeedPitchTest = false;
        for (TestCase tc : TestManager.getInstance().getSelectedTestCases()) {
            if ("speed_pitch_failure".equals(tc.id)) {
                hasSpeedPitchTest = true;
                break;
            }
        }

        if (!hasSpeedPitchTest)
            return;

        // 模拟音调设置失败
        tvPitchSetResult.setText(R.string.message_pitch_set_failed);
        DelayedTaskHelper.clearTextDelayed(tvPitchSetResult, Constants.TOAST_MESSAGE_DELAY);

        // 记录测试日志
        Log.i("TTS_TEST", "=== 音调设置失败测试 ===");
        Log.i("TTS_TEST", "模拟音调设置失败，显示警告文字");
        Log.i("TTS_TEST", "警告文字将在 " + Constants.TOAST_MESSAGE_DELAY + "ms 后自动清除");
        Log.i("TTS_TEST", "=== 音调设置失败测试完成 ===");
    }

    private void showLanguageSelectionDialog() {
        // 获取系统语言
        Locale systemLocale = LocaleHelper.getSystemLocale();
        boolean isSystemChinese = LocaleHelper.isChinese(systemLocale);
        boolean isSystemEnglish = LocaleHelper.isEnglish(systemLocale);

        String[] languages = {
                getString(R.string.language_follow_system),
                getString(R.string.language_chinese),
                getString(R.string.language_english)
        };

        // 创建可选择的项目列表
        boolean[] enabledItems = { true, true, true };

        // 如果系统语言是中文，禁用中文选项
        if (isSystemChinese) {
            enabledItems[1] = false;
        }
        // 如果系统语言是英文，禁用英文选项
        if (isSystemEnglish) {
            enabledItems[2] = false;
        }

        int currentMode = LocaleHelper.getLanguageMode(this);
        int checkedItem = currentMode == LocaleHelper.MODE_FOLLOW_SYSTEM ? 0
                : (LocaleHelper.isChinese(LocaleHelper.getCurrentLocale(this)) ? 1 : 2);

        // 如果当前选中的项目被禁用，改为选择"跟随系统"
        if (!enabledItems[checkedItem]) {
            checkedItem = 0;
        }

        // 创建自定义对话框布局
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.language_selection_dialog,
                findViewById(android.R.id.content), false);
        android.widget.ImageView ivLanguageInfo = dialogView.findViewById(R.id.ivLanguageInfo);
        android.widget.TextView tvLanguageDialogTitle = dialogView.findViewById(R.id.tvLanguageDialogTitle);

        // 设置信息图标点击事件和内容（用InfoIconHelper）
        com.citadawn.speechapp.util.InfoIconHelper.setupInfoIcons(this,
                new Object[] { ivLanguageInfo, R.string.language_selection_info_title,
                        R.string.language_selection_info_message });
        // 动态定位信息图标（用InfoIconPositionHelper）
        dialogView.post(() -> {
            if (ivLanguageInfo != null && tvLanguageDialogTitle != null) {
                com.citadawn.speechapp.util.InfoIconPositionHelper.setIconPosition(ivLanguageInfo,
                        tvLanguageDialogTitle);
            }
        });

        // 创建自定义适配器
        android.widget.BaseAdapter adapter = new android.widget.BaseAdapter() {
            @Override
            public int getCount() {
                return languages.length;
            }

            @Override
            public Object getItem(int position) {
                return languages[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public android.view.View getView(int position, android.view.View convertView,
                    android.view.ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_single_choice, parent,
                            false);
                }

                android.widget.TextView textView = convertView.findViewById(android.R.id.text1);
                textView.setText(languages[position]);

                // 设置禁用项目的样式
                if (!enabledItems[position]) {
                    textView.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.darker_gray));
                    textView.setEnabled(false);
                } else {
                    textView.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.black));
                    textView.setEnabled(true);
                }

                return convertView;
            }

            @Override
            public boolean isEnabled(int position) {
                return enabledItems[position];
            }
        };

        new AlertDialog.Builder(this)
                .setCustomTitle(dialogView)
                .setSingleChoiceItems(adapter, checkedItem, (dialog, which) -> {
                    // 如果选中的项目被禁用，不允许选择
                    if (!enabledItems[which]) {
                        return;
                    }

                    int newMode;
                    Locale newLocale;

                    switch (which) {
                        case 0: // 跟随系统 Follow system
                            newMode = LocaleHelper.MODE_FOLLOW_SYSTEM;
                            newLocale = LocaleHelper.getSystemLocale();
                            break;
                        case 1: // 中文 Chinese
                            newMode = LocaleHelper.MODE_MANUAL;
                            newLocale = new Locale("zh", "CN");
                            break;
                        case 2: // 英文 English
                            newMode = LocaleHelper.MODE_MANUAL;
                            newLocale = new Locale("en", "US");
                            break;
                        default:
                            return;
                    }

                    // 检查是否需要切换语言 Check if language needs to be changed
                    Locale currentLocale = LocaleHelper.getCurrentLocale(this);
                    if (!newLocale.equals(currentLocale)) {
                        LocaleHelper.setLanguageMode(this, newMode, newLocale);
                        ToastHelper.showShort(this, R.string.toast_language_changed);

                        // 重新创建Activity以应用新语言 Recreate activity to apply new language
                        recreate();
                    }

                    dialog.dismiss();
                })
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .show();
    }
    // endregion

    // 新增logTtsVoices方法

    /**
     * 输出TTS引擎所有语言和发音人信息到logcat，标注默认项
     */
    private void logTtsVoices() {
        if (tts == null)
            return;
        try {
            Locale defaultLang = tts.getDefaultVoice() != null ? tts.getDefaultVoice().getLocale()
                    : Locale.getDefault();
            Voice defaultVoice = tts.getDefaultVoice();
            Voice currentVoice = tts.getVoice();
            Set<Voice> voices = tts.getVoices();
            Map<Locale, List<Voice>> localeVoices = new HashMap<>();
            for (Voice v : voices) {
                Locale l = v.getLocale();
                localeVoices.computeIfAbsent(l, k -> new ArrayList<>()).add(v);
            }
            Log.i("TTS_TEST", "==== TTS Engine Supported Languages and Voices ====");
            Log.i("TTS_TEST", "Default Language: " + (defaultLang != null ? defaultLang : "null"));
            Log.i("TTS_TEST", "Default Voice: " + (defaultVoice != null ? defaultVoice.getName() : "null"));
            // 新增：直接输出当前TTS实例使用的voice对象
            Log.i("TTS_TEST", "Current TTS Voice: " + currentVoice);
            for (Locale locale : localeVoices.keySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append("[Language] ").append(locale);
                if (locale.equals(defaultLang))
                    sb.append("  <== DEFAULT");
                Log.i("TTS_TEST", sb.toString());
                List<Voice> vlist = localeVoices.get(locale);
                Voice langDefaultVoice = languageDefaultVoices.get(locale);
                if (vlist != null) {
                    for (Voice v : vlist) {
                        StringBuilder vinfo = new StringBuilder();
                        vinfo.append("    - Voice: ").append(v.getName());
                        if (defaultVoice != null && v.getName().equals(defaultVoice.getName()))
                            vinfo.append("  <== DEFAULT");
                        if (langDefaultVoice != null && v.getName().equals(langDefaultVoice.getName()))
                            vinfo.append("  <== DEFAULT for this language");
                        Log.i("TTS_TEST", vinfo.toString());
                    }
                }
            }
            Log.i("TTS_TEST", "===============================================");
        } catch (Exception e) {
            Log.e("TTS_TEST", "logTtsVoices error", e);
        }
    }
}
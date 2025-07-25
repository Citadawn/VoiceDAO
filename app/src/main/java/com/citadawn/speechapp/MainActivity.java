package com.citadawn.speechapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.speech.tts.Voice;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import android.content.SharedPreferences;
import androidx.documentfile.provider.DocumentFile;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.WindowInsets;

import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.text.Collator;
import java.util.Locale;
import java.util.Set;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
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
    private ActivityResultLauncher<Intent> createFileLauncher;
    private String pendingAudioText = null;
    private TextView textSpeechRateValue, textPitchValue;
    private TextView btnSpeedMinus, btnSpeedPlus, btnPitchMinus, btnPitchPlus;
    private Button btnSpeedReset, btnPitchReset;
    private TextView tvSpeedSetResult, tvPitchSetResult;
    private Spinner spinnerLanguage, spinnerVoice;
    private Button btnLangVoiceReset;
    private Locale defaultLocale = null; // 默认语言，将通过TTS API获取
    private Voice globalDefaultVoice = null; // 全局默认发音人
    private final HashMap<Locale, Voice> languageDefaultVoices = new HashMap<>(); // 每个语言的默认发音人
    private final ArrayList<Locale> localeList = new ArrayList<>();
    private final ArrayList<Voice> voiceList = new ArrayList<>();
    private ArrayAdapter<String> langAdapter;
    private ArrayAdapter<String> voiceAdapter;
    private boolean isLangSpinnerInit = false;
    private boolean isVoiceSpinnerInit = false;
    private TextView tvTtsEngineStatus, tvAudioSaveDir, tvTtsSpeakStatus;
    private final Handler ttsStatusHandler = new Handler(Looper.getMainLooper());
    private Runnable ttsStatusRunnable;
    private Uri pendingAudioUri = null;
    private static final String PREFS_NAME = "tts_prefs";
    private static final String KEY_SAVE_DIR_URI = "save_dir_uri";
    private static final String AUDIO_FILE_NAME = "tts_output.wav";
    private static final String TEMP_FILE_NAME = "tts_temp.wav";
    private Uri saveDirUri = null;
    private Button btnSetSaveDir, btnCancelSave;
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

    // 删除：testHandler和testIsSpeakingRunnable相关的所有测试代码

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // 始终浅色
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 设置自定义Toolbar为ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_more_vert_white_24dp));

        // 动态设置statusBarSpacer高度为状态栏高度
        // 删除与statusBarSpacer相关的所有代码
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editText = findViewById(R.id.editText);
        View rootView = findViewById(R.id.main);
        // 点击空白处收起键盘并让EditText失去焦点
        rootView.setOnClickListener(v -> {
            editText.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        });
        // 删除顶部自动适配WindowInsets的代码，因为现在用ScrollView了
        btnSpeak = findViewById(R.id.btnSpeak);
        seekBarSpeed = findViewById(R.id.seekBarSpeed);
        seekBarPitch = findViewById(R.id.seekBarPitch);
        tvTtsEngineStatus = findViewById(R.id.tvTtsEngineStatus);
        tvAudioSaveDir = findViewById(R.id.tvAudioSaveDir);
        tvTtsSpeakStatus = findViewById(R.id.tvTtsSpeakStatus);
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
        btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(v -> {
            if (tts != null && isTtsReady) {
                tts.stop();
                ttsWorkState = TtsWorkState.IDLE;
                tvTtsSpeakStatus.setText("空闲");
                tvTtsSpeakStatus
                        .setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_green_dark));
            }
        });

        btnSaveAudio = findViewById(R.id.btnSaveAudio);
        // SAF文件选择器回调
        createFileLauncher = registerForActivityResult(
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
                Toast.makeText(this, R.string.status_not_ready, Toast.LENGTH_SHORT).show();
                return;
            }
            String text = editText.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, R.string.hint_input_save_text, Toast.LENGTH_SHORT).show();
                return;
            }
            if (saveDirUri == null) {
                Toast.makeText(this, R.string.toast_no_save_dir, Toast.LENGTH_SHORT).show();
                return;
            }
            if (isSavingAudio) {
                Toast.makeText(this, R.string.toast_saving_audio, Toast.LENGTH_SHORT).show();
                return;
            }
            btnSpeak.setEnabled(false);
            if (text.length() > 3500) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_title_warning)
                        .setMessage(R.string.dialog_message_text_too_long)
                        .setPositiveButton(R.string.dialog_button_continue, (dialog, which) -> {
                            showFileNameInputDialogAndSave(text);
                        })
                        .setNegativeButton(R.string.dialog_button_cancel, (dialog, which) -> {
                            Toast.makeText(this, R.string.toast_reduce_text, Toast.LENGTH_SHORT).show();
                        })
                        .show();
                return;
            }
            showFileNameInputDialogAndSave(text);
        });
        btnSetSaveDir = findViewById(R.id.btnSetSaveDir);
        btnCancelSave = findViewById(R.id.btnCancelSave);
        btnCancelSave.setEnabled(false);
        // 读取保存目录Uri
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String uriStr = prefs.getString(KEY_SAVE_DIR_URI, null);
        if (uriStr != null) {
            saveDirUri = Uri.parse(uriStr);
        }
        btnSetSaveDir.setOnClickListener(v -> openSaveDirPicker());
        btnCancelSave.setOnClickListener(v -> confirmCancelSave());

        textSpeechRateValue = findViewById(R.id.textSpeechRateValue);
        textPitchValue = findViewById(R.id.textPitchValue);
        btnSpeedMinus = findViewById(R.id.btnSpeedMinus);
        btnSpeedPlus = findViewById(R.id.btnSpeedPlus);
        btnPitchMinus = findViewById(R.id.btnPitchMinus);
        btnPitchPlus = findViewById(R.id.btnPitchPlus);
        btnSpeedReset = findViewById(R.id.btnSpeedReset);
        btnPitchReset = findViewById(R.id.btnPitchReset);
        tvSpeedSetResult = findViewById(R.id.tvSpeedSetResult);
        tvPitchSetResult = findViewById(R.id.tvPitchSetResult);

        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        spinnerVoice = findViewById(R.id.spinnerVoice);
        btnLangVoiceReset = findViewById(R.id.btnLangVoiceReset);
        // 启动定时任务，定期检查TTS状态
        ttsStatusRunnable = new Runnable() {
            @Override
            public void run() {
                // 优先显示准备状态
                if (pendingTtsAction == PendingTtsAction.PENDING_SPEAK) {
                    tvTtsSpeakStatus.setText("准备开始朗读……");
                    tvTtsSpeakStatus
                            .setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_orange_dark));
                } else if (pendingTtsAction == PendingTtsAction.PENDING_SAVE) {
                    tvTtsSpeakStatus.setText("准备开始保存音频……");
                    tvTtsSpeakStatus
                            .setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_orange_dark));
                } else if (ttsWorkState == TtsWorkState.SPEAKING) {
                    tvTtsSpeakStatus.setText("正在朗读");
                    tvTtsSpeakStatus
                            .setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_dark));
                } else if (ttsWorkState == TtsWorkState.SAVING) {
                    tvTtsSpeakStatus.setText("正在保存音频");
                    tvTtsSpeakStatus
                            .setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_blue_dark));
                } else {
                    tvTtsSpeakStatus.setText("空闲");
                    tvTtsSpeakStatus
                            .setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_green_dark));
                }
                btnStop.setEnabled(ttsWorkState == TtsWorkState.SPEAKING);
                updateSpeakAndSaveButtons();
                ttsStatusHandler.postDelayed(this, 300);
            }
        };
        ttsStatusHandler.post(ttsStatusRunnable);
        // 初始化语言和发音人Spinner
        langAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, new ArrayList<>());
        langAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerLanguage.setAdapter(langAdapter);
        voiceAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, new ArrayList<>());
        voiceAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerVoice.setAdapter(voiceAdapter);

        // 语速调节
        seekBarSpeed.setMax(15); // 0.5~2.0，步进0.1
        seekBarSpeed.setProgress(5); // 默认1.0
        // 在语速相关变化后调用
        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = 0.5f + progress * 0.1f;
                value = Math.round(value * 10f) / 10f; // 保留一位小数
                textSpeechRateValue.setText(String.format("%.2f", value));
                speechRate = value;
                updateResetButtons();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        btnSpeedMinus.setOnClickListener(v -> {
            float value = Float.parseFloat(textSpeechRateValue.getText().toString());
            value -= 0.01f;
            if (value < 0.5f)
                value = 0.5f;
            value = Math.round(value * 100f) / 100f; // 保留两位小数
            textSpeechRateValue.setText(String.format("%.2f", value));
            speechRate = value;
            int progress = Math.round((value - 0.5f) / 0.1f);
            seekBarSpeed.setProgress(progress);
            if (tts != null && isTtsReady) {
                int result = tts.setSpeechRate(speechRate);
                if (result != TextToSpeech.SUCCESS) {
                    tvSpeedSetResult.setText(R.string.message_speed_set_failed);
                    tvSpeedSetResult.postDelayed(() -> tvSpeedSetResult.setText(""), 500);
                }
            }
            updateResetButtons();
        });
        btnSpeedPlus.setOnClickListener(v -> {
            float value = Float.parseFloat(textSpeechRateValue.getText().toString());
            value += 0.01f;
            if (value > 2.0f)
                value = 2.0f;
            value = Math.round(value * 100f) / 100f;
            textSpeechRateValue.setText(String.format("%.2f", value));
            speechRate = value;
            int progress = Math.round((value - 0.5f) / 0.1f);
            seekBarSpeed.setProgress(progress);
            if (tts != null && isTtsReady) {
                int result = tts.setSpeechRate(speechRate);
                if (result != TextToSpeech.SUCCESS) {
                    tvSpeedSetResult.setText(R.string.message_speed_set_failed);
                    tvSpeedSetResult.postDelayed(() -> tvSpeedSetResult.setText(""), 500);
                }
            }
            updateResetButtons();
        });
        btnSpeedReset.setOnClickListener(v -> {
            seekBarSpeed.setProgress(5);
            textSpeechRateValue.setText("1.00");
            speechRate = 1.0f;
            if (tts != null && isTtsReady) {
                int result = tts.setSpeechRate(speechRate);
                if (result != TextToSpeech.SUCCESS) {
                    tvSpeedSetResult.setText(R.string.message_speed_set_failed);
                    tvSpeedSetResult.postDelayed(() -> tvSpeedSetResult.setText(""), 500);
                }
            }
            updateResetButtons();
        });

        // 音调调节
        seekBarPitch.setMax(15);
        seekBarPitch.setProgress(5);
        // 在音调相关变化后调用
        seekBarPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = 0.5f + progress * 0.1f;
                value = Math.round(value * 10f) / 10f; // 保留一位小数
                textPitchValue.setText(String.format("%.2f", value));
                pitch = value;
                updateResetButtons();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        btnPitchMinus.setOnClickListener(v -> {
            float value = Float.parseFloat(textPitchValue.getText().toString());
            value -= 0.01f;
            if (value < 0.5f)
                value = 0.5f;
            value = Math.round(value * 100f) / 100f;
            textPitchValue.setText(String.format("%.2f", value));
            pitch = value;
            int progress = Math.round((value - 0.5f) / 0.1f);
            seekBarPitch.setProgress(progress);
            if (tts != null && isTtsReady) {
                int result = tts.setPitch(pitch);
                if (result != TextToSpeech.SUCCESS) {
                    tvPitchSetResult.setText(R.string.message_pitch_set_failed);
                    tvPitchSetResult.postDelayed(() -> tvPitchSetResult.setText(""), 500);
                }
            }
            updateResetButtons();
        });
        btnPitchPlus.setOnClickListener(v -> {
            float value = Float.parseFloat(textPitchValue.getText().toString());
            value += 0.01f;
            if (value > 2.0f)
                value = 2.0f;
            value = Math.round(value * 100f) / 100f;
            textPitchValue.setText(String.format("%.2f", value));
            pitch = value;
            int progress = Math.round((value - 0.5f) / 0.1f);
            seekBarPitch.setProgress(progress);
            if (tts != null && isTtsReady) {
                int result = tts.setPitch(pitch);
                if (result != TextToSpeech.SUCCESS) {
                    tvPitchSetResult.setText(R.string.message_pitch_set_failed);
                    tvPitchSetResult.postDelayed(() -> tvPitchSetResult.setText(""), 500);
                }
            }
            updateResetButtons();
        });
        btnPitchReset.setOnClickListener(v -> {
            seekBarPitch.setProgress(5);
            textPitchValue.setText("1.00");
            pitch = 1.0f;
            if (tts != null && isTtsReady) {
                int result = tts.setPitch(pitch);
                if (result != TextToSpeech.SUCCESS) {
                    tvPitchSetResult.setText(R.string.message_pitch_set_failed);
                    tvPitchSetResult.postDelayed(() -> tvPitchSetResult.setText(""), 500);
                }
            }
            updateResetButtons();
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // API 21+ 使用 getDefaultVoice().getLocale() 获取默认语言
                    Voice defaultVoice = tts.getDefaultVoice();
                    if (defaultVoice != null) {
                        defaultLocale = defaultVoice.getLocale();
                        this.globalDefaultVoice = defaultVoice;
                        currentLocale = defaultLocale; // 使用默认语言作为当前语言
                    } else {
                        // 如果获取不到默认发音人，使用系统默认语言
                        defaultLocale = Locale.getDefault();
                        currentLocale = defaultLocale;
                    }
                } else {
                    // API 21 以下使用已废弃的 getDefaultLanguage()
                    defaultLocale = tts.getDefaultLanguage();
                    currentLocale = defaultLocale;
                }

                int result = tts.setLanguage(currentLocale);
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
                                Toast.makeText(MainActivity.this, R.string.toast_tts_speak_done, Toast.LENGTH_SHORT)
                                        .show();
                                updateSpeakAndSaveButtons();
                            });
                        } else if ("tts_save".equals(utteranceId)) {
                            runOnUiThread(() -> {
                                if (saveDirUri != null && tempAudioFile != null && tempAudioFile.exists()) {
                                    boolean ok = copyTempToSaveDir();
                                    if (ok) {
                                        Toast.makeText(MainActivity.this, R.string.toast_save_audio_success,
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, R.string.toast_save_audio_write_fail,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    tempAudioFile.delete();
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
                        ttsWorkState = TtsWorkState.IDLE;
                        pendingTtsAction = PendingTtsAction.NONE;
                        if ("tts_speak".equals(utteranceId)) {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, R.string.toast_tts_speak_error, Toast.LENGTH_SHORT)
                                        .show();
                                updateSpeakAndSaveButtons();
                            });
                        } else if ("tts_save".equals(utteranceId)) {
                            runOnUiThread(() -> {
                                if (tempAudioFile != null && tempAudioFile.exists())
                                    tempAudioFile.delete();
                                Toast.makeText(MainActivity.this, R.string.toast_save_audio_synth_fail,
                                        Toast.LENGTH_SHORT).show();
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Set<Locale> locales = tts.getAvailableLanguages();
                    localeList.clear();
                    langAdapter.clear();
                    int defaultIndex = 0, idx = 0;
                    for (Locale locale : locales) {
                        localeList.add(locale);
                    }
                    // 按语言名称排序（默认语言排在最前面）
                    Collator collator = Collator.getInstance(Locale.CHINESE);
                    localeList.sort((l1, l2) -> {
                        // 默认语言始终排在最前面
                        if (l1.equals(defaultLocale))
                            return -1;
                        if (l2.equals(defaultLocale))
                            return 1;
                        // 其他语言按拼音排序
                        return collator.compare(l1.getDisplayName(), l2.getDisplayName());
                    });
                    for (Locale locale : localeList) {
                        String name = locale.getDisplayName();
                        if (locale.equals(defaultLocale)) {
                            // 如果使用的是系统默认语言且没有获取到TTS默认发音人，显示特殊标识
                            if (defaultLocale.equals(Locale.getDefault()) && globalDefaultVoice == null) {
                                name += "（系统默认）";
                            } else {
                                name += "（默认）";
                            }
                            defaultIndex = 0; // 默认语言现在总是在第一位
                        }
                        langAdapter.add(name);
                        idx++;
                    }
                    langAdapter.notifyDataSetChanged();
                    isLangSpinnerInit = true;
                    spinnerLanguage.setSelection(defaultIndex);
                }
                // 初始化每个语言的默认发音人
                initializeLanguageDefaultVoices();
                // 获取可用发音人
                updateVoiceList(currentLocale, true);

                // 输出语言列表和默认发音人
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                    sb.append("语言列表：\n");
                    for (Locale locale : sortedLocales) {
                        String display = locale.getDisplayName() + " (" + locale.toLanguageTag() + ")";
                        Voice defVoice = languageDefaultVoices.get(locale);
                        if (locale.equals(defaultLocale)) {
                            sb.append(display).append("  默认发音人: ");
                        } else {
                            sb.append(display).append(" 的默认发音人: ");
                        }
                        sb.append(defVoice != null ? defVoice.getName() : "无").append("\n");
                    }
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
                Toast.makeText(this, R.string.status_not_ready, Toast.LENGTH_SHORT).show();
                return;
            }
            String text = editText.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, R.string.hint_input_text, Toast.LENGTH_SHORT).show();
                return;
            }
            btnSaveAudio.setEnabled(false);
            if (text.length() > 3500) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_title_warning)
                        .setMessage(R.string.dialog_message_text_too_long)
                        .setPositiveButton(R.string.dialog_button_continue, (dialog, which) -> {
                            Toast.makeText(this, R.string.toast_read_task_submitted, Toast.LENGTH_SHORT).show();
                            pendingTtsAction = PendingTtsAction.PENDING_SPEAK;
                            tts.setLanguage(currentLocale);
                            tts.setSpeechRate(speechRate);
                            tts.setPitch(pitch);
                            Bundle params = new Bundle();
                            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_speak");
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "tts_speak");
                        })
                        .setNegativeButton(R.string.dialog_button_cancel, (dialog, which) -> {
                            if (tts != null && isTtsReady)
                                tts.stop();
                            Toast.makeText(this, R.string.toast_reduce_text, Toast.LENGTH_SHORT).show();
                        })
                        .show();
                return;
            }
            Toast.makeText(this, R.string.toast_read_task_submitted, Toast.LENGTH_SHORT).show();
            pendingTtsAction = PendingTtsAction.PENDING_SPEAK;
            tts.setLanguage(currentLocale);
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
                btnClear.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
        // 初始化时也判断一次
        btnClear.setEnabled(editText.getText().toString().length() > 0);
        // 初始化时也调用一次
        updateResetButtons();
        // 删除：testHandler.post(testIsSpeakingRunnable);
    }

    @Override
    protected void onDestroy() {
        ttsStatusHandler.removeCallbacksAndMessages(null);
        // 移除测试定时器回调，防止泄漏
        // testHandler.removeCallbacksAndMessages(null); // 删除
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_tts_settings) {
            try {
                Intent intent = new Intent("com.android.settings.TTS_SETTINGS");
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, R.string.toast_cannot_open_tts_settings, Toast.LENGTH_SHORT).show();
            }
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

    // 合成到指定uri
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void synthesizeTextToUri(String text, Uri uri) {
        pendingAudioUri = uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10+
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
                Toast.makeText(this, getString(R.string.message_save_failed, e.getMessage()), Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // Android 5.0~9.0
            File tempWav = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "tts_temp.wav");
            HashMap<String, String> ttsParams = new HashMap<>();
            ttsParams.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "1.0");
            ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_save");
            tts.setLanguage(currentLocale);
            tts.setSpeechRate(speechRate);
            tts.setPitch(pitch);
            tts.synthesizeToFile(text, ttsParams, tempWav.getAbsolutePath());
        } else {
            Toast.makeText(this, R.string.message_android_version_not_support, Toast.LENGTH_SHORT).show();
        }
    }

    // 拷贝文件到SAF Uri
    private void copyFileToUri(File src, Uri uri, String mimeType) {
        try (FileInputStream fis = new FileInputStream(src);
                OutputStream os = getContentResolver().openOutputStream(uri)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
            os.flush();
            Toast.makeText(this, R.string.toast_save_audio_success, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.message_copy_audio_failed, e.getMessage()), Toast.LENGTH_SHORT)
                    .show();
        }
        src.delete();
    }

    // 初始化每个语言的默认发音人
    private void initializeLanguageDefaultVoices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Set<Voice> voices = tts.getVoices();
            languageDefaultVoices.clear();

            // 按语言分组，为每个语言确定默认发音人
            HashMap<Locale, ArrayList<Voice>> voicesByLanguage = new HashMap<>();

            for (Voice voice : voices) {
                Locale locale = voice.getLocale();
                if (!voicesByLanguage.containsKey(locale)) {
                    voicesByLanguage.put(locale, new ArrayList<>());
                }
                voicesByLanguage.get(locale).add(voice);
            }

            // 为每个语言确定默认发音人（通常是第一个）
            for (Map.Entry<Locale, ArrayList<Voice>> entry : voicesByLanguage.entrySet()) {
                Locale locale = entry.getKey();
                ArrayList<Voice> voiceList = entry.getValue();

                if (!voiceList.isEmpty()) {
                    // 如果全局默认发音人是这个语言的，优先使用它
                    Voice defaultVoice = null;
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
    }

    // 更新发音人列表
    private void updateVoiceList(Locale locale, boolean resetToDefault) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Set<Voice> voices = tts.getVoices();
            voiceList.clear();
            voiceAdapter.clear();
            int defaultIndex = 0, idx = 0;

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

            for (Voice voice : voiceList) {
                String name = voice.getName();
                // 去除下划线及后缀数字
                name = name.replaceAll("_[0-9]+$", "");
                // 检查是否是当前语言的默认发音人
                boolean isDefault = (voice.equals(currentLangDefaultVoice));
                if (isDefault) {
                    name += "（默认）";
                    defaultIndex = 0; // 默认发音人现在总是在第一位
                }
                voiceAdapter.add(name);
                idx++;
            }
            voiceAdapter.notifyDataSetChanged();
            isVoiceSpinnerInit = true;
            if (resetToDefault && currentLangDefaultVoice != null) {
                spinnerVoice.setSelection(0); // 默认发音人现在总是在第一位
                tts.setVoice(currentLangDefaultVoice);
            } else if (!voiceList.isEmpty()) {
                spinnerVoice.setSelection(0);
                tts.setVoice(voiceList.get(0));
            }
        }
    }

    // 更新状态信息显示区域
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
        } else {
            tvAudioSaveDir.setText("未设置");
        }
        // 语音合成状态（此处只初始化，动态状态由其它逻辑控制）
        // tvTtsSpeakStatus.setText("空闲"); // 由其它逻辑动态设置
    }

    private void openSaveDirPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try {
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    saveDirUri = uri;
                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString(KEY_SAVE_DIR_URI, uri.toString());
                    editor.apply();
                    Toast.makeText(this, R.string.toast_save_dir_set_success, Toast.LENGTH_SHORT).show();
                    updateStatusInfo();
                } catch (Exception e) {
                    Toast.makeText(this, R.string.toast_save_dir_set_fail, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showFileNameInputDialogAndSave(String text) {
        // 自动生成默认文件名
        String defaultName = "tts_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        final EditText input = new EditText(this);
        input.setText(defaultName);
        input.setSelection(defaultName.length());
        new AlertDialog.Builder(this)
                .setTitle("输入音频文件名")
                .setView(input)
                .setPositiveButton("保存", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty())
                        name = defaultName;
                    if (!name.endsWith(".wav"))
                        name += ".wav";
                    currentAudioFileName = name;
                    Toast.makeText(this, R.string.toast_save_task_submitted, Toast.LENGTH_SHORT).show();
                    pendingTtsAction = PendingTtsAction.PENDING_SAVE;
                    startSaveAudio(text);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void startSaveAudio(String text) {
        isSavingAudio = true;
        btnSaveAudio.setEnabled(false);
        btnCancelSave.setEnabled(true);
        tempAudioFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), currentAudioFileName);
        HashMap<String, String> ttsParams = new HashMap<>();
        ttsParams.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "1.0");
        ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_save");
        tts.setLanguage(currentLocale);
        tts.setSpeechRate(speechRate);
        tts.setPitch(pitch);
        tts.synthesizeToFile(text, ttsParams, tempAudioFile.getAbsolutePath());
    }

    // 在TTS合成完成回调onDone/onError中处理拷贝和清理
    private boolean copyTempToSaveDir() {
        try {
            DocumentFile dir = DocumentFile.fromTreeUri(this, saveDirUri);
            if (dir == null || !dir.canWrite())
                return false;
            // 先删除同名文件
            DocumentFile old = dir.findFile(currentAudioFileName);
            if (old != null)
                old.delete();
            DocumentFile newFile = dir.createFile("audio/wav", currentAudioFileName);
            if (newFile == null)
                return false;
            try (OutputStream os = getContentResolver().openOutputStream(newFile.getUri());
                    FileInputStream fis = new FileInputStream(tempAudioFile)) {
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
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_warning)
                .setMessage(R.string.dialog_message_cancel_save)
                .setPositiveButton(R.string.dialog_button_cancel_save, (dialog, which) -> cancelSaveAudio())
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .show();
    }

    private void cancelSaveAudio() {
        if (isSavingAudio) {
            if (tts != null)
                tts.stop();
            if (tempAudioFile != null && tempAudioFile.exists())
                tempAudioFile.delete();
            if (saveDirUri != null) {
                DocumentFile dir = DocumentFile.fromTreeUri(this, saveDirUri);
                if (dir != null) {
                    DocumentFile file = dir.findFile(currentAudioFileName);
                    if (file != null)
                        file.delete();
                }
            }
            isSavingAudio = false;
            btnSaveAudio.setEnabled(true);
            btnCancelSave.setEnabled(false);
            updateSpeakAndSaveButtons();
            Toast.makeText(this, R.string.toast_cancel_save_success, Toast.LENGTH_SHORT).show();
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
        Voice currentVoice = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && tts != null) {
            currentVoice = tts.getVoice();
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
            return "/storage/emulated/0/" + subPath.replace("%2F", "/");
        }
        return uriStr;
    }
}
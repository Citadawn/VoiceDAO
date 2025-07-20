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

import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    private TextView tvStatus;
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
    private Button btnSpeedMinus, btnSpeedPlus, btnPitchMinus, btnPitchPlus, btnSpeedReset, btnPitchReset;
    private TextView tvSpeedSetResult, tvPitchSetResult;
    private Spinner spinnerLanguage, spinnerVoice;
    private Button btnLangVoiceReset;
    private Locale defaultLocale = null; // 默认语言，将通过TTS API获取
    private Voice globalDefaultVoice = null; // 全局默认发音人
    private HashMap<Locale, Voice> languageDefaultVoices = new HashMap<>(); // 每个语言的默认发音人
    private ArrayList<Locale> localeList = new ArrayList<>();
    private ArrayList<Voice> voiceList = new ArrayList<>();
    private ArrayAdapter<String> langAdapter;
    private ArrayAdapter<String> voiceAdapter;
    private boolean isLangSpinnerInit = false;
    private boolean isVoiceSpinnerInit = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editText = findViewById(R.id.editText);
        // 顶部自动适配WindowInsets，保证不同机型间距一致
        View mainLayout = findViewById(R.id.main);
        mainLayout.setOnApplyWindowInsetsListener((v, insets) -> {
            int topInset = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            } else {
                topInset = insets.getSystemWindowInsetTop();
            }
            int baseMargin = (int) (getResources().getDisplayMetrics().density * 32); // 32dp
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) editText.getLayoutParams();
            lp.topMargin = topInset + baseMargin;
            editText.setLayoutParams(lp);
            return insets;
        });
        btnSpeak = findViewById(R.id.btnSpeak);
        seekBarSpeed = findViewById(R.id.seekBarSpeed);
        seekBarPitch = findViewById(R.id.seekBarPitch);
        tvStatus = findViewById(R.id.tvStatus);
        btnStop = findViewById(R.id.btnStop);
        btnSaveAudio = findViewById(R.id.btnSaveAudio);
        // TTS未初始化时按钮不可用
        btnSpeak.setEnabled(false);
        btnStop.setEnabled(false);
        btnSaveAudio.setEnabled(false);
        updateStatusInfo();
        btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(v -> editText.setText(""));
        btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(v -> {
            if (tts != null && isTtsReady) {
                tts.stop();
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
            String text = editText.getText().toString();
            if (text.isEmpty()) {
                Toast.makeText(this, R.string.hint_input_save_text, Toast.LENGTH_SHORT).show();
                return;
            }
            if (text.length() > 3500) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_title_warning)
                        .setMessage(R.string.dialog_message_text_too_long)
                        .setPositiveButton(R.string.dialog_button_continue, (dialog, which) -> {
                            // 弹出SAF文件管理器
                            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("audio/wav");
                            intent.putExtra(Intent.EXTRA_TITLE, "tts_output.wav");
                            pendingAudioText = text;
                            createFileLauncher.launch(intent);
                            Toast.makeText(this, R.string.toast_please_wait, Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(R.string.dialog_button_cancel, (dialog, which) -> {
                            Toast.makeText(this, R.string.toast_reduce_text, Toast.LENGTH_SHORT).show();
                        })
                        .show();
                return;
            }
            // 弹出SAF文件管理器
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/wav");
            intent.putExtra(Intent.EXTRA_TITLE, "tts_output.wav");
            pendingAudioText = text;
            createFileLauncher.launch(intent);
        });

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
        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = 0.5f + progress * 0.1f;
                value = Math.round(value * 10f) / 10f; // 保留一位小数
                textSpeechRateValue.setText(String.format("%.2f", value));
                speechRate = value;
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
        });
        btnSpeedReset.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                    .setDuration(80)
                    .withEndAction(() -> v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .start())
                    .start();
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
        });

        // 音调调节
        seekBarPitch.setMax(15);
        seekBarPitch.setProgress(5);
        seekBarPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = 0.5f + progress * 0.1f;
                value = Math.round(value * 10f) / 10f; // 保留一位小数
                textPitchValue.setText(String.format("%.2f", value));
                pitch = value;
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
        });
        btnPitchReset.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                    .setDuration(80)
                    .withEndAction(() -> v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .start())
                    .start();
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        btnLangVoiceReset.setOnClickListener(v -> {
            v.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(80)
                .withEndAction(() -> v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(80)
                    .start())
                .start();
            if (defaultLocale != null) {
                int idx = localeList.indexOf(defaultLocale);
                if (idx >= 0)
                    spinnerLanguage.setSelection(idx);
                tts.setLanguage(defaultLocale);
                updateVoiceList(defaultLocale, true);
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
                // 设置全局朗读进度监听
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        if ("tts_speak".equals(utteranceId)) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "朗读开始", Toast.LENGTH_SHORT).show());
                        }
                    }
                    @Override
                    public void onDone(String utteranceId) {
                        if ("tts_speak".equals(utteranceId)) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "朗读结束", Toast.LENGTH_SHORT).show());
                        }
                    }
                    @Override
                    public void onError(String utteranceId) {
                        if ("tts_speak".equals(utteranceId)) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "朗读出错", Toast.LENGTH_SHORT).show());
                        }
                    }
                });
                isTtsReady = true;
                btnSpeak.setEnabled(true);
                btnStop.setEnabled(true);
                btnSaveAudio.setEnabled(true);
                tvStatus.setText(R.string.status_ready);
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
                        if (l1.equals(defaultLocale)) return -1;
                        if (l2.equals(defaultLocale)) return 1;
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
            String text = editText.getText().toString();
            if (text.isEmpty()) {
                Toast.makeText(this, R.string.hint_input_text, Toast.LENGTH_SHORT).show();
                return;
            }
            if (text.length() > 3500) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_title_warning)
                        .setMessage(R.string.dialog_message_text_too_long)
                        .setPositiveButton(R.string.dialog_button_continue, (dialog, which) -> {
                            tts.setLanguage(currentLocale);
                            tts.setSpeechRate(speechRate);
                            tts.setPitch(pitch);
                            Bundle params = new Bundle();
                            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_speak");
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "tts_speak");
                            Toast.makeText(this, R.string.toast_please_wait, Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(R.string.dialog_button_cancel, (dialog, which) -> {
                            if (tts != null && isTtsReady)
                                tts.stop();
                            Toast.makeText(this, R.string.toast_reduce_text, Toast.LENGTH_SHORT).show();
                        })
                        .show();
                return;
            }
            tts.setLanguage(currentLocale);
            tts.setSpeechRate(speechRate);
            tts.setPitch(pitch);
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_speak");
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "tts_speak");
        });

        @SuppressLint("ClickableViewAccessibility")
        View.OnTouchListener scaleTouch = (v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setScaleX(0.92f);
                    v.setScaleY(0.92f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setScaleX(1f);
                    v.setScaleY(1f);
                    break;
            }
            return false;
        };
        btnSpeedMinus.setOnTouchListener(scaleTouch);
        btnSpeedPlus.setOnTouchListener(scaleTouch);
        btnPitchMinus.setOnTouchListener(scaleTouch);
        btnPitchPlus.setOnTouchListener(scaleTouch);
        btnSpeak.setOnTouchListener(scaleTouch);
        btnStop.setOnTouchListener(scaleTouch);
        btnSaveAudio.setOnTouchListener(scaleTouch);
    }

    @Override
    protected void onDestroy() {
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
    private void synthesizeTextToUri(String text, Uri uri) {
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
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            if ("tts_save".equals(utteranceId)) {
                                runOnUiThread(() -> Toast.makeText(MainActivity.this, "音频保存开始", Toast.LENGTH_SHORT).show());
                            }
                        }
                        @Override
                        public void onDone(String utteranceId) {
                            if ("tts_save".equals(utteranceId)) {
                                runOnUiThread(() -> Toast.makeText(MainActivity.this, "音频保存成功", Toast.LENGTH_SHORT).show());
                            }
                        }
                        @Override
                        public void onError(String utteranceId) {
                            if ("tts_save".equals(utteranceId)) {
                                runOnUiThread(() -> Toast.makeText(MainActivity.this, "音频保存失败", Toast.LENGTH_SHORT).show());
                            }
                        }
                    });
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
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    if ("tts_save".equals(utteranceId)) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "音频保存开始", Toast.LENGTH_SHORT).show());
                    }
                }
                @Override
                public void onDone(String utteranceId) {
                    if ("tts_save".equals(utteranceId)) {
                        runOnUiThread(() -> copyFileToUri(tempWav, uri, "audio/wav"));
                    }
                }
                @Override
                public void onError(String utteranceId) {
                    if ("tts_save".equals(utteranceId)) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "音频保存失败", Toast.LENGTH_SHORT).show());
                    }
                    tempWav.delete();
                }
            });
            int result = tts.synthesizeToFile(text, ttsParams, tempWav.getAbsolutePath());
            if (result != TextToSpeech.SUCCESS) {
                Toast.makeText(this, R.string.message_audio_synthesis_failed, Toast.LENGTH_SHORT).show();
            }
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
            Toast.makeText(this, "音频保存成功", Toast.LENGTH_SHORT).show();
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
                boolean isDefault = (currentLangDefaultVoice != null && voice.equals(currentLangDefaultVoice));
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
        StringBuilder statusInfo = new StringBuilder();

        if (isTtsReady) {
            statusInfo.append(getString(R.string.status_ready));

            // 如果使用的是系统默认语言，添加提示信息
            if (defaultLocale != null && defaultLocale.equals(Locale.getDefault()) && globalDefaultVoice == null) {
                statusInfo.append("\n\n");
                statusInfo
                        .append(getString(R.string.status_ready_with_system_language, defaultLocale.getDisplayName()));
            }
        } else {
            statusInfo.append(getString(R.string.status_not_ready));
        }

        tvStatus.setText(statusInfo.toString());
    }
}
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;

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
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private TextToSpeech tts;
    private EditText editText;
    private Button btnSpeak;
    private SeekBar seekBarSpeed, seekBarPitch;
    private TextView tvStatus;
    private TextView tvSupportedLanguages;
    private boolean isTtsReady = false;
    private float speechRate = 1.0f;
    private float pitch = 1.0f;
    private Locale currentLocale = Locale.CHINESE;
    private Button btnClear;
    private Button btnStop;
    private Button btnSaveAudio;
    private ActivityResultLauncher<Intent> createFileLauncher;
    private String pendingAudioText = null;
    private TextView textSpeechRateValue, textPitchValue;
    private Button btnSpeedMinus, btnSpeedPlus, btnPitchMinus, btnPitchPlus, btnSpeedReset, btnPitchReset;

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
        tvSupportedLanguages = findViewById(R.id.tvSupportedLanguages);
        btnSpeak.setEnabled(false);
        btnStop = findViewById(R.id.btnStop);
        btnStop.setEnabled(false);
        btnSaveAudio = findViewById(R.id.btnSaveAudio);
        // TTS未初始化时按钮不可用
        btnSpeak.setEnabled(false);
        btnStop.setEnabled(false);
        btnSaveAudio.setEnabled(false);
        tvStatus.setText(R.string.status_not_ready);
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
            }
        );
        btnSaveAudio.setOnClickListener(v -> {
            if (!isTtsReady) {
                Toast.makeText(this, R.string.status_not_ready, Toast.LENGTH_SHORT).show();
                return;
            }
            String text = editText.getText().toString();
            if (text.isEmpty()) {
                Toast.makeText(this, "请输入要保存的文本", Toast.LENGTH_SHORT).show();
                return;
            }
            if (text.length() > 3500) {
                new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("Android TTS单次最大支持3999字节，建议每次朗读不超过3500字符（含标点、空格），超长文本请分段朗读")
                    .setPositiveButton("确定", null)
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
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        btnSpeedMinus.setOnClickListener(v -> {
            float value = Float.parseFloat(textSpeechRateValue.getText().toString());
            value -= 0.01f;
            if (value < 0.5f) value = 0.5f;
            value = Math.round(value * 100f) / 100f; // 保留两位小数
            textSpeechRateValue.setText(String.format("%.2f", value));
            speechRate = value;
            // 同步SeekBar
            int progress = Math.round((value - 0.5f) / 0.1f);
            seekBarSpeed.setProgress(progress);
        });
        btnSpeedPlus.setOnClickListener(v -> {
            float value = Float.parseFloat(textSpeechRateValue.getText().toString());
            value += 0.01f;
            if (value > 2.0f) value = 2.0f;
            value = Math.round(value * 100f) / 100f;
            textSpeechRateValue.setText(String.format("%.2f", value));
            speechRate = value;
            int progress = Math.round((value - 0.5f) / 0.1f);
            seekBarSpeed.setProgress(progress);
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
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        btnPitchMinus.setOnClickListener(v -> {
            float value = Float.parseFloat(textPitchValue.getText().toString());
            value -= 0.01f;
            if (value < 0.5f) value = 0.5f;
            value = Math.round(value * 100f) / 100f;
            textPitchValue.setText(String.format("%.2f", value));
            pitch = value;
            int progress = Math.round((value - 0.5f) / 0.1f);
            seekBarPitch.setProgress(progress);
        });
        btnPitchPlus.setOnClickListener(v -> {
            float value = Float.parseFloat(textPitchValue.getText().toString());
            value += 0.01f;
            if (value > 2.0f) value = 2.0f;
            value = Math.round(value * 100f) / 100f;
            textPitchValue.setText(String.format("%.2f", value));
            pitch = value;
            int progress = Math.round((value - 0.5f) / 0.1f);
            seekBarPitch.setProgress(progress);
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
        });

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(currentLocale);
                tts.setSpeechRate(speechRate);
                tts.setPitch(pitch);
                isTtsReady = true;
                btnSpeak.setEnabled(true);
                btnStop.setEnabled(true);
                btnSaveAudio.setEnabled(true);
                tvStatus.setText(R.string.status_ready);

                // 显示支持的语言
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Set<Locale> locales = tts.getAvailableLanguages();
                    StringBuilder sb = new StringBuilder();
                    for (Locale locale : locales) {
                        sb.append(LocaleDisplayNameMapper.getLocaleDisplayName(locale)).append("\n");
                    }
                    tvSupportedLanguages.setText(sb.toString().trim());
                } else {
                    tvSupportedLanguages.setText("(当前系统不支持查询)");
                }
            } else {
                tvStatus.setText(R.string.status_not_ready);
                btnSpeak.setEnabled(false);
                btnStop.setEnabled(false);
                btnSaveAudio.setEnabled(false);
            }
        });

        btnSpeak.setOnClickListener(v -> {
            if (!isTtsReady) {
                Toast.makeText(this, R.string.status_not_ready, Toast.LENGTH_SHORT).show();
                return;
            }
            String text = editText.getText().toString();
            if (text.isEmpty()) {
                Toast.makeText(this, "请输入要朗读的文本", Toast.LENGTH_SHORT).show();
                return;
            }
            if (text.length() > 3500) {
                new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("Android TTS单次最大支持3999字节，建议每次朗读不超过3500字符（含标点、空格），超长文本请分段朗读")
                    .setPositiveButton("确定", null)
                    .show();
                return;
            }
            if (!text.isEmpty()) {
                tts.setLanguage(currentLocale);
                tts.setSpeechRate(speechRate);
                tts.setPitch(pitch);
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        @SuppressLint("ClickableViewAccessibility") View.OnTouchListener scaleTouch = (v, event) -> {
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
                Toast.makeText(this, "无法打开系统TTS设置界面", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.action_info) {
            String info = getString(R.string.desc_tts_info) + "\n\n" + getString(R.string.desc_tts_length_limit);
            new AlertDialog.Builder(this)
                .setTitle("说明")
                .setMessage(info)
                .setPositiveButton("确定", null)
                .show();
            return true;
        } else if (id == R.id.action_about) {
            new AlertDialog.Builder(this)
                .setTitle("关于")
                .setMessage("（内容待补充）")
                .setPositiveButton("确定", null)
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
                    tts.setLanguage(currentLocale);
                    tts.setSpeechRate(speechRate);
                    tts.setPitch(pitch);
                    tts.synthesizeToFile(text, params, pfd, "tts_output");
                    Toast.makeText(this, "已开始保存音频，完成后请在文件管理器中查看。", Toast.LENGTH_LONG).show();
                    pfd.close();
                }
            } catch (Exception e) {
                Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // Android 5.0~9.0
            File tempWav = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "tts_temp.wav");
            HashMap<String, String> ttsParams = new HashMap<>();
            ttsParams.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "1.0");
            ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_temp");
            tts.setLanguage(currentLocale);
            tts.setSpeechRate(speechRate);
            tts.setPitch(pitch);
            int result = tts.synthesizeToFile(text, ttsParams, tempWav.getAbsolutePath());
            if (result == TextToSpeech.SUCCESS) {
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {}
                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(() -> {
                            try (FileInputStream fis = new FileInputStream(tempWav);
                                 OutputStream os = getContentResolver().openOutputStream(uri)) {
                                byte[] buffer = new byte[4096];
                                int len;
                                while ((len = fis.read(buffer)) > 0) {
                                    os.write(buffer, 0, len);
                                }
                                os.flush();
                                Toast.makeText(MainActivity.this, "音频已保存到自定义目录", Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                Toast.makeText(MainActivity.this, "拷贝音频失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            tempWav.delete();
                        });
                    }
                    @Override
                    public void onError(String utteranceId) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "音频合成失败", Toast.LENGTH_SHORT).show());
                        tempWav.delete();
                    }
                });
            } else {
                Toast.makeText(this, "音频合成失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Android 5.0及以上才支持保存为音频文件", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "音频已保存到自定义目录", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "拷贝音频失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        src.delete();
    }
}
package com.citadawn.speechapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.TypedValue;
import android.graphics.Paint;

public class TextEditorActivity extends AppCompatActivity {
    public static final String EXTRA_TEXT = "extra_text";
    private EditText editorEditText;
    private Button btnEditorClear, btnEditorOk;
    private TextView tvEditorCharCount;
    private int maxCharCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);

        Toolbar toolbar = findViewById(R.id.editorToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(android.graphics.Color.WHITE);
        }
        toolbar.setNavigationOnClickListener(v -> finish()); // 返回不保存

        editorEditText = findViewById(R.id.editorEditText);
        btnEditorClear = findViewById(R.id.btnEditorClear);
        btnEditorOk = findViewById(R.id.btnEditorOk);
        tvEditorCharCount = findViewById(R.id.tvEditorCharCount);

        // 打开时显示传入内容
        String text = getIntent().getStringExtra(EXTRA_TEXT);
        if (text != null) {
            editorEditText.setText(text);
        }
        // 记录原始文本
        final String originalText = text == null ? "" : text;
        btnEditorOk.setEnabled(false); // 初始禁用

        // 根据内容动态启用/禁用清空按钮和确定按钮
        btnEditorClear.setEnabled(editorEditText.getText().toString().length() > 0);
        maxCharCount = android.speech.tts.TextToSpeech.getMaxSpeechInputLength();
        updateCharCount();
        editorEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnEditorClear.setEnabled(s.length() > 0);
                btnEditorOk.setEnabled(!s.toString().equals(originalText));
                updateCharCount();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // 设置清空按钮逻辑（复用工具类）
        ClearButtonHelper.setupClearButton(btnEditorClear, editorEditText);

        btnEditorOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(EXTRA_TEXT, editorEditText.getText().toString());
                setResult(RESULT_OK, data);
                finish();
            }
        });

        findViewById(R.id.btnEditorInfo).setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(TextEditorActivity.this)
                .setTitle(R.string.dialog_title_editor_info)
                .setMessage(R.string.dialog_message_editor_info)
                .setPositiveButton(R.string.dialog_button_editor_info_ok, null)
                .show();
        });
    }

    private void updateCharCount() {
        int current = editorEditText.getText().length();
        String text = "字数：" + current + "/" + maxCharCount;
        if (current > maxCharCount) {
            // 只将当前字数部分标红
            int start = 3; // "字数："长度
            int end = start + String.valueOf(current).length();
            android.text.SpannableString ss = new android.text.SpannableString(text);
            ss.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.RED), start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvEditorCharCount.setText(ss);
        } else {
            tvEditorCharCount.setText(text);
            tvEditorCharCount.setTextColor(android.graphics.Color.parseColor("#666666"));
        }
    }
} 
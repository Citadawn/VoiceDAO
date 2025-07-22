package com.citadawn.speechapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.TypedValue;
import android.graphics.Paint;

public class TextEditorActivity extends AppCompatActivity {
    public static final String EXTRA_TEXT = "extra_text";
    private EditText editorEditText;
    private Button btnEditorClear, btnEditorOk;

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
        editorEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnEditorClear.setEnabled(s.length() > 0);
                btnEditorOk.setEnabled(!s.toString().equals(originalText));
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
                .setTitle("使用说明")
                .setMessage("1. 可在此编辑、粘贴、整理大段文本，便于朗读或保存。\n2. “确定”按钮仅在内容被修改后可用，点击后将内容带回主界面。\n3. “清空”按钮需连续两次点击才会真正清空内容，防止误操作。\n4. 支持多行滚动、复制、粘贴等常用编辑操作。\n5. 返回按钮不保存内容，直接退出编辑器。")
                .setPositiveButton("知道了", null)
                .show();
        });
    }
} 
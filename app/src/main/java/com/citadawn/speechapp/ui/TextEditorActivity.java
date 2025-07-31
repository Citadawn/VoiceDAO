package com.citadawn.speechapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.citadawn.speechapp.R;
import com.citadawn.speechapp.util.ButtonTextHelper;
import com.citadawn.speechapp.util.ClearButtonHelper;
import com.citadawn.speechapp.util.DialogHelper;
import com.citadawn.speechapp.util.LocaleHelper;
import com.citadawn.speechapp.util.TextLengthHelper;

/**
 * 文本编辑器活动
 * 提供大文本编辑功能，支持清空、字数统计等
 */
public class TextEditorActivity extends AppCompatActivity {
    // region 成员变量
    public static final String EXTRA_TEXT = "extra_text";
    private EditText editorEditText;
    private Button btnEditorClear, btnEditorOk;
    private TextView tvEditorCharCount;
    private int maxCharCount;

    // endregion
    // region 生命周期
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 应用用户选择的语言设置 Apply user selected language setting
        LocaleHelper.setLocale(this, LocaleHelper.getCurrentLocale(this));

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
        btnEditorClear.setEnabled(!editorEditText.getText().toString().isEmpty());
        maxCharCount = TextLengthHelper.getMaxTextLength();
        updateCharCount();
        editorEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnEditorClear.setEnabled(s.length() > 0);
                btnEditorOk.setEnabled(!s.toString().equals(originalText));
                updateCharCount();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        // 设置清空按钮逻辑（复用工具类）
        ClearButtonHelper.setupClearButton(btnEditorClear, editorEditText);

        // 为编辑器按钮设置自动文本大小调整
        ButtonTextHelper.setupAutoTextSize(btnEditorOk);
        ButtonTextHelper.setupAutoTextSize(btnEditorClear);

        btnEditorOk.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra(EXTRA_TEXT, editorEditText.getText().toString());
            setResult(RESULT_OK, data);
            finish();
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            editorEditText.setTextSelectHandleTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#272727")));
        }
    }

    // endregion
    // region 公开方法

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_text_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_editor_info) {
            DialogHelper.showInfoDialog(this,
                    R.string.dialog_title_editor_info, R.string.dialog_message_editor_info);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // endregion
    // region 私有方法
    private void updateCharCount() {
        int current = editorEditText.getText().length();
        String baseText = getString(R.string.char_count, current, maxCharCount);
        if (current > maxCharCount) {
            // 只将当前字数部分标红
            String currentStr = String.valueOf(current);
            int start = baseText.indexOf(currentStr);
            int end = start + currentStr.length();
            android.text.SpannableString ss = new android.text.SpannableString(baseText);
            ss.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.RED), start, end,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvEditorCharCount.setText(ss);
        } else {
            tvEditorCharCount.setText(baseText);
            tvEditorCharCount.setTextColor(ContextCompat.getColor(this, R.color.gray_666));
        }
    }
    // endregion
}
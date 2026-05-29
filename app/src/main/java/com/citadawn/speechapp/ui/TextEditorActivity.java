package com.citadawn.speechapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.citadawn.speechapp.R;
import com.citadawn.speechapp.util.ButtonTextHelper;
import com.citadawn.speechapp.util.ClearButtonHelper;
import com.citadawn.speechapp.util.DialogHelper;
import com.citadawn.speechapp.util.LocaleHelper;
import com.citadawn.speechapp.util.SystemBarsHelper;
import com.citadawn.speechapp.util.TextLengthHelper;

/**
 * 文本编辑器活动
 * 提供大文本编辑功能，支持清空、字数统计等
 */
public class TextEditorActivity extends AppCompatActivity {

    // region 常量

    /**
     * Intent 传递文本的键名
     */
    public static final String EXTRA_TEXT = "extra_text";

    // endregion

    // region 成员变量

    private EditText editorEditText;
    private Button btnEditorClear, btnEditorSave;
    private TextView tvEditorCharCount;
    private int maxCharCount;
    private String originalText = "";
    private int lastImeInsetBottom;

    // endregion

    // region 生命周期方法

    /**
     * 活动创建时初始化UI和事件监听
     *
     * @param savedInstanceState 保存的状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.setLocale(this, LocaleHelper.getCurrentLocale(this));

        super.onCreate(savedInstanceState);
        SystemBarsHelper.enable(this);
        setContentView(R.layout.activity_text_editor);

        Toolbar toolbar = findViewById(R.id.editorToolbar);
        SystemBarsHelper.applyToolbarTopInsets(toolbar);

        View rootContainer = findViewById(R.id.rootContainer);
        View layoutEditorBottomBar = findViewById(R.id.layoutEditorBottomBar);
        final int bottomBarBaseMargin = getResources().getDimensionPixelSize(R.dimen.dp_24);
        ViewCompat.setOnApplyWindowInsetsListener(rootContainer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomInset = Math.max(systemBars.bottom, ime.bottom);

            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            ViewGroup.MarginLayoutParams barParams =
                    (ViewGroup.MarginLayoutParams) layoutEditorBottomBar.getLayoutParams();
            barParams.bottomMargin = bottomBarBaseMargin + bottomInset;
            layoutEditorBottomBar.setLayoutParams(barParams);

            if (ime.bottom > lastImeInsetBottom) {
                scheduleScrollEditorToCursorIfNeeded();
            }
            lastImeInsetBottom = ime.bottom;

            return insets;
        });
        ViewCompat.requestApplyInsets(rootContainer);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(android.graphics.Color.WHITE);
        }
        toolbar.setNavigationOnClickListener(v -> requestExit());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requestExit();
            }
        });

        editorEditText = findViewById(R.id.editorEditText);
        btnEditorClear = findViewById(R.id.btnEditorClear);
        btnEditorSave = findViewById(R.id.btnEditorSave);
        tvEditorCharCount = findViewById(R.id.tvEditorCharCount);

        String text = getIntent().getStringExtra(EXTRA_TEXT);
        if (text != null) {
            editorEditText.setText(text);
        }
        originalText = text == null ? "" : text;
        btnEditorSave.setEnabled(false);

        btnEditorClear.setEnabled(!editorEditText.getText().toString().isEmpty());
        maxCharCount = TextLengthHelper.getMaxTextLength();
        updateCharCount();
        editorEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {
                btnEditorClear.setEnabled(s.length() > 0);
                btnEditorSave.setEnabled(!TextUtils.equals(s, originalText));
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                updateCharCount();
            }
        });

        ClearButtonHelper.setupClearButton(btnEditorClear, editorEditText);

        ButtonTextHelper.setupAutoTextSize(btnEditorSave);

        btnEditorSave.setOnClickListener(v -> saveAndFinish());
    }

    // endregion

    // region 菜单相关方法

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_text_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_editor_info) {
            DialogHelper.showInfoDialog(this,
                    R.string.dialog_title_editor_info, R.string.dialog_message_editor_info);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // endregion

    // region 私有辅助方法

    private boolean hasUnsavedChanges() {
        return !editorEditText.getText().toString().equals(originalText);
    }

    private void saveAndFinish() {
        Intent data = new Intent();
        data.putExtra(EXTRA_TEXT, editorEditText.getText().toString());
        setResult(RESULT_OK, data);
        finish();
    }

    private void requestExit() {
        if (hasUnsavedChanges()) {
            DialogHelper.showEditorUnsavedDialog(this, this::saveAndFinish, this::finish);
        } else {
            finish();
        }
    }

    /**
     * 键盘弹出时将光标滚入可见区域；手动滚动文本时不打断用户。
     */
    private void scheduleScrollEditorToCursorIfNeeded() {
        if (editorEditText == null) {
            return;
        }
        editorEditText.post(this::scrollEditorToCursor);
    }

    private void scrollEditorToCursor() {
        if (editorEditText == null) {
            return;
        }
        android.text.Layout layout = editorEditText.getLayout();
        if (layout == null) {
            return;
        }
        int selection = editorEditText.getSelectionStart();
        if (selection < 0) {
            return;
        }
        int line = layout.getLineForOffset(selection);
        int lineBottom = layout.getLineBottom(line);
        int lineTop = layout.getLineTop(line);
        int visibleHeight = editorEditText.getHeight()
                - editorEditText.getTotalPaddingTop()
                - editorEditText.getTotalPaddingBottom();
        if (visibleHeight <= 0) {
            return;
        }
        int scrollY = editorEditText.getScrollY();
        if (lineBottom - scrollY > visibleHeight) {
            editorEditText.scrollTo(0, lineBottom - visibleHeight);
        } else if (lineTop < scrollY) {
            editorEditText.scrollTo(0, lineTop);
        }
    }

    private void updateCharCount() {
        int current = editorEditText.getText().length();
        String baseText = getString(R.string.char_count, current, maxCharCount);
        if (current > maxCharCount) {
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

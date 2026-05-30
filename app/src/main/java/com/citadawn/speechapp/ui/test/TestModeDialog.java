package com.citadawn.speechapp.ui.test;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.citadawn.speechapp.R;
import com.citadawn.speechapp.util.DialogHelper;
import com.citadawn.speechapp.util.SystemBarsHelper;

import java.util.List;

/**
 * 调试面板：勾选调试项并启用。
 */
public class TestModeDialog extends Dialog {

    private final List<TestCase> testCases;
    private final OnTestSelectedListener listener;
    private Button btnStartTest;
    private TestCaseAdapter adapter;

    public TestModeDialog(@NonNull Context context, List<TestCase> testCases, OnTestSelectedListener listener) {
        super(context, R.style.Theme_VoiceDAO);
        this.testCases = testCases;
        this.listener = listener;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_mode_dialog);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.45f);
            SystemBarsHelper.applySolidStatusBarForOverlay(window, getContext());
        }

        RecyclerView rvTestCases = findViewById(R.id.rvTestCases);
        Button btnCancel = findViewById(R.id.btnCancel);
        btnStartTest = findViewById(R.id.btnStartTest);
        TextView btnExpandAllDesc = findViewById(R.id.btnExpandAllDesc);
        TextView btnCollapseAllDesc = findViewById(R.id.btnCollapseAllDesc);
        ImageView ivTestModeInfo = findViewById(R.id.ivTestModeInfo);

        btnExpandAllDesc.setOnClickListener(v -> {
            if (adapter != null) {
                adapter.expandAllDesc();
            }
        });
        btnCollapseAllDesc.setOnClickListener(v -> {
            if (adapter != null) {
                adapter.collapseAllDesc();
            }
        });

        ivTestModeInfo.setOnClickListener(v -> showTestModeInfo());

        int scrollbarPx = getContext().getResources().getDimensionPixelSize(R.dimen.dp_4);
        rvTestCases.setScrollBarSize(scrollbarPx);
        rvTestCases.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TestCaseAdapter(testCases);
        rvTestCases.setAdapter(adapter);
        adapter.setOnSelectionChangedListener(this::updateButtonState);
        updateButtonState();

        btnCancel.setOnClickListener(v -> dismiss());
        btnStartTest.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTestSelected(testCases);
            }
            dismiss();
        });
    }

    private void updateButtonState() {
        if (btnStartTest == null) {
            return;
        }

        int selectedCount = 0;
        for (TestCase testCase : testCases) {
            if (testCase.selected) {
                selectedCount++;
            }
        }

        boolean hasSelected = selectedCount > 0;
        btnStartTest.setEnabled(hasSelected);
        btnStartTest.setAlpha(hasSelected ? 1.0f : 0.38f);

        if (hasSelected) {
            btnStartTest.setText(getContext().getString(R.string.test_mode_btn_start_with_count, selectedCount));
        } else {
            btnStartTest.setText(R.string.test_mode_btn_start);
        }
    }

    private void showTestModeInfo() {
        DialogHelper.showInfoDialog(getContext(), R.string.test_mode_info_title, R.string.test_mode_info_content);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window != null) {
            android.util.DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
            int width = (int) (dm.widthPixels * 0.92);
            int maxHeight = (int) (dm.heightPixels * 0.85);
            window.setLayout(width, maxHeight);
            window.setGravity(android.view.Gravity.CENTER);
        }
    }

    public interface OnTestSelectedListener {
        void onTestSelected(List<TestCase> selected);
    }
}

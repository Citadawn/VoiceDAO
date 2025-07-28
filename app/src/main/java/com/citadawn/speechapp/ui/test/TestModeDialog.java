package com.citadawn.speechapp.ui.test;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.citadawn.speechapp.util.InfoIconPositionHelper;
import com.citadawn.speechapp.util.ViewHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.citadawn.speechapp.R;

import java.util.List;

public class TestModeDialog extends Dialog {
    private final List<TestCase> testCases;
    private final OnTestSelectedListener listener;
    private Button btnStartTest;
    private TestCaseAdapter adapter;

    public interface OnTestSelectedListener {
        void onTestSelected(List<TestCase> selected);
    }

    public TestModeDialog(@NonNull Context context, List<TestCase> testCases, OnTestSelectedListener listener) {
        super(context);
        this.testCases = testCases;
        this.listener = listener;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.test_mode_dialog, null);
        setContentView(view);

        RecyclerView rvTestCases = view.findViewById(R.id.rvTestCases);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnStartTest = view.findViewById(R.id.btnStartTest);
        TextView btnExpandAllDesc = view.findViewById(R.id.btnExpandAllDesc);
        TextView btnCollapseAllDesc = view.findViewById(R.id.btnCollapseAllDesc);
        
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
        
        // 设置信息图标点击事件
        ImageView ivTestModeInfo = view.findViewById(R.id.ivTestModeInfo);
        ivTestModeInfo.setOnClickListener(v -> showTestModeInfo());
        
        // 使用统一工具类设置信息图标位置
        setIconPosition(ivTestModeInfo);

        rvTestCases.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TestCaseAdapter(testCases);
        rvTestCases.setAdapter(adapter);

        // 设置选择状态监听
        adapter.setOnSelectionChangedListener(this::updateButtonState);
        
        // 初始化按钮状态
        updateButtonState();

        btnCancel.setOnClickListener(v -> dismiss());
        btnStartTest.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTestSelected(testCases);
            }
            dismiss();
        });
    }

    /**
     * 更新测试按钮状态
     */
    private void updateButtonState() {
        if (btnStartTest == null) return;
        
        // 统计选中的测试项数量
        int selectedCount = 0;
        for (TestCase testCase : testCases) {
            if (testCase.selected) {
                selectedCount++;
            }
        }
        
        // 根据选择状态启用/禁用按钮
        boolean hasSelected = selectedCount > 0;
        btnStartTest.setEnabled(hasSelected);
        
        // 更新按钮文本，显示选择数量
        String buttonText;
        if (hasSelected) {
            buttonText = getContext().getString(R.string.test_mode_btn_start) + " (" + selectedCount + ")";
            btnStartTest.setAlpha(1.0f);
        } else {
            buttonText = getContext().getString(R.string.test_mode_btn_start);
            btnStartTest.setAlpha(0.5f);
        }
        btnStartTest.setText(buttonText);
    }

    /**
     * 使用统一工具类设置信息图标位置
     */
    private void setIconPosition(ImageView imageView) {
        // 延迟执行，确保布局完成
        imageView.post(() -> {
            // 获取TextView的实际文字大小
            TextView textView = getTextViewFromLayout();
            if (textView != null) {
                // 使用统一工具类设置位置
                InfoIconPositionHelper.setIconPosition(getContext(), imageView, textView);
            }
        });
    }
    
                /**
             * 从布局中获取TextView
             */
            private TextView getTextViewFromLayout() {
                // 获取根视图
                View rootView = getWindow().getDecorView();
                String targetText = getContext().getString(R.string.test_mode_dialog_title);
                return ViewHelper.findTextViewByTargetText(rootView, targetText);
            }



    /**
     * 显示测试模式信息对话框
     */
    private void showTestModeInfo() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.test_mode_info_title)
                .setMessage(android.text.Html.fromHtml(getContext().getString(R.string.test_mode_info_content), android.text.Html.FROM_HTML_MODE_COMPACT))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getWindow() != null) {
            int width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
            getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            // 设置dialog居中显示
            getWindow().setGravity(android.view.Gravity.CENTER);
        }
    }
} 
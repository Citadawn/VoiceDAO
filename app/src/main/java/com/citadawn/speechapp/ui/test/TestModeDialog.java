package com.citadawn.speechapp.ui.test;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.citadawn.speechapp.R;
import com.citadawn.speechapp.util.InfoIconPositionHelper;
import com.citadawn.speechapp.util.ViewHelper;

import java.util.List;

/**
 * 测试模式选择对话框
 * 提供测试用例选择界面，支持多选、展开/收起描述等功能
 */
public class TestModeDialog extends Dialog {
    
    // region 成员变量
    
    /** 测试用例列表 */
    private final List<TestCase> testCases;
    
    /** 测试选择监听器 */
    private final OnTestSelectedListener listener;
    
    /** 开始测试按钮 */
    private Button btnStartTest;
    
    /** 测试用例适配器 */
    private TestCaseAdapter adapter;
    
    // endregion
    
    // region 接口定义
    
    /**
     * 测试选择监听器接口
     */
    public interface OnTestSelectedListener {
        /**
         * 当测试用例被选择时调用
         * @param selected 已选择的测试用例列表
         */
        void onTestSelected(List<TestCase> selected);
    }
    
    // endregion
    
    // region 构造方法
    
    /**
     * 创建测试模式对话框
     * @param context 上下文
     * @param testCases 测试用例列表
     * @param listener 选择监听器
     */
    public TestModeDialog(@NonNull Context context, List<TestCase> testCases, OnTestSelectedListener listener) {
        super(context);
        this.testCases = testCases;
        this.listener = listener;
    }
    
    // endregion
    
    // region 生命周期方法
    
    /**
     * 对话框创建时初始化UI和事件监听
     * @param savedInstanceState 保存的状态
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_mode_dialog);

        RecyclerView rvTestCases = findViewById(R.id.rvTestCases);
        Button btnCancel = findViewById(R.id.btnCancel);
        btnStartTest = findViewById(R.id.btnStartTest);
        TextView btnExpandAllDesc = findViewById(R.id.btnExpandAllDesc);
        TextView btnCollapseAllDesc = findViewById(R.id.btnCollapseAllDesc);

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
        ImageView ivTestModeInfo = findViewById(R.id.ivTestModeInfo);
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
    
    // endregion
    
    // region 私有辅助方法
    
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
                InfoIconPositionHelper.setIconPosition(imageView, textView);
            }
        });
    }

    /**
     * 从布局中获取TextView
     */
    private TextView getTextViewFromLayout() {
        // 获取根视图
        if (getWindow() == null) return null;
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
    
    // endregion
    
    // region 生命周期方法
    
    /**
     * 对话框显示时设置窗口属性
     */
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
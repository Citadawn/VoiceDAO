package com.citadawn.speechapp.ui.test;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.citadawn.speechapp.R;

import java.util.Arrays;
import java.util.List;

/**
 * 测试用例适配器
 * 用于在RecyclerView中显示测试用例列表，支持选择、展开/收起描述等功能
 */
public class TestCaseAdapter extends RecyclerView.Adapter<TestCaseAdapter.ViewHolder> {
    
    // region 成员变量
    
    /** 测试用例列表 */
    private final List<TestCase> testCases;
    
    /** 选择状态变化监听器 */
    private OnSelectionChangedListener selectionListener;
    
    /** 记录每个item描述是否展开 */
    private final boolean[] descExpanded;
    
    // endregion
    
    // region 接口定义
    
    /**
     * 选择状态变化监听器接口
     */
    public interface OnSelectionChangedListener {
        /**
         * 当选择状态发生变化时调用
         */
        void onSelectionChanged();
    }
    
    // endregion
    
    // region 构造方法
    
    /**
     * 创建测试用例适配器
     * @param testCases 测试用例列表
     */
    public TestCaseAdapter(List<TestCase> testCases) {
        this.testCases = testCases;
        this.descExpanded = new boolean[testCases.size()];
    }
    
    // endregion
    
    // region 公开方法
    
    /**
     * 收起所有测试用例的描述
     */
    public void collapseAllDesc() {
        Arrays.fill(descExpanded, false);
        notifyItemRangeChanged(0, descExpanded.length);
    }

    /**
     * 展开所有测试用例的描述
     */
    public void expandAllDesc() {
        Arrays.fill(descExpanded, true);
        notifyItemRangeChanged(0, descExpanded.length);
    }

    /**
     * 设置选择状态变化监听器
     * @param listener 监听器
     */
    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }
    
    // endregion
    
    // region 适配器核心方法
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test_case, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TestCase testCase = testCases.get(position);
        String name, desc;
        name = testCase.name;
        desc = testCase.description;
        holder.cbTestCase.setText(name);
        holder.cbTestCase.setChecked(testCase.selected);
        holder.tvTestCaseDesc.setText(desc);
        holder.tvTestCaseDesc.setVisibility(descExpanded[position] ? View.VISIBLE : View.GONE);
        // 更新展开按钮的箭头方向
        holder.btnToggleDesc.setText(descExpanded[position] ? "▲" : "▼");
        // 点击展开按钮切换描述展开/收起
        holder.btnToggleDesc.setOnClickListener(v -> {
            descExpanded[position] = !descExpanded[position];
            notifyItemChanged(position);
        });
        holder.cbTestCase.setOnCheckedChangeListener(null);
        holder.cbTestCase.setChecked(testCase.selected);
        holder.cbTestCase.setOnCheckedChangeListener((buttonView, isChecked) -> {
            testCase.selected = isChecked;
            if (selectionListener != null) {
                selectionListener.onSelectionChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return testCases.size();
    }
    
    // endregion

    // region 内部类
    
    /**
     * ViewHolder 内部类
     * 用于缓存 RecyclerView 中的视图引用
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        
        /** 测试用例选择复选框 */
        final CheckBox cbTestCase;
        
        /** 测试用例描述文本 */
        final TextView tvTestCaseDesc;
        
        /** 展开/收起描述按钮 */
        final TextView btnToggleDesc;

        /**
         * 创建 ViewHolder
         * @param itemView 列表项视图
         */
        ViewHolder(View itemView) {
            super(itemView);
            cbTestCase = itemView.findViewById(R.id.cbTestCase);
            tvTestCaseDesc = itemView.findViewById(R.id.tvTestCaseDesc);
            btnToggleDesc = itemView.findViewById(R.id.btnToggleDesc);
        }
    }
    
    // endregion
} 
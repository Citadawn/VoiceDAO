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

public class TestCaseAdapter extends RecyclerView.Adapter<TestCaseAdapter.ViewHolder> {
    private final List<TestCase> testCases;
    private OnSelectionChangedListener selectionListener;
    // 新增：记录每个item描述是否展开
    private final boolean[] descExpanded;

    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    public TestCaseAdapter(List<TestCase> testCases) {
        this.testCases = testCases;
        this.descExpanded = new boolean[testCases.size()];
    }

    // 新增：外部调用收起全部描述
    public void collapseAllDesc() {
        Arrays.fill(descExpanded, false);
        notifyItemRangeChanged(0, descExpanded.length);
    }

    // 新增：外部调用展开全部描述
    public void expandAllDesc() {
        Arrays.fill(descExpanded, true);
        notifyItemRangeChanged(0, descExpanded.length);
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test_case, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TestCase testCase = testCases.get(position);
        holder.itemView.getContext();
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final CheckBox cbTestCase;
        final TextView tvTestCaseDesc;
        final TextView btnToggleDesc;

        ViewHolder(View itemView) {
            super(itemView);
            cbTestCase = itemView.findViewById(R.id.cbTestCase);
            tvTestCaseDesc = itemView.findViewById(R.id.tvTestCaseDesc);
            btnToggleDesc = itemView.findViewById(R.id.btnToggleDesc);
        }
    }
} 
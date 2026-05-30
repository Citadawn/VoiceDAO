package com.citadawn.speechapp.ui.test;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.citadawn.speechapp.R;
import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;

/**
 * 调试面板列表适配器：多选、展开/收起描述。
 */
public class TestCaseAdapter extends RecyclerView.Adapter<TestCaseAdapter.ViewHolder> {

    private final List<TestCase> testCases;
    @NonNull
    private final boolean[] descExpanded;
    private OnSelectionChangedListener selectionListener;

    public TestCaseAdapter(@NonNull List<TestCase> testCases) {
        this.testCases = testCases;
        this.descExpanded = new boolean[testCases.size()];
    }

    public void collapseAllDesc() {
        Arrays.fill(descExpanded, false);
        notifyItemRangeChanged(0, descExpanded.length);
    }

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
        holder.cbTestCase.setText(testCase.name);
        holder.tvTestCaseDesc.setText(testCase.description);
        holder.tvTestCaseDesc.setVisibility(descExpanded[position] ? View.VISIBLE : View.GONE);
        holder.btnToggleDesc.setImageResource(descExpanded[position]
                ? R.drawable.ic_debug_collapse
                : R.drawable.ic_debug_expand);

        applyItemCardStyle(holder, testCase.selected);

        holder.btnToggleDesc.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) {
                return;
            }
            descExpanded[pos] = !descExpanded[pos];
            notifyItemChanged(pos);
        });

        holder.cbTestCase.setOnCheckedChangeListener(null);
        holder.cbTestCase.setChecked(testCase.selected);
        holder.cbTestCase.setOnCheckedChangeListener((buttonView, isChecked) -> {
            testCase.selected = isChecked;
            applyItemCardStyle(holder, isChecked);
            if (selectionListener != null) {
                selectionListener.onSelectionChanged();
            }
        });

        holder.itemView.setOnClickListener(v -> holder.cbTestCase.toggle());
    }

    private void applyItemCardStyle(@NonNull ViewHolder holder, boolean selected) {
        int border = ContextCompat.getColor(holder.itemView.getContext(), R.color.theme_border);
        int accent = DebugModeUi.accentColor(holder.itemView.getContext());
        holder.cardTestCase.setStrokeColor(selected ? accent : border);
        holder.cardTestCase.setStrokeWidth(selected
                ? (int) (2 * holder.itemView.getResources().getDisplayMetrics().density)
                : (int) (1 * holder.itemView.getResources().getDisplayMetrics().density));
        int surfaceColor = ContextCompat.getColor(holder.itemView.getContext(),
                selected ? R.color.debug_mode_accent_surface : R.color.pure_white);
        holder.cardTestCase.setCardBackgroundColor(ColorStateList.valueOf(surfaceColor));
        holder.cbTestCase.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                selected ? R.color.accent_warning : R.color.text_primary));
    }

    @Override
    public int getItemCount() {
        return testCases.size();
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final MaterialCardView cardTestCase;
        final CheckBox cbTestCase;
        final TextView tvTestCaseDesc;
        final ImageView btnToggleDesc;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTestCase = itemView.findViewById(R.id.cardTestCase);
            cbTestCase = itemView.findViewById(R.id.cbTestCase);
            tvTestCaseDesc = itemView.findViewById(R.id.tvTestCaseDesc);
            btnToggleDesc = itemView.findViewById(R.id.btnToggleDesc);
        }
    }
}

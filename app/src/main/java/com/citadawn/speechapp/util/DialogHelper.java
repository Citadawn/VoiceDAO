package com.citadawn.speechapp.util;

import android.content.Context;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.citadawn.speechapp.R;

/**
 * 对话框工具类
 * 提供简化的对话框创建方法，统一管理对话框显示逻辑
 */
public class DialogHelper {
    // region 静态工具方法

    /**
     * 显示警告对话框
     * @param context 上下文
     * @param messageResId 消息资源ID
     * @param onPositive 确认按钮回调
     * @param onNegative 取消按钮回调
     */
    public static void showWarningDialog(Context context, int messageResId, 
                                       Runnable onPositive, Runnable onNegative) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_warning)
                .setMessage(messageResId)
                .setPositiveButton(R.string.dialog_button_continue, (dialog, which) -> {
                    if (onPositive != null) onPositive.run();
                })
                .setNegativeButton(R.string.dialog_button_cancel, (dialog, which) -> {
                    if (onNegative != null) onNegative.run();
                })
                .show();
    }

    /**
     * 显示信息对话框
     * @param context 上下文
     * @param titleResId 标题资源ID
     * @param messageResId 消息资源ID
     */
    public static void showInfoDialog(Context context, int titleResId, int messageResId) {
        // 获取消息文本并检查是否包含HTML标签
        String message = context.getString(messageResId);
        boolean isHtml = message.contains("<") && message.contains(">");
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(titleResId)
                .setPositiveButton(R.string.dialog_button_ok, null);
        
        if (isHtml) {
            // 如果包含HTML标签，使用HTML格式显示
            builder.setMessage(android.text.Html.fromHtml(message, android.text.Html.FROM_HTML_MODE_COMPACT));
        } else {
            // 普通文本显示
            builder.setMessage(messageResId);
        }
        
        builder.show();
    }

    /**
     * 显示输入对话框
     * @param context 上下文
     * @param titleResId 标题资源ID
     * @param defaultText 默认文本
     * @param onConfirm 确认回调，参数为输入的文本
     * @param onCancel 取消回调
     */
    public static void showInputDialog(Context context, int titleResId, String defaultText,
                                     InputDialogCallback onConfirm, Runnable onCancel) {
        EditText input = new EditText(context);
        input.setText(defaultText);
        if (defaultText != null) {
            input.setSelection(defaultText.length());
        }
        
        new AlertDialog.Builder(context)
                .setTitle(titleResId)
                .setView(input)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    if (onConfirm != null) {
                        onConfirm.onConfirm(input.getText().toString().trim());
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    if (onCancel != null) onCancel.run();
                })
                .show();
    }

    /**
     * 显示确认取消对话框
     * @param context 上下文
     * @param messageResId 消息资源ID
     * @param onConfirm 确认回调
     */
    public static void showConfirmCancelDialog(Context context, int messageResId, Runnable onConfirm) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_warning)
                .setMessage(messageResId)
                .setPositiveButton(R.string.dialog_button_cancel_save, (dialog, which) -> {
                    if (onConfirm != null) onConfirm.run();
                })
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .show();
    }

    /**
     * 输入对话框回调接口
     */
    public interface InputDialogCallback {
        void onConfirm(String inputText);
    }

    // endregion
} 
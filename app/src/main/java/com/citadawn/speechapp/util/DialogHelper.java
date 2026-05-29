package com.citadawn.speechapp.util;

import android.content.Context;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.citadawn.speechapp.R;

/**
 * 对话框工具类
 * 提供简化的对话框创建方法，统一管理对话框显示逻辑
 */
public class DialogHelper {
    // region 静态工具方法

    /**
     * 显示信息对话框
     *
     * @param context      上下文
     * @param titleResId   标题资源ID
     * @param messageResId 消息资源ID
     */
    public static void showInfoDialog(@NonNull Context context, int titleResId, int messageResId) {
        String message = context.getString(messageResId);
        new AlertDialog.Builder(context)
                .setTitle(titleResId)
                .setMessage(HtmlBulletHelper.formatInfoMessage(context, message))
                .setPositiveButton(R.string.dialog_button_ok, null)
                .show();
    }

    /**
     * 显示输入对话框
     *
     * @param context     上下文
     * @param titleResId  标题资源ID
     * @param defaultText 默认文本
     * @param onConfirm   确认回调，参数为输入的文本
     * @param onCancel    取消回调
     */
    public static void showInputDialog(@NonNull Context context, int titleResId, @Nullable String defaultText,
                                       @Nullable InputDialogCallback onConfirm, @Nullable Runnable onCancel) {
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
                    if (onCancel != null) {
                        onCancel.run();
                    }
                })
                .show();
    }

    /**
     * 显示确认取消对话框
     *
     * @param context      上下文
     * @param messageResId 消息资源ID
     * @param onConfirm    确认回调
     */
    public static void showConfirmCancelDialog(@NonNull Context context, int messageResId, @Nullable Runnable onConfirm) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_warning)
                .setMessage(messageResId)
                .setPositiveButton(R.string.dialog_button_cancel_save, (dialog, which) -> {
                    if (onConfirm != null) {
                        onConfirm.run();
                    }
                })
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .show();
    }

    /**
     * 文本编辑器退出确认：保存、不保存或留在当前页。
     *
     * @param onSave    点击「保存」
     * @param onDiscard 点击「不保存」
     */
    public static void showEditorUnsavedDialog(@NonNull Context context,
                                               @NonNull Runnable onSave,
                                               @NonNull Runnable onDiscard) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_warning)
                .setMessage(R.string.dialog_message_editor_unsaved)
                .setPositiveButton(R.string.save, (dialog, which) -> onSave.run())
                .setNegativeButton(R.string.dialog_button_discard, (dialog, which) -> onDiscard.run())
                .setNeutralButton(R.string.cancel, null)
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
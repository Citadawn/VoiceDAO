package com.citadawn.speechapp.util;

import android.content.Context;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;

import com.citadawn.speechapp.R;

/**
 * 信息图标工具类
 * 统一管理所有信息图标的点击事件和对话框显示
 * <p>
 * 使用示例：
 * InfoIconHelper.setupInfoIcon(context, findViewById(R.id.ivInfo),
 * R.string.info_title, R.string.info_content);
 */
public class InfoIconHelper {
    // region 批量设置

    /**
     * 批量设置信息图标
     *
     * @param context         上下文
     * @param infoIconConfigs 信息图标配置数组，每个元素包含 [ImageView, titleResId, contentResId]
     */
    public static void setupInfoIcons(Context context, Object[]... infoIconConfigs) {
        if (context == null || infoIconConfigs == null) return;
        for (Object[] config : infoIconConfigs) {
            if (config.length >= 3) {
                ImageView infoIcon = (ImageView) config[0];
                int titleResId = (Integer) config[1];
                int contentResId = (Integer) config[2];
                setupInfoIcon(context, infoIcon, titleResId, contentResId);
            }
        }
    }
    // endregion

    // region 单个设置

    /**
     * 设置信息图标（使用字符串资源ID）
     *
     * @param context      上下文
     * @param infoIcon     信息图标ImageView
     * @param titleResId   标题字符串资源ID
     * @param contentResId 内容字符串资源ID
     */
    public static void setupInfoIcon(Context context, ImageView infoIcon, int titleResId, int contentResId) {
        if (infoIcon != null && context != null) {
            infoIcon.setOnClickListener(v -> showInfoDialog(context, titleResId, contentResId));
        }
    }
    // endregion

    // region 对话框

    /**
     * 显示信息对话框（使用字符串资源ID）
     *
     * @param context      上下文
     * @param titleResId   标题字符串资源ID
     * @param contentResId 内容字符串资源ID
     */
    public static void showInfoDialog(Context context, int titleResId, int contentResId) {
        if (context != null) {
            new AlertDialog.Builder(context)
                    .setTitle(titleResId)
                    .setMessage(HtmlCompat.fromHtml(context.getString(contentResId), HtmlCompat.FROM_HTML_MODE_LEGACY))
                    .setPositiveButton(R.string.tts_support_info_button, null)
                    .show();
        }
    }
    // endregion
}
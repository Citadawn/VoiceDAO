package com.citadawn.speechapp.util;

import android.content.Context;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.LeadingMarginSpan;

import androidx.annotation.NonNull;

import com.citadawn.speechapp.R;

/**
 * 为 HTML 文本中的「•」列表项应用悬挂缩进，使换行与首行正文对齐。
 */
public final class HtmlBulletHelper {

    private static final char BULLET = '\u2022';

    private HtmlBulletHelper() {
    }

    public static boolean isHtmlMessage(@NonNull String message) {
        return message.contains("<") && message.contains(">");
    }

    @NonNull
    public static CharSequence formatInfoMessage(@NonNull Context context, int messageResId) {
        return formatInfoMessage(context, context.getString(messageResId));
    }

    @NonNull
    public static CharSequence formatInfoMessage(@NonNull Context context, @NonNull String message) {
        if (isHtmlMessage(message)) {
            return fromHtmlWithBulletIndent(context, message);
        }
        return message;
    }

    @NonNull
    public static CharSequence fromHtmlWithBulletIndent(@NonNull Context context, @NonNull String html) {
        Spanned spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
        SpannableStringBuilder builder = new SpannableStringBuilder(spanned);
        applyBulletHangingIndent(context, builder);
        return builder;
    }

    private static void applyBulletHangingIndent(@NonNull Context context, @NonNull SpannableStringBuilder builder) {
        String text = builder.toString();
        int restIndentPx = measureBulletTextIndentPx(context);

        int searchFrom = 0;
        while (searchFrom < text.length()) {
            int bulletIndex = text.indexOf(BULLET, searchFrom);
            if (bulletIndex < 0) {
                break;
            }
            if (bulletIndex > 0 && text.charAt(bulletIndex - 1) != '\n') {
                searchFrom = bulletIndex + 1;
                continue;
            }

            int blockEnd = text.indexOf('\n', bulletIndex);
            if (blockEnd < 0) {
                blockEnd = text.length();
            }

            builder.setSpan(new LeadingMarginSpan.Standard(0, restIndentPx), bulletIndex, blockEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            searchFrom = blockEnd + 1;
        }
    }

    private static int measureBulletTextIndentPx(@NonNull Context context) {
        float textSizePx = context.getResources().getDimension(R.dimen.sp_14);
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSizePx);
        return (int) Math.ceil(paint.measureText("• "));
    }
}

package com.citadawn.speechapp.util;

import android.content.Context;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 信息图标位置辅助工具类
 * 统一处理信息图标的动态定位
 */
public class InfoIconPositionHelper {
    
    /**
     * 为信息图标设置动态位置
     * @param context 上下文
     * @param imageView 信息图标
     * @param textView 对应的文字视图
     * @param percentage 文字高度的百分比（0.0-1.0）
     */
    public static void setIconPosition(Context context, ImageView imageView, TextView textView, float percentage) {
        if (imageView == null || textView == null) return;
        
        // 获取TextView的实际文字大小（px）
        float textSizePx = textView.getTextSize();
        
        // 计算文字高度
        int textHeight = getTextHeightFromPx(textSizePx);
        
        // 设置marginBottom为文字高度的指定百分比
        int marginBottom = (int) (textHeight * percentage);
        
        // 设置marginBottom
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
        if (params != null) {
            params.bottomMargin = marginBottom;
            imageView.setLayoutParams(params);
        }
    }
    
    /**
     * 为信息图标设置动态位置（使用默认80%百分比）
     * @param context 上下文
     * @param imageView 信息图标
     * @param textView 对应的文字视图
     */
    public static void setIconPosition(Context context, ImageView imageView, TextView textView) {
        setIconPosition(context, imageView, textView, 0.6f);
    }
    
    /**
     * 获取指定字体大小的文字高度（px单位）
     */
    private static int getTextHeightFromPx(float textSizePx) {
        // 创建Paint对象
        Paint paint = new Paint();
        paint.setTextSize(textSizePx);
        
        // 获取字体度量
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        
        // 计算文字高度
        float textHeight = fontMetrics.bottom - fontMetrics.top;
        
        return (int) textHeight;
    }
    
    /**
     * 获取指定字体大小的文字高度（sp单位）
     */
    public static int getTextHeight(Context context, float textSizeSp) {
        // 将sp转换为px
        float textSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 
            textSizeSp, 
            context.getResources().getDisplayMetrics()
        );
        
        return getTextHeightFromPx(textSizePx);
    }
} 
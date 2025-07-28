package com.citadawn.speechapp.util;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * View辅助工具类
 * 提供通用的View查找和操作功能
 */
public class ViewHelper {
    
    /**
     * 根据文本内容查找TextView
     * 
     * @param activity 当前Activity
     * @param text 要查找的文本内容
     * @return 找到的TextView，如果没找到返回null
     */
    public static TextView findTextViewByText(Activity activity, String text) {
        if (activity == null || text == null) return null;
        
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView instanceof ViewGroup) {
            return findTextViewInViewGroup((ViewGroup) rootView, text);
        }
        return null;
    }
    
    /**
     * 根据文本内容查找TextView（重载方法，使用View作为根视图）
     * 
     * @param rootView 根视图
     * @param text 要查找的文本内容
     * @return 找到的TextView，如果没找到返回null
     */
    public static TextView findTextViewByText(View rootView, String text) {
        if (rootView == null || text == null) return null;
        
        if (rootView instanceof ViewGroup) {
            return findTextViewInViewGroup((ViewGroup) rootView, text);
        }
        return null;
    }
    
    /**
     * 在ViewGroup中递归查找指定文本的TextView
     * 
     * @param viewGroup 要搜索的ViewGroup
     * @param text 要查找的文本内容
     * @return 找到的TextView，如果没找到返回null
     */
    public static TextView findTextViewInViewGroup(ViewGroup viewGroup, String text) {
        if (viewGroup == null || text == null) return null;
        
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                if (text.equals(textView.getText().toString())) {
                    return textView;
                }
            } else if (child instanceof ViewGroup) {
                TextView found = findTextViewInViewGroup((ViewGroup) child, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    /**
     * 根据文本内容查找TextView（用于Dialog等场景）
     * 
     * @param rootView 根视图
     * @param targetText 目标文本内容
     * @return 找到的TextView，如果没找到返回null
     */
    public static TextView findTextViewByTargetText(View rootView, String targetText) {
        if (rootView == null || targetText == null) return null;
        
        if (rootView instanceof ViewGroup) {
            return findTextViewInViewGroupByTargetText((ViewGroup) rootView, targetText);
        }
        return null;
    }
    
    /**
     * 在ViewGroup中递归查找指定目标文本的TextView
     * 
     * @param viewGroup 要搜索的ViewGroup
     * @param targetText 目标文本内容
     * @return 找到的TextView，如果没找到返回null
     */
    public static TextView findTextViewInViewGroupByTargetText(ViewGroup viewGroup, String targetText) {
        if (viewGroup == null || targetText == null) return null;
        
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                if (targetText.equals(textView.getText().toString())) {
                    return textView;
                }
            } else if (child instanceof ViewGroup) {
                TextView found = findTextViewInViewGroupByTargetText((ViewGroup) child, targetText);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
} 
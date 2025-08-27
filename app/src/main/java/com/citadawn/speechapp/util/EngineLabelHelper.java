package com.citadawn.speechapp.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;

import java.util.Locale;

/**
 * 引擎/应用标签获取工具
 * 基于当前应用界面语言，优先使用对方 APK 的本地化 label 资源；否则回退到系统提供的人类可读名称。
 */
public final class EngineLabelHelper {

    private EngineLabelHelper() {
    }

    /**
     * 按当前界面语言获取引擎（应用包）的本地化名称。
     *
     * @param context     当前上下文（用于获取界面语言与PM）
     * @param packageName 引擎包名
     * @return 本地化的人类可读名称；异常或获取失败时返回包名本身
     */
    public static String getLocalizedAppLabel(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);

            // 目标包的上下文
            Context pkgCtx = context.createPackageContext(packageName, 0);
            Resources baseRes = pkgCtx.getResources();

            // 按应用界面语言构造配置
            Locale appLocale = LocaleHelper.getCurrentLocale(context);
            Configuration cfg = new Configuration(baseRes.getConfiguration());
            cfg.setLocales(new LocaleList(appLocale));

            Context localizedPkgCtx = pkgCtx.createConfigurationContext(cfg);
            Resources localizedRes = localizedPkgCtx.getResources();

            if (ai.labelRes != 0) {
                // 直接用资源ID在目标包的本地化资源中取值
                CharSequence label = localizedRes.getText(ai.labelRes);
                return label.toString();
            }

            // 回退：系统的人类可读名称（根据系统/上下文语言）
            CharSequence fallback = pm.getApplicationLabel(ai);
            return fallback.toString();
        } catch (Throwable t) {
            return packageName;
        }
    }
}


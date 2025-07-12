package com.citadawn.speechapp;

import java.util.Locale;

public class LocaleDisplayNameMapper {
    public static String getLocaleDisplayName(Locale locale) {
        String code = locale.toString();
        switch (code) {
            case "zh_CN": return "中文（中国） zh_CN";
            case "zh_TW": return "中文（台湾） zh_TW";
            case "zh_HK": return "中文（香港） zh_HK";
            case "zh_SG": return "中文（新加坡） zh_SG";
            case "zh_MO": return "中文（澳门） zh_MO";
            case "en_US": return "英语（美国） en_US";
            case "en_GB": return "英语（英国） en_GB";
            case "en_AU": return "英语（澳大利亚） en_AU";
            case "en_CA": return "英语（加拿大） en_CA";
            case "en_IN": return "英语（印度） en_IN";
            case "en_NZ": return "英语（新西兰） en_NZ";
            case "en_SG": return "英语（新加坡） en_SG";
            case "fr_FR": return "法语（法国） fr_FR";
            case "fr_CA": return "法语（加拿大） fr_CA";
            case "fr_BE": return "法语（比利时） fr_BE";
            case "fr_CH": return "法语（瑞士） fr_CH";
            case "de_DE": return "德语（德国） de_DE";
            case "de_AT": return "德语（奥地利） de_AT";
            case "de_CH": return "德语（瑞士） de_CH";
            case "ja_JP": return "日语（日本） ja_JP";
            case "ko_KR": return "韩语（韩国） ko_KR";
            case "ru_RU": return "俄语（俄罗斯） ru_RU";
            case "es_ES": return "西班牙语（西班牙） es_ES";
            case "es_MX": return "西班牙语（墨西哥） es_MX";
            case "es_US": return "西班牙语（美国） es_US";
            case "it_IT": return "意大利语（意大利） it_IT";
            case "it_CH": return "意大利语（瑞士） it_CH";
            case "pt_PT": return "葡萄牙语（葡萄牙） pt_PT";
            case "pt_BR": return "葡萄牙语（巴西） pt_BR";
            case "ar_SA": return "阿拉伯语（沙特） ar_SA";
            case "ar_EG": return "阿拉伯语（埃及） ar_EG";
            case "nl_NL": return "荷兰语（荷兰） nl_NL";
            case "nl_BE": return "荷兰语（比利时） nl_BE";
            case "sv_SE": return "瑞典语（瑞典） sv_SE";
            case "fi_FI": return "芬兰语（芬兰） fi_FI";
            case "da_DK": return "丹麦语（丹麦） da_DK";
            case "no_NO": return "挪威语（挪威） no_NO";
            case "pl_PL": return "波兰语（波兰） pl_PL";
            case "tr_TR": return "土耳其语（土耳其） tr_TR";
            case "cs_CZ": return "捷克语（捷克） cs_CZ";
            case "el_GR": return "希腊语（希腊） el_GR";
            case "hu_HU": return "匈牙利语（匈牙利） hu_HU";
            case "th_TH": return "泰语（泰国） th_TH";
            case "vi_VN": return "越南语（越南） vi_VN";
            case "id_ID": return "印尼语（印尼） id_ID";
            case "ms_MY": return "马来语（马来西亚） ms_MY";
            default: return code;
        }
    }
}

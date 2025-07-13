package com.citadawn.speechapp;

import java.util.Locale;

public class LocaleDisplayNameMapper {
    public static String getLocaleDisplayName(Locale locale) {
        String code = locale.toString();
        switch (code) {
            // 常见语言优先
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
            case "ja_JP": return "日语（日本） ja_JP";
            case "ko_KR": return "韩语（韩国） ko_KR";
            case "ru_RU": return "俄语（俄罗斯） ru_RU";
            case "fr_FR": return "法语（法国） fr_FR";
            case "de_DE": return "德语（德国） de_DE";
            case "es_ES": return "西班牙语（西班牙） es_ES";
            case "it_IT": return "意大利语（意大利） it_IT";
            case "pt_PT": return "葡萄牙语（葡萄牙） pt_PT";
            case "ar_SA": return "阿拉伯语（沙特） ar_SA";
            case "hi_IN": return "印地语（印度） hi_IN";
            case "sw_KE": return "斯瓦希里语（肯尼亚） sw_KE";

            // 其余按代码首字母排序
            case "ar_EG": return "阿拉伯语（埃及） ar_EG";
            case "bg_BG": return "保加利亚语（保加利亚） bg_BG";
            case "bn_BD": return "孟加拉语（孟加拉国） bn_BD";
            case "bn_IN": return "孟加拉语（印度） bn_IN";
            case "bs_BA": return "波斯尼亚语（波斯尼亚和黑塞哥维那） bs_BA";
            case "ca_ES": return "加泰罗尼亚语（西班牙） ca_ES";
            case "cs_CZ": return "捷克语（捷克） cs_CZ";
            case "cy_GB": return "威尔士语（英国） cy_GB";
            case "da_DK": return "丹麦语（丹麦） da_DK";
            case "de_AT": return "德语（奥地利） de_AT";
            case "de_CH": return "德语（瑞士） de_CH";
            case "el_GR": return "希腊语（希腊） el_GR";
            case "en_NG": return "英语（尼日利亚） en_NG";
            case "es_MX": return "西班牙语（墨西哥） es_MX";
            case "es_US": return "西班牙语（美国） es_US";
            case "et_EE": return "爱沙尼亚语（爱沙尼亚） et_EE";
            case "fi_FI": return "芬兰语（芬兰） fi_FI";
            case "fil_PH": return "菲律宾语（菲律宾） fil_PH";
            case "fr_BE": return "法语（比利时） fr_BE";
            case "fr_CA": return "法语（加拿大） fr_CA";
            case "fr_CH": return "法语（瑞士） fr_CH";
            case "gu_IN": return "古吉拉特语（印度） gu_IN";
            case "he_IL": return "希伯来语（以色列） he_IL";
            case "hr_HR": return "克罗地亚语（克罗地亚） hr_HR";
            case "hu_HU": return "匈牙利语（匈牙利） hu_HU";
            case "id_ID": return "印尼语（印尼） id_ID";
            case "is_IS": return "冰岛语（冰岛） is_IS";
            case "it_CH": return "意大利语（瑞士） it_CH";
            case "jv_ID": return "爪哇语（印尼） jv_ID";
            case "km_KH": return "高棉语（柬埔寨） km_KH";
            case "kn_IN": return "卡纳达语（印度） kn_IN";
            case "lt_LT": return "立陶宛语（立陶宛） lt_LT";
            case "lv_LV": return "拉脱维亚语（拉脱维亚） lv_LV";
            case "ml_IN": return "马拉雅拉姆语（印度） ml_IN";
            case "mr_IN": return "马拉地语（印度） mr_IN";
            case "ms_MY": return "马来语（马来西亚） ms_MY";
            case "nb_NO": return "挪威博克马尔语（挪威） nb_NO";
            case "ne_NP": return "尼泊尔语（尼泊尔） ne_NP";
            case "nl_BE": return "荷兰语（比利时） nl_BE";
            case "nl_NL": return "荷兰语（荷兰） nl_NL";
            case "no_NO": return "挪威语（挪威） no_NO";
            case "pa_BD": return "旁遮普语（孟加拉国） pa_BD";
            case "pa_GJ": return "旁遮普语（印度） pa_GJ";
            case "pa_IN": return "旁遮普语（印度） pa_IN";
            case "pa_PK": return "旁遮普语（巴基斯坦） pa_PK";
            case "pl_PL": return "波兰语（波兰） pl_PL";
            case "pt_BR": return "葡萄牙语（巴西） pt_BR";
            case "ro_RO": return "罗马尼亚语（罗马尼亚） ro_RO";
            case "sk_SK": return "斯洛伐克语（斯洛伐克） sk_SK";
            case "si_LK": return "僧伽罗语（斯里兰卡） si_LK";
            case "sq_AL": return "阿尔巴尼亚语（阿尔巴尼亚） sq_AL";
            case "su_ID": return "巽他语（印尼） su_ID";
            case "sv_SE": return "瑞典语（瑞典） sv_SE";
            case "ta_IN": return "泰米尔语（印度） ta_IN";
            case "te_IN": return "泰卢固语（印度） te_IN";
            case "th_TH": return "泰语（泰国） th_TH";
            case "tr_TR": return "土耳其语（土耳其） tr_TR";
            case "uk_UA": return "乌克兰语（乌克兰） uk_UA";
            case "ur_PK": return "乌尔都语（巴基斯坦） ur_PK";
            case "vi_VN": return "越南语（越南） vi_VN";
            case "yue_HK": return "粤语（香港） yue_HK";
            default: return code;
        }
    }
}

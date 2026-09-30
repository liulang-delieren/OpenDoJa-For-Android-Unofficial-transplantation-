package com.nttdocomo.ui;

import java.util.Locale;
import java.util.Set;

final class DesktopFontSets {
    static final String[] GENERAL_FONT_SET = {
            "MS Gothic",
            "MS UI Gothic",
            "MS PGothic",
            "Yu Gothic UI",
            "Yu Gothic",
            "YuGothic",
            "Meiryo UI",
            "Meiryo",
            "Osaka-Mono",
            "Osaka",
            "Hiragino Kaku Gothic ProN",
            "Hiragino Kaku Gothic Pro",
            "Hiragino Sans",
            "Noto Sans Mono CJK JP",
            "Noto Sans CJK JP",
            "Noto Sans JP",
            "IPAexGothic",
            "IPAGothic",
            "Noto Sans Mono CJK SC",
            "Noto Sans CJK SC",
            "DejaVu Sans Mono",
            "DejaVu Sans"
    };
    static final String[] CHINESE_FONT_SET = {
            "SimSun", "宋体",
            "NSimSun", "新宋体",
            "MingLiU", "細明體",
            "PMingLiU", "新細明體",
            "Microsoft YaHei", "微软雅黑",
            "Noto Sans CJK SC",
            "Noto Sans SC",
            "Noto Sans CJK TC",
            "Noto Sans TC",
            "Noto Sans Mono CJK SC",
            "Noto Sans Mono CJK TC",
            "PingFang SC",
            "PingFang TC",
            "Hiragino Sans GB",
            "Songti SC",
            "Songti TC",
            "MS UI Gothic",
            "Meiryo UI",
            "MS Gothic"
    };

    private DesktopFontSets() {
    }

    static String resolveFamily(int face, Locale locale, Set<String> availableFamilies) {
        String[] candidates = isChineseLocale(locale) ? CHINESE_FONT_SET : GENERAL_FONT_SET;
        for (String candidate : candidates) {
            if (availableFamilies.contains(candidate.toLowerCase(Locale.ROOT))) {
                return candidate;
            }
        }
        return logicalFallback(face);
    }

    private static boolean isChineseLocale(Locale locale) {
        return locale != null && "zh".equalsIgnoreCase(locale.getLanguage());
    }

    private static String logicalFallback(int face) {
        if (face == Font.FACE_MONOSPACE) {
            return opendoja.compat.awt.Font.MONOSPACED;
        }
        if (face == Font.FACE_PROPORTIONAL) {
            return opendoja.compat.awt.Font.SANS_SERIF;
        }
        return opendoja.compat.awt.Font.DIALOG;
    }

}

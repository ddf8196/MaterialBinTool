package com.ddf.materialbintool.main.util;

import java.util.Locale;

public class StringUtil {
    public static String toUnderScore(String camel) {
        if (camel.contains("_"))
            return camel;
        return camel.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT);
    }
}

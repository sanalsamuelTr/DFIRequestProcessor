package com.tr.drp.common.utils;

public class StringUtils {
    public static String remSuffix(String text, String suffix) {
        return text.substring(0, text.length()-suffix.length());
    }
}

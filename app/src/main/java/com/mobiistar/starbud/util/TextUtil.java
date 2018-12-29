package com.mobiistar.starbud.util;

/**
 * Description: Text operation.
 * Date：18-11-5-下午5:46
 * Author: black
 */
public class TextUtil {

    private static final String NULL = "null";

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty() || text.equalsIgnoreCase(NULL);
    }
}

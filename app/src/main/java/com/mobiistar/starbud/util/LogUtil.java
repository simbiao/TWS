package com.mobiistar.starbud.util;

import android.util.Log;

import com.mobiistar.starbud.BuildConfig;


/**
 * Description: Log operation.
 * Date：18-11-2-下午5:07
 * Author: black
 */
public class LogUtil {

    private static final int LIMIT_LEVEL = Log.INFO;
    private static final boolean IS_DEBUG = BuildConfig.DEBUG;

    public static void printLog(int logLevel, String logTag, String logContent) {
        if (logLevel < LIMIT_LEVEL && !IS_DEBUG) {
            return;
        }

        Log.println(logLevel, logTag, logContent);
    }

    public static void printLog(int logLevel, String logTag, Exception e) {
        printLog(logLevel, logTag, "Exception : " + e.getMessage());
        if (IS_DEBUG) {
            e.printStackTrace();
        }
    }
}

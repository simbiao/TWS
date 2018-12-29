package com.mobiistar.starbud.util;

import android.util.Log;

import com.mobiistar.starbud.app.StarApp;

import java.io.File;

/**
 * Description: Record the crash info and save to file.
 * Date：18-11-5-上午10:18
 * Author: black
 */
public class CrashUtil {

    /**
     * Init in the StarApp.
     */
    public static void init() {
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                printCashInfo(t, e);
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

    private static void printCashInfo(Thread t, Throwable e) {
        e.printStackTrace();
        if (!StarUtil.hasFilePermission(StarApp.getInstance())) {
            LogUtil.printLog(Log.ERROR, "CrashUtil", "hasPermission==" + false);
            return;
        }
        String content = getPhoneInfo() + getThreadInfo(t) + getThrowableHeadInfo();
        String filePath = StarUtil.getCrashDir() + File.separator + System.currentTimeMillis() + StarUtil.EXTENSION_NAME_LOG;
        FileUtil.saveToFile(content, filePath);
        FileUtil.saveToFile(e, filePath);
    }

    private static String getPhoneInfo() {
        String phoneInfo = "============PhoneInfo============\n" +
                "DEVICE: " + android.os.Build.DEVICE + "\n" +
                "DISPLAY: " + android.os.Build.DISPLAY + "\n" +
                "CPU_ABI: " + android.os.Build.SUPPORTED_ABIS[0] + "\n" +
                "SDK_INT: " + android.os.Build.VERSION.SDK_INT + "\n" +
                "RELEASE: " + android.os.Build.VERSION.RELEASE + "\n" +
                "FINGERPRINT: " + android.os.Build.FINGERPRINT + "\n" + "\n";
        return phoneInfo;
    }

    private static String getThreadInfo(Thread t) {
        StringBuilder threadInfo = new StringBuilder();
        String clsName = t.getContextClassLoader().getClass().getSimpleName();
        String headInfo = "============ThreadInfo============\n" +
                "ClassName==" + clsName + "\n" +
                "Id==" + t.getId() + "\n" +
                "Name==" + t.getName() + "\n" +
                "Priority==" + t.getPriority() + "\n" +
                "State==" + t.getState() + "\n";
        threadInfo.append(headInfo);
        threadInfo.append("\n");
        return threadInfo.toString();
    }

    private static String getThrowableHeadInfo() {
        return "============ThrowableInfo============\n";
    }
}

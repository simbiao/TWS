package com.mobiistar.starbud.view.activity.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.mobiistar.starbud.util.LogUtil;

/**
 * Description: Base activity.
 * Date：18-11-2-下午3:39
 * Author: black
 */
public abstract class BaseActivity extends Activity {

    private long latestClickTime = 0;
    protected Context mContext;

    protected abstract int getContentLayoutId();

    protected abstract void init();

    protected abstract void unInit();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        printLogI("onCreate...");

        mContext = this;
        setContentView(getContentLayoutId());
        setStatusBarColor(Color.WHITE);
        init();
    }

    @Override
    protected void onDestroy() {
        unInit();

        printLogI("onDestroy...");
        super.onDestroy();
    }

    protected void setStatusBarColor(int color) {
        setStatusBarColor(color, true);
    }

    protected void setStatusBarColor(int color, boolean isDark) {
        int visibility = isDark ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        getWindow().getDecorView().setSystemUiVisibility(visibility);
        getWindow().setStatusBarColor(color);
    }

    protected void printLogD(String logContent) {
        LogUtil.printLog(Log.DEBUG, getClass().getSimpleName(), logContent);
    }

    protected void printLogI(String logContent) {
        LogUtil.printLog(Log.INFO, getClass().getSimpleName(), logContent);
    }

    protected void printLogE(String logContent) {
        LogUtil.printLog(Log.ERROR, getClass().getSimpleName(), logContent);
    }

    protected void printLogE(Exception e) {
        LogUtil.printLog(Log.ERROR, getClass().getSimpleName(), e);
    }

    protected boolean isInDuration(long duration) {
        long currentTime = SystemClock.uptimeMillis();
        if (currentTime - latestClickTime > duration) {
            latestClickTime = currentTime;
            return false;
        }
        return true;
    }
}

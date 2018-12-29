package com.mobiistar.starbud.app;

import android.app.Application;

import com.mobiistar.starbud.util.CrashUtil;

/**
 * Description: The Star app.
 * Date：18-11-2-下午4:14
 * Author: black
 */
public class StarApp extends Application {

    private static StarApp mStarApp;

    @Override
    public void onCreate() {
        super.onCreate();

        init();
    }

    private void init() {
        mStarApp = this;

        CrashUtil.init();
    }

    public static StarApp getInstance() {
        return mStarApp;
    }
}

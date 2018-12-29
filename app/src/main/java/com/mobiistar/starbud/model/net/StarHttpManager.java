package com.mobiistar.starbud.model.net;

import com.mobiistar.starbud.model.StarModelCallback;

/**
 * Description: Http get json operation manager.
 * Date：18-12-11-下午2:44
 * Author: black
 */
public class StarHttpManager {
    private static StarHttpManager mStarHttpManager;
    private static StarModelCallback mStarModelCallback;

    private StarHttpManager() {

    }

    public static StarHttpManager getInstance(StarModelCallback callback) {
        if (mStarHttpManager == null) {
            mStarHttpManager = new StarHttpManager();
        }
        mStarModelCallback = callback;
        return mStarHttpManager;
    }

    public void getResponse(String netUrl) {
        ThreadPoolManager.getInstance().execute(new StarHttpTask(mStarModelCallback, netUrl), netUrl);
    }
}

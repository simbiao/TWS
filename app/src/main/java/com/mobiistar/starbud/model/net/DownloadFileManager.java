package com.mobiistar.starbud.model.net;

import com.mobiistar.starbud.model.StarModelCallback;

/**
 * Description: File download util.
 * Date：18-11-13-下午5:01
 * Author: black
 */
public class DownloadFileManager {
    private static DownloadFileManager mDownloadFileManager;
    private static StarModelCallback mStarModelCallback;

    private DownloadFileManager() {

    }

    public static DownloadFileManager getInstance(StarModelCallback callback) {
        if (mDownloadFileManager == null) {
            mDownloadFileManager = new DownloadFileManager();
        }
        mStarModelCallback = callback;
        return mDownloadFileManager;
    }

    public void downloadFile(String netUrl, String saveUrl, long fileLength) {
        ThreadPoolManager.getInstance().execute(new DownloadTask(mStarModelCallback, netUrl, saveUrl, fileLength), saveUrl);
    }
}

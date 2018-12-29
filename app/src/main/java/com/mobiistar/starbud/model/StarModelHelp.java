package com.mobiistar.starbud.model;

import com.mobiistar.starbud.model.net.DownloadFileManager;
import com.mobiistar.starbud.model.net.StarHttpManager;

/**
 * Description: The port of dealing data for the star app.
 * Date：18-11-13-下午4:59
 * Author: black
 */
public class StarModelHelp {
    public static void downloadFile(String netUrl, String saveUrl, StarModelCallback callback, long fileLength) {
        DownloadFileManager.getInstance(callback).downloadFile(netUrl, saveUrl, fileLength);
    }

    public static void getHttpResponse(String netUrl, StarModelCallback callback) {
        StarHttpManager.getInstance(callback).getResponse(netUrl);
    }
}

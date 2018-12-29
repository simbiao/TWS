package com.mobiistar.starbud.model.net;

import android.util.Log;

import com.mobiistar.starbud.model.StarModelCallback;
import com.mobiistar.starbud.util.FileUtil;
import com.mobiistar.starbud.util.LogUtil;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Description: Download task.
 * Date：18-11-13-下午5:04
 * Author: black
 */
class DownloadTask extends BaseTask {

    private static final int CONNECT_TIME_OUT = 5000;
    private static final int BUFFER_SIZE = 4096;
    private static final int PROGRESS_MAX_VALUE = 100;
    private static final double DOUBLE_FLAG = 1.0;
    private static final String REQUEST_METHOD = "GET";
    private StarModelCallback mStarModelCallback;
    private String mNetUrl;
    private String mSaveUrl;
    private long mFileLength;

    DownloadTask(StarModelCallback callback, String netUrl, String saveUrl, long fileLength) {
        mStarModelCallback = callback;
        mNetUrl = netUrl;
        mSaveUrl = saveUrl;
        mFileLength = fileLength;
    }

    @Override
    public void run() {
        super.run();
        OutputStream outputStream = null;
        InputStream inputStream = null;
        int resultCode = Integer.MIN_VALUE;
        try {
            printLog("run...mNetUrl==" + mNetUrl);
            printLog("run...mSaveUrl==" + mSaveUrl);
            URL url = new URL(mNetUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(CONNECT_TIME_OUT);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod(REQUEST_METHOD);
//            httpURLConnection.disconnect();
//            httpURLConnection.connect();
            int responseCode = httpURLConnection.getResponseCode();
            int contentLength = httpURLConnection.getContentLength();
            printLog("run...responseCode==" + responseCode);
            printLog("run...contentLength==" + contentLength);
            File file = new File(mSaveUrl);
            boolean checkResult = FileUtil.checkFile(file);
            if (HttpURLConnection.HTTP_OK == responseCode && checkResult) {
                inputStream = httpURLConnection.getInputStream();
                outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[BUFFER_SIZE];
                int result = 0;
                long readLength = 0;
                while (result != -1 && mIsRunnable) {
                    readLength += result;
                    outputStream.write(buffer, 0, result);
                    result = inputStream.read(buffer, 0, buffer.length);
                    if (mFileLength > 0 && mStarModelCallback != null) {
                        mStarModelCallback.onProgress((int) (readLength * DOUBLE_FLAG / mFileLength * PROGRESS_MAX_VALUE));
                    }
                }
                printLog("run...mIsRunnable==" + mIsRunnable);
                printLog("run...readLength==" + readLength);
                outputStream.flush();
                if (mIsRunnable) {
                    resultCode = Integer.MAX_VALUE;
                }
            } else {
                resultCode = responseCode;
            }
        } catch (Exception e) {
            printLogE(e);
        } finally {
            closeStream(inputStream);
            closeStream(outputStream);
        }
        if (mStarModelCallback != null && mIsRunnable) {
            if (Integer.MAX_VALUE == resultCode) {
                mStarModelCallback.onSuccess(mSaveUrl);
            } else {
                mStarModelCallback.onFailed(resultCode);
            }
        }
    }

    private void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                printLogE(e);
            }
        }
    }

    private void printLog(String content) {
        LogUtil.printLog(Log.ERROR, getClass().getSimpleName(), content);
    }

    private void printLogE(Exception e) {
        LogUtil.printLog(Log.ERROR, getClass().getSimpleName(), e);
    }
}

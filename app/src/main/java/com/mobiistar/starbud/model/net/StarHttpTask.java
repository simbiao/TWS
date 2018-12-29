package com.mobiistar.starbud.model.net;

import android.util.Log;

import com.mobiistar.starbud.model.StarModelCallback;
import com.mobiistar.starbud.util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Description: Http task.
 * Date：18-12-11-下午2:47
 * Author: black
 */
public class StarHttpTask extends BaseTask {

    private static final int CONNECT_TIME_OUT = 5000;
    private static final int BUFFER_SIZE = 4096;
    private static final String REQUEST_METHOD = "GET";
    private StarModelCallback mStarModelCallback;
    private String mNetUrl;

    StarHttpTask(StarModelCallback callback, String netUrl) {
        mStarModelCallback = callback;
        mNetUrl = netUrl;
    }

    @Override
    public void run() {
        super.run();
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        String resultJson = null;
        int resultCode = Integer.MIN_VALUE;
        try {
            printLog("run...mNetUrl==" + mNetUrl);
            URL url = new URL(mNetUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(CONNECT_TIME_OUT);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod(REQUEST_METHOD);
            int responseCode = httpURLConnection.getResponseCode();
            int contentLength = httpURLConnection.getContentLength();
            printLog("run...responseCode==" + responseCode);
            printLog("run...contentLength==" + contentLength);
            if (HttpURLConnection.HTTP_OK == responseCode) {
                inputStream = httpURLConnection.getInputStream();
                outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                int result = 0;
                long readLength = 0;
                while (result != -1 && mIsRunnable) {
                    readLength += result;
                    outputStream.write(buffer, 0, result);
                    result = inputStream.read(buffer, 0, buffer.length);
                }
                printLog("run...mIsRunnable==" + mIsRunnable);
                printLog("run...readLength==" + readLength);
                outputStream.flush();
                resultJson = outputStream.toString();
                printLog("run...resultJson==" + resultJson);
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
                mStarModelCallback.onSuccess(resultJson);
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

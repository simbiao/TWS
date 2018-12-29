package com.mobiistar.starbud.model;

/**
 * Description: Star model operation callback.
 * Date：18-11-13-下午7:13
 * Author: black
 */
public interface StarModelCallback {
    void onFailed(int code);

    void onProgress(int progress);

    void onSuccess(String result);
}

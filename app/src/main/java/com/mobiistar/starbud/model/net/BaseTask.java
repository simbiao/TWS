package com.mobiistar.starbud.model.net;

/**
 * Description: Base task.
 * Date：18-11-14-下午5:08
 * Author: black
 */
public class BaseTask implements Runnable {
    protected boolean mIsRunnable = true;

    @Override
    public void run() {

    }

    public void setIsRunnable(boolean isRunnable) {
        mIsRunnable = isRunnable;
    }
}

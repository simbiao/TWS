package com.mobiistar.starbud.model.net;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Description: Thread pool manager.
 * Date：18-11-13-下午7:00
 * Author: black
 */
class ThreadPoolManager {
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAXIMUM_POOL_SIZE = 10;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final BlockingDeque<Runnable> WORK_QUEUE = new LinkedBlockingDeque<>(10);
    private static Map<String, BaseTask> mTaskMap = new HashMap<>(10);
    private static ThreadPoolManager mThreadPoolManager;

    private static ThreadPoolExecutor mThreadPoolExecutor;

    private ThreadPoolManager() {
        mThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, WORK_QUEUE);
    }

    public static ThreadPoolManager getInstance() {
        if (mThreadPoolManager == null) {
            mThreadPoolManager = new ThreadPoolManager();
        }
        return mThreadPoolManager;
    }

    public void execute(BaseTask command, String url) {
        BaseTask oldTask = mTaskMap.get(url);
        if (oldTask != null) {
            mThreadPoolExecutor.remove(oldTask);
            oldTask.setIsRunnable(false);
        }
        mTaskMap.put(url, command);
        mThreadPoolExecutor.execute(command);
    }

}

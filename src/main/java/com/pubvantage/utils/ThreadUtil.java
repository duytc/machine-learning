package com.pubvantage.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by quyendq on 16/03/2018.
 */
public class ThreadUtil {
    public static void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(1, TimeUnit.DAYS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static ExecutorService start(ExecutorService executorService) {
        executorService = Executors.newFixedThreadPool(ConfigLoaderUtil.getExecuteServiceThreadLeaner());
        return executorService;
    }
}

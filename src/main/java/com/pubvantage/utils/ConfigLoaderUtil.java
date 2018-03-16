package com.pubvantage.utils;

import java.util.Properties;

/**
 * Created by quyendq on 16/03/2018.
 */
public class ConfigLoaderUtil {
    private static Properties defaultConfig;
    private static Properties userConfig;
    private static final int THREAD_EXECUTE_SERVICE_SCORE_DEFAULT = 20;
    private static final int THREAD_EXECUTE_SERVICE_LEARNER_DEFAULT = 20;

    static {
        AppResource appResource = new AppResource();
        defaultConfig = appResource.getPropValues();
        userConfig = appResource.getUserConfiguration();
    }

    public static int getExecuteServiceThreadScore() {
        try {
            int userConfigValue = Integer.parseInt(userConfig.get("THREAD_EXECUTE_SERVICE_SCORE").toString());
            if (userConfigValue < THREAD_EXECUTE_SERVICE_SCORE_DEFAULT) {
                return THREAD_EXECUTE_SERVICE_SCORE_DEFAULT;
            }
            return userConfigValue;
        } catch (Exception e) {
            return THREAD_EXECUTE_SERVICE_SCORE_DEFAULT;
        }
    }

    public static int getExecuteServiceThreadLeaner() {
        try {
            int userConfigValue = Integer.parseInt(userConfig.get("THREAD_EXECUTE_SERVICE_LEARNER").toString());
            if (userConfigValue < THREAD_EXECUTE_SERVICE_LEARNER_DEFAULT) {
                return THREAD_EXECUTE_SERVICE_LEARNER_DEFAULT;
            }
            return userConfigValue;
        } catch (Exception e) {
            return THREAD_EXECUTE_SERVICE_LEARNER_DEFAULT;
        }
    }
}

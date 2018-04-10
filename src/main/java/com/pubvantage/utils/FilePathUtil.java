package com.pubvantage.utils;

import org.apache.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class FilePathUtil {
    private static Properties properties;
    private static final String BASE_FOLDER = "learnedModel";

    static {
        AppResource appResource = new AppResource();
        properties = appResource.getPropValues();
    }

    /**
     * @param optimizeRuleId auto optimization config id
     * @param identifier     identifier
     * @return string path of folder
     */
    public static String getLearnerModelPath(Long optimizeRuleId,
                                             String identifier,
                                             String optimizeField) {
        String baseFolder = properties.getProperty("path.learner.model");
        if (baseFolder == null || baseFolder.isEmpty()) {
            baseFolder = BASE_FOLDER;
        }
        return baseFolder + "/"
                + optimizeRuleId + "/"
                + identifier + "/"
                + optimizeField;
    }

}

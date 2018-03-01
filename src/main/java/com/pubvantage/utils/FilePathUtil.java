package com.pubvantage.utils;

import org.apache.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class FilePathUtil {
    private static Properties properties;
    static Logger logger = Logger.getLogger(FilePathUtil.class.getName());
    private static final String BASE_FOLDER = "learnedModel";

    static {
        AppResource appResource = new AppResource();
        properties = appResource.getPropValues();
    }

    /**
     *  create folder by auto optimization config id and identifier
     * @param autoOptimizationConfigId auto optimization config id
     * @param identifier identifier
     * @return string path of created folder
     */
    public static String createLearnedModeldFolder(Long autoOptimizationConfigId, String identifier, List<String> oneSegmentGroup, Object uniqueValue) {
        try {
            String filePath = getLearnerModelPath(autoOptimizationConfigId,identifier, oneSegmentGroup, uniqueValue);
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
            Files.createDirectories(path.getParent());
            Files.createFile(path);

            return filePath;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     *
     * @param autoOptimizationConfigId auto optimization config id
     * @param identifier identifier
     * @return string path of folder
     */
    public static String getLearnerModelPath(Long autoOptimizationConfigId, String identifier, List<String> oneSegmentGroup, Object uniqueValue) {
        String baseFolder = properties.getProperty("path.learner.model");
        if (baseFolder == null || baseFolder.isEmpty()) {
            baseFolder = BASE_FOLDER;
        }
        return baseFolder + "/" + autoOptimizationConfigId + "/" + identifier +"/";

    }

}

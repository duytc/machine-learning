package com.pubvantage.utils;

import org.apache.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Properties;
import java.util.Set;

public class FilePathHelper {
    private static AppResource appResource;
    private static Properties properties;
    static Logger logger = Logger.getLogger(FilePathHelper.class.getName());
    static {
        appResource = new AppResource();
        properties = appResource.getPropValues();
    }

    /**
     *
     * @param filePath path to create
     * @return path created of folder
     */
    public static String createDataConvertedFolder(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);

            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwxrwx");

//            Files.createDirectories(path.getParent().getParent(), PosixFilePermissions.asFileAttribute(perms));
            Files.createDirectories(path.getParent());
            Files.createFile(path);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return filePath;
    }

    /**
     *
     * @param autoOptimizationId auto optimization id
     * @param identifier identifier
     * @param fileName file name
     * @param fileType file type
     * @return path like ConvertedData/{autoOptimizationId}/{identifiers}/converted_data.txt
     */
    public static String getPathToSaveConvertData(long autoOptimizationId, String identifier, String fileName, String fileType) {
        String folderName = properties.getProperty("path.converted.data");
        if (folderName == null || folderName.isEmpty()) {
            folderName = "ConvertedData";
        }
        StringBuilder stringBuilder = new StringBuilder(folderName).append("/");

        return stringBuilder.append(autoOptimizationId)
                .append("/")
                .append(identifier)
                .append("/")
                .append(fileName)
                .append(".")
                .append(fileType).toString();
    }

    /**
     *
     * @param autoOptimizationId auto optimization id
     * @param identifiers identifier
     * @return path like ConvertedData/{autoOptimizationId}/{identifiers}
     */
    public static String getConvertedDataPath(long autoOptimizationId, String identifiers) {
        String folderName = properties.getProperty("path.converted.data");
        if (folderName == null || folderName.isEmpty()) {
            folderName = "ConvertedData";
        }
        StringBuilder stringBuilder = new StringBuilder(folderName).append("/");
        return stringBuilder.append(autoOptimizationId)
                .append("/")
                .append(identifiers)
                .toString();
    }

    /**
     *
     * @param autoOptimizationId auto optimization id
     * @param identifiers identifier
     * @return path like ConvertedData/{autoOptimizationId}/{identifiers}/converted_data.txt
     */

    public static String getConvertedDataFilePathFull(long autoOptimizationId, String identifiers) {
        StringBuilder stringBuilder = new StringBuilder(getConvertedDataPath(autoOptimizationId, identifiers));
        String folderName = properties.getProperty("data.convert");
        if (folderName == null || folderName.isEmpty()) {
            folderName = "converted_data";
        }

        stringBuilder.append("/").append(folderName).append(".txt");
        return stringBuilder.toString();
    }
}

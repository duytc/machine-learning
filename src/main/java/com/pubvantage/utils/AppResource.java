package com.pubvantage.utils;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppResource {
    private InputStream inputStream;
    private static Logger logger = Logger.getLogger(AppResource.class.getName());

    /**
     * get resource file
     *
     * @return Properties object
     */
    public Properties getPropValues() {
        Properties prop = new Properties();
        try {
            String propFileName = "config.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return prop;
    }

    public Properties getUserConfiguration() {
        Properties prop = new Properties();
        try {
            String propFileName = "./user.config.properties";
            inputStream = new FileInputStream(propFileName);
            prop.load(inputStream);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return prop;
    }
}

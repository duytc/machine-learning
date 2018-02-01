package com.pubvantage.utils;

import com.pubvantage.AppMain;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.Properties;

public class SparkSqlUtil {

    private Properties userConfig;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String dbTimeZone;

    private static SparkSqlUtil instance;

    private SparkSqlUtil() {
        AppResource appResource = new AppResource();
        userConfig = appResource.getUserConfiguration();
        getDataBaseConfig();
    }

    public static SparkSqlUtil getInstance() {
        if (instance == null) {
            instance = new SparkSqlUtil();
        }
        return instance;
    }

    private void getDataBaseConfig() {
        dbUrl = userConfig.getProperty("db.url");
        dbUser = userConfig.getProperty("db.user");
        dbPassword = userConfig.getProperty("db.password");
        dbTimeZone = userConfig.getProperty("db.serverTimezone");
    }

    /**
     * @param tableName table name
     * @return Dataset of tableName
     */
    public Dataset<Row> getDataSet(String tableName) {
        return AppMain.sparkSession.read()
                .format("jdbc")
                .option("url", dbUrl)
                .option("dbtable", tableName)
                .option("user", dbUser)
                .option("password", dbPassword)
                .option("serverTimezone", dbTimeZone)
                .load();
    }
}

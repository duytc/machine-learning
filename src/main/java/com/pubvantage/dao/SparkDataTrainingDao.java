package com.pubvantage.dao;

import com.pubvantage.AppMain;
import com.pubvantage.utils.AppResource;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.SparkSqlUtil;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SparkDataTrainingDao implements SparkDataTrainingDaoInterface {
    private SparkSqlUtil sqlUtil;
    private Properties userConfig;

    private String TABLE_NAME_PREFIX = "__data_training_";

    public SparkDataTrainingDao() {
        sqlUtil = SparkSqlUtil.getInstance();
        AppResource appResource = new AppResource();
        userConfig = appResource.getUserConfiguration();
    }

    /**
     * @param autoOptimizationConfigId auto optimization config id
     * @param identifier               input identifier
     * @return training data from database
     */
    public Dataset<Row> getDataSetByTable(Long autoOptimizationConfigId, String identifier, String[] objectiveAndFactor) {
        String tableName = TABLE_NAME_PREFIX +
                autoOptimizationConfigId;
        Dataset<Row> jdbcDF = sqlUtil.getDataSet(tableName);

        jdbcDF.createOrReplaceTempView(tableName);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        stringBuilder.append(String.join(",", objectiveAndFactor));
        stringBuilder.append(" FROM ");
        stringBuilder.append(tableName);

        if (identifier != null) {
            stringBuilder.append(" WHERE ");
            stringBuilder.append(userConfig.getProperty("column.identifier"))
                    .append(" = '").append(identifier).append("'")
                    .append(" AND ")
                    .append(ConvertUtil.generateAllIsNoteNull(objectiveAndFactor));

        }
        return AppMain.sparkSession.sql(stringBuilder.toString());
    }

    /**
     * @param autoOptimizationConfigId auto optimization config id
     * @return list contains identifiers
     */
    @Override
    public List<Row> getIdentifiers(Long autoOptimizationConfigId) {
        String tableName = TABLE_NAME_PREFIX + autoOptimizationConfigId;
        Dataset<Row> jdbcDF = sqlUtil.getDataSet(tableName);
        jdbcDF.createOrReplaceTempView(tableName);
        String stringQuery = "SELECT DISTINCT " +
                userConfig.getProperty("column.identifier") +
                " FROM " +
                tableName;

        Dataset<Row> sqlDF = AppMain.sparkSession.sql(stringQuery);

        return sqlDF.collectAsList();
    }

    @Override
    public List<Object> getDistinctByFactor(String factorName, Long autoOptimizationConfigId) {
        String tableName = TABLE_NAME_PREFIX + autoOptimizationConfigId;
        Dataset<Row> jdbcDF = sqlUtil.getDataSet(tableName);
        jdbcDF.createOrReplaceTempView(tableName);

        String stringQuery = "SELECT DISTINCT " + factorName + " FROM " + tableName;
        Dataset<Row> sqlDF = AppMain.sparkSession.sql(stringQuery);
        List<Row> resultList = sqlDF.collectAsList();
        List<Object> listData = new ArrayList<>();
        for (Row row : resultList) {
            Object data = row.get(0);
            listData.add(data);
        }
        return listData;
    }

    /**
     * consider avoid use collectAsList() if data is big. it cause out of memory.
     * @param optimizationRuleId
     * @param identifier
     * @param oneSegmentFieldGroup
     * @return
     */
    @Override
    public List<Object> getAllUniqueValuesForOneSegmentFieldGroup(Long optimizationRuleId, String identifier, List<String> oneSegmentFieldGroup) {
        String segments = String.join(",", oneSegmentFieldGroup);
        String tableName = TABLE_NAME_PREFIX + optimizationRuleId;
        Dataset<Row> jdbcDF = sqlUtil.getDataSet(tableName);
        jdbcDF.createOrReplaceTempView(tableName);

        String stringQuery = "SELECT DISTINCT " + segments + " FROM " + tableName + " WHERE identifier = " + identifier;
        Dataset<Row> sqlDF = AppMain.sparkSession.sql(stringQuery);
        List<Row> resultList = sqlDF.collectAsList();
        List<Object> listData = new ArrayList<>();
        for (Row row : resultList) {
            Object data = row.get(0);
            if (data != null)
                listData.add(data);
        }
        return listData;
    }
}

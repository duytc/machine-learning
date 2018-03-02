package com.pubvantage.dao;

import com.pubvantage.AppMain;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.utils.AppResource;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.SparkSqlUtil;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.*;

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

    @Override
    public Dataset<Row> getDataSet(Long optimizationRuleId,
                                   String identifier,
                                   List<String> objectiveAndFields,
                                   Map<String, Object> uniqueValue,
                                   List<String> oneSegmentGroup) {

        String tableName = TABLE_NAME_PREFIX + optimizationRuleId;
        Dataset<Row> jdbcDF = sqlUtil.getDataSet(tableName);

        jdbcDF.createOrReplaceTempView(tableName);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        stringBuilder.append(ConvertUtil.joinListString(objectiveAndFields, ", "));
        stringBuilder.append(" FROM ");
        stringBuilder.append(tableName);

        if (identifier != null) {
            stringBuilder.append(" WHERE ");
            stringBuilder.append(MyConstant.IDENTIFIER_COLUMN)
                    .append(" = '").append(identifier).append("'")
                    .append(" AND ");
            for (String field : oneSegmentGroup) {
                Object value = uniqueValue.get(field);
                if (value instanceof Date) {
                    stringBuilder.append("DATE_FORMAT(" + field + ", '" + MyConstant.DATE_FORMAT + "')")
                            .append(" = '")
                            .append(value.toString()).append("' AND ");
                } else if (value instanceof Number) {
                    stringBuilder.append(field)
                            .append(" = ")
                            .append(value).append(" AND ");
                } else {
                    stringBuilder.append(field)
                            .append(" = '")
                            .append(value).append("' AND ");
                }

            }
            stringBuilder.append(" 1 = 1");
        }
        Dataset<Row> rowDataset = AppMain.sparkSession.sql(stringBuilder.toString());
        return rowDataset;
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
     *
     * @param optimizationRuleId
     * @param identifier
     * @param oneSegmentFieldGroup
     * @return
     */
    @Override
    public List<Map<String, Object>> getAllUniqueValuesForOneSegmentFieldGroup(Long optimizationRuleId, String identifier, List<String> oneSegmentFieldGroup) {
        String segments = String.join(",", oneSegmentFieldGroup);
        String tableName = TABLE_NAME_PREFIX + optimizationRuleId;
        Dataset<Row> jdbcDF = sqlUtil.getDataSet(tableName);
        jdbcDF.createOrReplaceTempView(tableName);

        String stringQuery = "SELECT DISTINCT " + segments + " FROM " + tableName + " WHERE identifier = " + identifier;
        Dataset<Row> sqlDF = AppMain.sparkSession.sql(stringQuery);
        List<Row> resultList = sqlDF.collectAsList();
        List<Map<String, Object>> listData = new ArrayList<>();

        for (Row row : resultList) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (String field : oneSegmentFieldGroup) {
                map.put(field, row.getAs(field));
            }
            listData.add(map);
        }
        return listData;
    }
}

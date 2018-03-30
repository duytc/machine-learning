package com.pubvantage.dao;

import com.pubvantage.AppMain;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.utils.AppResource;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.JsonUtil;
import com.pubvantage.utils.SparkSqlUtil;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.spark.sql.functions.col;

public class SparkDataTrainingDao implements SparkDataTrainingDaoInterface {
    private SparkSqlUtil sqlUtil;
    private Properties userConfig;

    private String TABLE_NAME_PREFIX = "__data_training_";

    public SparkDataTrainingDao() {
        sqlUtil = SparkSqlUtil.getInstance();
        AppResource appResource = new AppResource();
        userConfig = appResource.getUserConfiguration();
    }

    @Override
    public Dataset<Row> getDataSet(Long optimizationRuleId,
                                   String identifier,
                                   List<String> objectiveAndFields,
                                   Map<String, Object> uniqueValue,
                                   List<String> oneSegmentGroup,
                                   String dateField) {

        String tableName = TABLE_NAME_PREFIX + optimizationRuleId;
        Dataset<Row> jdbcDF = sqlUtil.getDataSet(tableName);

        jdbcDF.createOrReplaceTempView(tableName);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        stringBuilder.append(ConvertUtil.joinListString(ConvertUtil.buildListSUMQuery(objectiveAndFields), ", "));
        stringBuilder.append(" FROM ").append(tableName)
                .append(" WHERE ")
                .append(ConvertUtil.generateAllIsNoteNull(objectiveAndFields))
                .append(" ");

        if (identifier != null) {
            stringBuilder.append(" AND ");
            stringBuilder.append(MyConstant.IDENTIFIER_COLUMN)
                    .append(" = '").append(identifier).append("'")
                    .append(" AND ");
            if (oneSegmentGroup != null) {
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
            }

            stringBuilder.append(" 1 = 1");
            stringBuilder.append(" GROUP BY " + ConvertUtil.removeSpace(dateField));
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
        String stringQuery = "SELECT DISTINCT identifier" + " FROM " + tableName;

        Dataset<Row> sqlDF = AppMain.sparkSession.sql(stringQuery);

        return sqlDF.collectAsList();
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
    public List<Map<String, Object>> getAllUniqueValuesForOneSegmentFieldGroup(
            Long optimizationRuleId, String identifier, List<String> oneSegmentFieldGroup) {
        String segments = String.join(",", oneSegmentFieldGroup);
        String tableName = TABLE_NAME_PREFIX + optimizationRuleId;
        Dataset<Row> jdbcDF = sqlUtil.getDataSet(tableName);
        jdbcDF.createOrReplaceTempView(tableName);

        String stringQuery = "SELECT DISTINCT " + segments + " FROM " + tableName + " WHERE identifier = '" + identifier + "'";
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

    @Override
    public List<String> getDistinctDates(Long optimizationRuleId, String dateField) {
        String tableName = TABLE_NAME_PREFIX + optimizationRuleId;
        Dataset<Row> jdbcDF = sqlUtil.getDataSet(tableName);
        jdbcDF.createOrReplaceTempView(tableName);

        String noSpaceDateField = ConvertUtil.removeSpace(dateField);
        String stringQuery = "SELECT DISTINCT " + noSpaceDateField + " FROM " + tableName + " ORDER BY " + noSpaceDateField;
        Dataset<Row> sqlDF = AppMain.sparkSession.sql(stringQuery);
        List<Row> resultList = sqlDF.collectAsList();
        List<String> listData = new ArrayList<>();
        for (Row row : resultList) {
            try {
                Date date = row.getDate(0);
                String dateString = new SimpleDateFormat(MyConstant.DATE_FORMAT_JAVA).format(date);
                listData.add(dateString);
            } catch (Exception e) {
                
            }
        }
        return listData;
    }

    @Override
    public List<Double> getVectorData(List<String> metrics, CoreOptimizationRule optimizationRule, String dateValue) {
        Long optimizeRuleId = optimizationRule.getId();
        String dateField = optimizationRule.getDateField();

        String tableName = TABLE_NAME_PREFIX + optimizeRuleId;
        Dataset<Row> jdbcDF = sqlUtil.getDataSet(tableName);

        jdbcDF.createOrReplaceTempView(tableName);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT").append(" " + ConvertUtil.removeSpace(dateField) + ", ");
        stringBuilder.append(ConvertUtil.joinListString(ConvertUtil.buildListSUMQuery(metrics), ", "));
        stringBuilder.append("FROM ").append(tableName)
                .append(" WHERE ")
                .append(ConvertUtil.generateAllIsNoteNull(ConvertUtil.buildListSUMQuery(metrics)))
                .append(" AND ");
        stringBuilder.append(" 1 = 1").append(" GROUP BY ").append(dateField);

        Dataset<Row> sqlDF = AppMain.sparkSession.sql(stringBuilder.toString());
        Dataset<Row> filteredDataSet = sqlDF.filter(col(dateField).equalTo(dateValue));
        List<Row> resultList = filteredDataSet.collectAsList();

        if (resultList != null && !resultList.isEmpty()) {
            List<Double> listData = new ArrayList<>();
            Row row = resultList.get(0);
            for (int i = 1; i < row.length(); i++) {
                //skip 0 is date field
                Double value = ConvertUtil.convertObjectToDouble(row.get(i));
                listData.add(value);
            }
            return listData;
        }
        return null;
    }

    @Override
    public Double getObjectiveFromDB(String identifier,
                                     Map<String, Object> oneSegmentGroup,
                                     List<String> metrics,
                                     OptimizeField optimizeField,
                                     CoreOptimizationRule optimizationRule,
                                     String dateValue) {
        Long optimizeRuleId = optimizationRule.getId();
        String dateField = optimizationRule.getDateField();
        String noSpaceDateField = ConvertUtil.removeSpace(dateField);
        String tableName = TABLE_NAME_PREFIX + optimizeRuleId;
        Dataset<Row> jdbcDF = sqlUtil.getDataSet(tableName);

        jdbcDF.createOrReplaceTempView(tableName);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT SUM( " + ConvertUtil.removeSpace(optimizeField.getField()) + ") ");
        stringBuilder.append("FROM ").append(tableName)
                .append(" WHERE ")
                .append(ConvertUtil.generateAllIsNoteNull(metrics))
                .append(" AND ")
                .append(noSpaceDateField + " = '" + dateValue + "'").append(" AND ");
        if (identifier != null) {
            stringBuilder.append(MyConstant.IDENTIFIER_COLUMN)
                    .append(" = '").append(identifier).append("'")
                    .append(" AND ");
        }
        if (oneSegmentGroup != null) {
            for (Map.Entry<String, Object> entry : oneSegmentGroup.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();
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
        }
        stringBuilder.append(" 1 = 1").append(" GROUP BY ").append(noSpaceDateField);
        Dataset<Row> sqlDF = AppMain.sparkSession.sql(stringBuilder.toString());
        List<Row> resultList = sqlDF.collectAsList();

        if (resultList != null && !resultList.isEmpty()) {
            Row row = resultList.get(0);
            return ConvertUtil.convertObjectToDouble(row.get(0));
        }
        return null;
    }

}

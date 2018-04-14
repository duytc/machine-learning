package com.pubvantage.dao;

import com.pubvantage.AppMain;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.entity.prediction.PredictDataWrapper;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.JsonUtil;
import com.pubvantage.utils.SparkSqlUtil;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.json4s.jackson.Json;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SparkDataTrainingDao implements SparkDataTrainingDaoInterface {
    private SparkSqlUtil sqlUtil;
    private static Logger logger = Logger.getLogger(SparkDataTrainingDao.class.getName());
    private String TABLE_NAME_PREFIX = "__data_training_";

    public SparkDataTrainingDao() {
        sqlUtil = SparkSqlUtil.getInstance();
    }


    /**
     * Get data set for training: Not use group by DATE and SUM in case 2 or many rows has same value due to some dimension fields are not chosen
     * Example: url is a dimension but it's not chosen as a segment. May be there are some row different from other by url only
     * => Handled by UR api
     */
    @Override
    public Dataset<Row> getDataSet(CoreOptimizationRule optimizationRule, String identifier, List<String> objectiveAndFields) {
        String tableName = TABLE_NAME_PREFIX + optimizationRule.getId();
        Dataset<Row> jdbcDF = sqlUtil.getDataSet(tableName);
        jdbcDF.createOrReplaceTempView(tableName);

        String sumString = ConvertUtil.joinListString(objectiveAndFields, ", ");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ")
                .append(sumString)
                .append(" FROM ")
                .append(tableName)
                .append(" WHERE ")
                .append(ConvertUtil.generateAllIsNoteNull(objectiveAndFields))
                .append(" ");

        if (identifier != null) {
            stringBuilder.append(" AND ")
                    .append(MyConstant.IDENTIFIER_COLUMN)
                    .append(" = '")
                    .append(identifier)
                    .append("'");
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
                Object rowValue = row.get(0);
                Date date = (Date) rowValue;

                String dateString = new SimpleDateFormat(MyConstant.DATE_FORMAT_JAVA).format(date);
                listData.add(dateString);
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        return listData;
    }

    /**
     * We use Group by and (SUM or AVG)  here but do not use when got training data
     * Handled by UR api
     */
    @Override
    public Double getObjectiveFromDB(PredictDataWrapper predictDataWrapper,
                                     List<String> metrics,
                                     List<String> dimensions,
                                     CoreOptimizationRule optimizationRule) {

        Long optimizeRuleId = optimizationRule.getId();
        String dateField = optimizationRule.getDateField();
        String noSpaceDateField = ConvertUtil.removeSpace(dateField);
        String tableName = TABLE_NAME_PREFIX + optimizeRuleId;
        Dataset<Row> jdbcDF = sqlUtil.getDataSet(tableName);
        OptimizeField optimizeField = JsonUtil.jsonToObject(predictDataWrapper.getOptimizeFieldJson(), OptimizeField.class);

        jdbcDF.createOrReplaceTempView(tableName);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT SUM( ")
                .append(ConvertUtil.removeSpace(optimizeField.getField()))
                .append(") ")
                .append("FROM ").append(tableName)
                .append(" WHERE ")
                .append(ConvertUtil.generateAllIsNoteNull(metrics))
                .append(" AND ")
                .append(noSpaceDateField)
                .append(" = '")
                .append(predictDataWrapper.getDate())
                .append("'")
                .append(" AND ");
        if (predictDataWrapper.getIdentifier() != null) {
            stringBuilder
                    .append(MyConstant.IDENTIFIER_COLUMN)
                    .append(" = '")
                    .append(predictDataWrapper.getIdentifier())
                    .append("'")
                    .append(" AND ");
        }
        Map<String, String> segmentGroup = JsonUtil.jsonToMap(predictDataWrapper.getSegmentJson());
        if (segmentGroup != null) {
            if (segmentGroup.isEmpty()) {
                // user choose no segment
                stringBuilder.append(" (");
                stringBuilder.append(ConvertUtil.generateAllIsGlobal(dimensions, MyConstant.SQL_OR));
                stringBuilder.append(" )");
                stringBuilder.append(" AND ");
            } else {
                for (Map.Entry<String, String> entry : segmentGroup.entrySet()) {
                    String field = entry.getKey();
                    Object value = entry.getValue();
                    stringBuilder.append(field).append(" = '").append(value).append("' AND ");
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

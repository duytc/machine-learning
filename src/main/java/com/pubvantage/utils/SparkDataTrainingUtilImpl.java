package com.pubvantage.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pubvantage.AppMain;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.*;

public class SparkDataTrainingUtilImpl implements SparkDataUtil {
    private AppResource appResource;
    private Properties properties;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String dbTimeZone;

    public SparkDataTrainingUtilImpl() {
        appResource = new AppResource();
        properties = appResource.getPropValues();

        dbUrl = properties.getProperty("db.url");
        dbUser = properties.getProperty("db.user");
        dbPassword = properties.getProperty("db.password");
        dbTimeZone = properties.getProperty("db.serverTimezone");

    }


    public String[] getListFactor(long autoOptimizationId) {
        String tableName = "core_auto_optimzation_config";
        Dataset<Row> jdbcDF = getDataSet(tableName);

        jdbcDF.createOrReplaceTempView(tableName);
        StringBuilder stringBuilder = new StringBuilder("SELECT objective, factors FROM ");
        stringBuilder.append(tableName);
        stringBuilder.append(" WHERE id = ");
        stringBuilder.append(autoOptimizationId);

        Dataset<Row> sqlDF = AppMain.spark.sql(stringBuilder.toString());
        List<Row> result = sqlDF.collectAsList();
        String[] objectiveAndFactors = null;
        String objective;

        if (result != null && !result.isEmpty()) {
            JsonParser jsonParser = new JsonParser();
            JsonArray arrayFromString;

            if (result.get(0) != null && result.get(0).size() > 0) {
                if (result.get(0).get(0) != null) {
                    objective = result.get(0).get(0).toString();
                    arrayFromString = jsonParser.parse(result.get(0).get(1).toString()).getAsJsonArray();

                    objectiveAndFactors = new String[1 + arrayFromString.size()];
                    objectiveAndFactors[0] = objective;
                    for (int i = 1; i < objectiveAndFactors.length; i++) {
                        objectiveAndFactors[i] = arrayFromString.get(i - 1).getAsString();
                    }
                }
            }

        }
        return objectiveAndFactors;
    }


    /**
     * @param tableName  name of table in database
     * @param identifier input identifier
     * @return training data from database
     */
    public Dataset<Row> getDataSetByTable(String tableName, String identifier, String[] objectiveAndFactor) {
        Dataset<Row> jdbcDF = AppMain.spark.read()
                .format("jdbc")
                .option("driver", "com.mysql.jdbc.Driver")
                .option("url", dbUrl)
                .option("dbtable", tableName)
                .option("user", dbUser)
                .option("password", dbPassword)
                .option("serverTimezone", dbTimeZone)
                .load();

        jdbcDF.createOrReplaceTempView(tableName);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ")
                .append(String.join(",", objectiveAndFactor));
        stringBuilder.append(" FROM ");


        stringBuilder.append(tableName);

        if (identifier != null) {
            stringBuilder.append(" WHERE ");
            stringBuilder.append(properties.getProperty("column.identifier"))
                    .append(" = '").append(identifier).append("'");
        }
        return AppMain.spark.sql(stringBuilder.toString());

    }

    /**
     * @param autoOptimizationId input autoOptimizationId
     * @return fieldType data in database
     */
    public JsonObject getFieldType(long autoOptimizationId) {
        JsonObject fieldTypeObj = new JsonObject();

        String tableName = "core_auto_optimzation_config";
        Dataset<Row> jdbcDF = getDataSet(tableName);

        jdbcDF.createOrReplaceTempView(tableName);
        StringBuilder stringBuilder = new StringBuilder("SELECT field_types FROM ");
        stringBuilder.append(tableName);
        stringBuilder.append(" WHERE id = ");
        stringBuilder.append(autoOptimizationId);

        Dataset<Row> sqlDF = AppMain.spark.sql(stringBuilder.toString());

        List<Row> resultList = sqlDF.collectAsList();
        if (resultList != null && !resultList.isEmpty()) {
            String fieldTypesJSON = "{}";
            if (resultList.get(0) != null && resultList.get(0).size() > 0) {
                fieldTypesJSON = resultList.get(0).get(0).toString();
            }

            Gson gson = new Gson();
            fieldTypeObj = gson.fromJson(fieldTypesJSON, JsonObject.class);
        }
        return fieldTypeObj;
    }

    /**
     * @param autoOptimizationId input autoOptimizationId
     * @param field              positive_factors or negative_factors
     * @return positive_factors or negative_factors data
     */
    public List<String> getPositiveNegativeField(long autoOptimizationId, String field) {
        ArrayList<String> resultList = new ArrayList<String>();
        String tableName = "core_auto_optimzation_config";
        Dataset<Row> jdbcDF = getDataSet(tableName);

        jdbcDF.createOrReplaceTempView(tableName);
        StringBuilder stringBuilder = new StringBuilder("SELECT ");
        stringBuilder.append(field);
        stringBuilder.append(" FROM ");
        stringBuilder.append(tableName);
        stringBuilder.append(" WHERE id = ");
        stringBuilder.append(autoOptimizationId);

        Dataset<Row> sqlDF = AppMain.spark.sql(stringBuilder.toString());
        List<Row> list = sqlDF.collectAsList();

        if (list != null && !list.isEmpty()) {
            String jsonArrayString = list.get(0).toString();

            Gson googleJson = new Gson();
            ArrayList<ArrayList<String>> javaArrayListFromGSON = googleJson.fromJson(jsonArrayString, ArrayList.class);

            if (javaArrayListFromGSON != null && !javaArrayListFromGSON.isEmpty()) {
                resultList = javaArrayListFromGSON.get(0);
            }
        }

        return resultList;
    }


    @Override
    public String[] filterListAutoOptimizationConfigId(String[] autoOptimizationIdParameterArray, boolean runAllAutoOptimizationConfigId) {
        String tableName = "core_auto_optimzation_config";
        Dataset<Row> jdbcDF = getDataSet(tableName);

        jdbcDF.createOrReplaceTempView(tableName);
        StringBuilder stringBuilder = new StringBuilder("SELECT DISTINCT id FROM ");
        stringBuilder.append(tableName);

        if (!runAllAutoOptimizationConfigId && autoOptimizationIdParameterArray.length > 0) {
            stringBuilder.append(" WHERE id IN (")
                    .append(String.join(",", autoOptimizationIdParameterArray))
                    .append(")");
        }

        Dataset<Row> sqlDF = AppMain.spark.sql(stringBuilder.toString());
        List<Row> resultList = sqlDF.collectAsList();

        if (resultList != null && !resultList.isEmpty()) {
            String[] array = new String[resultList.size()];
            for (int i = 0; i < resultList.size(); i++) {
                array[i] = resultList.get(i).get(0).toString();
            }
            return array;
        }
        return new String[0];
    }

    @Override
    public String[] filterIdentifier(String autoOptimizationConfigId, String[] identifierParameterArray, boolean runAllIdentifier) {
        String tableName = new StringBuilder("__data_training_").append(autoOptimizationConfigId).toString();
        Dataset<Row> jdbcDF = getDataSet(tableName);

        jdbcDF.createOrReplaceTempView(tableName);
        StringBuilder stringBuilder = new StringBuilder("SELECT DISTINCT ")
                .append(properties.getProperty("column.identifier"))
                .append(" FROM ")
                .append(tableName);

        if (!runAllIdentifier) {
            int length = identifierParameterArray.length;
            String[] temp = new String[identifierParameterArray.length];
            for (int i = 0; i < length; i++) {
                temp[i] = new StringBuilder("'")
                        .append(identifierParameterArray[i])
                        .append("'").toString();
            }
            if (temp.length > 0) {
                stringBuilder.append(" WHERE ")
                        .append(properties.getProperty("column.identifier"))
                        .append(" IN(")
                        .append(String.join(",", temp))
                        .append(")");
            }
        }

        Dataset<Row> sqlDF = AppMain.spark.sql(stringBuilder.toString());
        List<Row> resultList = sqlDF.collectAsList();
        String[] identifiers = new String[0];

        int index = 0;
        if (resultList != null && !resultList.isEmpty()) {
            int size = resultList.size();
            identifiers = new String[size];
            for (int i = 0; i < size; i++) {
                identifiers[index++] = resultList.get(i).get(0).toString();
            }
        }
        return identifiers;
    }


    /**
     * @param tableName table name
     * @return Dataset of tableName
     */
    private Dataset<Row> getDataSet(String tableName) {
        Dataset<Row> jdbcDF = AppMain.spark.read()
                .format("jdbc")
                .option("url", dbUrl)
                .option("dbtable", tableName)
                .option("user", dbUser)
                .option("password", dbPassword)
                .option("serverTimezone", dbTimeZone)
                .load();

        return jdbcDF;
    }
}

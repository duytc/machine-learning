package com.pubvantage.utils;

import com.google.gson.JsonObject;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;

public interface SparkDataUtil {

    String[] getListFactor(long autoOptimizationId);

    Dataset<Row> getDataSetByTable(String tableName, String identifier, String[] objectiveAndFactor);

    JsonObject getFieldType(long autoOptimizationId);

    List<String> getPositiveNegativeField(long autoOptimizationId, String type);


    String[] filterListAutoOptimizationConfigId(String[] autoOptimizationIdParameterArray, boolean runAllAutoOptimizationConfigId);

    String[] filterIdentifier(String autoOptimizationConfigId, String[] identifierParameterArray, boolean runAllIdentifier);
}

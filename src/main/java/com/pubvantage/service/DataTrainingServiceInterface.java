package com.pubvantage.service;

import com.pubvantage.RestParams.FactorConditionData;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface DataTrainingServiceInterface {
    String[] getIdentifiers(Long autoOptimizationConfigId);

    Dataset<Row> getDataSetByTable(Long autoOptimizationConfigId, String identifier, String[] objectiveAndFactor);

    LinkedHashMap<String, List<Object>> getFilteredConditions(Long autoOptimizationConfigId, List<FactorConditionData> conditionDataList);
}

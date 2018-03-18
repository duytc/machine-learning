package com.pubvantage.dao;

import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.OptimizeField;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;
import java.util.Map;

public interface SparkDataTrainingDaoInterface {

    Dataset<Row> getDataSet(Long optimizationRuleId, String identifier, List<String> objectiveAndFields,
                            Map<String, Object> uniqueValue, List<String> oneSegmentGroup, String dateField);

    List<Row> getIdentifiers(Long autoOptimizationConfigId);

    List<Map<String, Object>> getAllUniqueValuesForOneSegmentFieldGroup(Long optimizationRuleId, String identifier, List<String> oneSegmentFieldGroup);

    List<String> getDistinctDates(Long optimizationRuleId, String dateField);


    List<Double> getVectorData(List<String> metrics, CoreOptimizationRule optimizationRule, String dateValue);

    Double getObjectiveFromDB(String identifier,
                              Map<String, Object> segment,
                              List<String> metrics,
                              OptimizeField optimizeField,
                              CoreOptimizationRule optimizationRule,
                              String dateValue);
}

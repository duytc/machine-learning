package com.pubvantage.service.DataTraning;

import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.OptimizeField;

import java.util.List;
import java.util.Map;

public interface DataTrainingServiceInterface {
    Long getOptimizationRuleId();

    void setOptimizationRuleId(Long optimizationRuleId);

    String getIdentifier();

    void setIdentifier(String identifier);


    List<Map<String, Object>> getAllUniqueValuesForOneSegmentFieldGroup();

    List<Double> getVectorData(List<String> metrics, CoreOptimizationRule optimizationRule, String dateValue);

    Double getObjectiveFromDB(String identifier,
                              Map<String, Object> segment,
                              List<String> metrics,
                              OptimizeField optimizeField,
                              CoreOptimizationRule optimizationRule,
                              String dateValue);
}

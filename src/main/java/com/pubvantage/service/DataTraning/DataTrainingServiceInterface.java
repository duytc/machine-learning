package com.pubvantage.service.DataTraning;

import com.pubvantage.entity.CoreOptimizationRule;

import java.util.List;
import java.util.Map;

public interface DataTrainingServiceInterface {
    Long getOptimizationRuleId();

    void setOptimizationRuleId(Long optimizationRuleId);

    String getIdentifier();

    void setIdentifier(String identifier);


    List<Map<String, Object>> getAllUniqueValuesForOneSegmentFieldGroup();

    List<Double> getVectorData(List<String> optimizeFieldAndMetrics, CoreOptimizationRule optimizationRule, String dateValue);
}

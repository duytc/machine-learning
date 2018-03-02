package com.pubvantage.service.DataTraning;

import java.util.List;
import java.util.Map;

public interface DataTrainingServiceInterface {
    Long getOptimizationRuleId();

    void setOptimizationRuleId(Long optimizationRuleId);

    String getIdentifier();

    void setIdentifier(String identifier);

    List<String> getOneSegmentFieldGroup();

    void setOneSegmentFieldGroup(List<String> oneSegmentFieldGroup);

    List<Map<String, Object>> getAllUniqueValuesForOneSegmentFieldGroup();
}

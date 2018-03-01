package com.pubvantage.service.DataTraning;

import java.util.List;

public interface DataTrainingServiceInterface {
    Long getOptimizationRuleId();

    void setOptimizationRuleId(Long optimizationRuleId);

    String getIdentifier();

    void setIdentifier(String identifier);

    List<String> getOneSegmentFieldGroup();

    void setOneSegmentFieldGroup(List<String> oneSegmentFieldGroup);

    List<Object> getAllUniqueValuesForOneSegmentFieldGroup();
}

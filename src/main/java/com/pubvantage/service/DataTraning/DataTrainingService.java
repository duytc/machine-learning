package com.pubvantage.service.DataTraning;

import com.pubvantage.learner.Params.SegmentFieldGroup;

import java.util.List;

public class DataTrainingService {
    private Long optimizationRuleId;
    private String identifier;
    private List<String> oneSegmentFieldGroup;

    public DataTrainingService() {
    }

    public DataTrainingService(Long optimizationRuleId, String identifier, List<String> oneSegmentFieldGroup) {
        this.optimizationRuleId = optimizationRuleId;
        this.identifier = identifier;
        this.oneSegmentFieldGroup = oneSegmentFieldGroup;
    }

    public Long getOptimizationRuleId() {
        return optimizationRuleId;
    }

    public void setOptimizationRuleId(Long optimizationRuleId) {
        this.optimizationRuleId = optimizationRuleId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<String> getOneSegmentFieldGroup() {
        return oneSegmentFieldGroup;
    }

    public void setOneSegmentFieldGroup(List<String> oneSegmentFieldGroup) {
        this.oneSegmentFieldGroup = oneSegmentFieldGroup;
    }

    public List<Object> getAllUniqueValuesForOneSegmentFieldGroup() {
        // Select distinct values of one segment field groups
        return  null;
    }
}

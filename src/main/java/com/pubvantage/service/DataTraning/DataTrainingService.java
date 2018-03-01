package com.pubvantage.service.DataTraning;

import com.pubvantage.learner.Params.SegmentFieldGroup;

import java.util.List;

public class DataTrainingService implements DataTrainingServiceInterface {
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


    @Override
    public Long getOptimizationRuleId() {
        return null;
    }

    @Override
    public void setOptimizationRuleId(Long optimizationRuleId) {

    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public void setIdentifier(String identifier) {

    }

    @Override
    public List<String> getOneSegmentFieldGroup() {
        return null;
    }

    @Override
    public void setOneSegmentFieldGroup(List<String> oneSegmentFieldGroup) {

    }

    @Override
    public List<Object> getAllUniqueValuesForOneSegmentFieldGroup() {
        return null;
    }
}

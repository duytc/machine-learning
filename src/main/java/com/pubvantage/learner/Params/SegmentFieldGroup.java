package com.pubvantage.learner.Params;

import java.util.List;

public class SegmentFieldGroup {

    private Long optimizationRuleId;
    private String identifier;
    private List<String> oneSegmentFieldGroup;

    public SegmentFieldGroup() {
    }

    public SegmentFieldGroup(Long optimizationRuleId, String identifier, List<String> oneSegmentFieldGroup) {
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
}

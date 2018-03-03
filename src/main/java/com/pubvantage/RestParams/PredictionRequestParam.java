package com.pubvantage.RestParams;

import com.pubvantage.entity.FactorValues;
import com.pubvantage.entity.SegmentField;

import java.util.List;

public class PredictionRequestParam {
    private Long optimizationRuleId;
    private List<String> identifiers;
    private List<SegmentField> conditions;
    private String token;
    private FactorValues factorValues;

    public PredictionRequestParam(Long optimizationRuleId, List<String> identifiers, List<SegmentField> conditions, String token) {
        this.optimizationRuleId = optimizationRuleId;
        this.identifiers = identifiers;
        this.conditions = conditions;
        this.token = token;
    }

    public PredictionRequestParam() {
    }

    public Long getOptimizationRuleId() {
        return optimizationRuleId;
    }

    public void setOptimizationRuleId(Long optimizationRuleId) {
        this.optimizationRuleId = optimizationRuleId;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<String> identifiers) {
        this.identifiers = identifiers;
    }

    public List<SegmentField> getConditions() {
        return conditions;
    }

    public void setConditions(List<SegmentField> conditions) {
        this.conditions = conditions;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public FactorValues getFactorValues() {
        return factorValues;
    }

    public void setFactorValues(FactorValues factorValues) {
        this.factorValues = factorValues;
    }
}

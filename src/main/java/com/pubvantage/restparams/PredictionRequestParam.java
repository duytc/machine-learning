package com.pubvantage.restparams;

import com.pubvantage.entity.Condition;

import java.util.List;

public class PredictionRequestParam {
    private Long optimizationRuleId;
    private List<String> identifiers;
    private Condition conditions;
    private String token;

    public PredictionRequestParam(Long optimizationRuleId, List<String> identifiers, Condition conditions, String token) {
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

    public Condition getConditions() {
        return conditions;
    }

    public void setConditions(Condition conditions) {
        this.conditions = conditions;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}

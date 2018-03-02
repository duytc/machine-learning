package com.pubvantage.RestParams;

import com.google.gson.JsonArray;

import java.util.List;

public class PredictionRequestParam {
    private Long optimizationRuleId;
    private List<String> identifiers;
    private JsonArray conditions;
    private String token;

    public PredictionRequestParam(Long optimizationRuleId, List<String> identifiers, JsonArray conditions, String token) {
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

    public JsonArray getConditions() {
        return conditions;
    }

    public void setConditions(JsonArray conditions) {
        this.conditions = conditions;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

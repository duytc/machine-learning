package com.pubvantage.restparams;

public class PredictionRequestParam {
    private Long optimizationRuleId;
    private String token;

    public PredictionRequestParam(Long optimizationRuleId, String token) {
        this.optimizationRuleId = optimizationRuleId;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}

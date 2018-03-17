package com.pubvantage.restparams;

public class PredictionRequestParamV2 {
    private Long optimizationRuleId;
    private String token;

    public PredictionRequestParamV2(Long optimizationRuleId, String token) {
        this.optimizationRuleId = optimizationRuleId;
        this.token = token;
    }

    public PredictionRequestParamV2() {
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

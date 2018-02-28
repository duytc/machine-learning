package com.pubvantage.RestParams;

public class LearnerRequestParam {
    private Long optimizationRuleId;
    private String token;

    public LearnerRequestParam() {
    }

    public LearnerRequestParam(Long optimizationRuleId, String token) {
        this.optimizationRuleId = optimizationRuleId;
        this.token = token;
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

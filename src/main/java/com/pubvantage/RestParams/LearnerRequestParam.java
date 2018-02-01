package com.pubvantage.RestParams;

public class LearnerRequestParam {
    private Long autoOptimizationConfigId;
    private String token;

    public LearnerRequestParam() {
    }

    public LearnerRequestParam(Long autoOptimizationConfigId, String token) {
        this.autoOptimizationConfigId = autoOptimizationConfigId;
        this.token = token;
    }

    public Long getAutoOptimizationConfigId() {
        return autoOptimizationConfigId;
    }

    public void setAutoOptimizationConfigId(Long autoOptimizationConfigId) {
        this.autoOptimizationConfigId = autoOptimizationConfigId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

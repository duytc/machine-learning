package com.pubvantage.Authentication;

import com.pubvantage.service.OptimizationRuleServiceInterface;
import com.pubvantage.service.OptimizationRuleService;

public class Authentication {

    private Long autoOptimizationRuleId;
    private String token;

    public Authentication(Long autoOptimizationRuleId, String token) {
        this.autoOptimizationRuleId = autoOptimizationRuleId;
        this.token = token;
    }

    public boolean authenticate() {
        OptimizationRuleServiceInterface sqlService = new OptimizationRuleService();
        return sqlService.checkToken(this.autoOptimizationRuleId, this.token);
    }
}

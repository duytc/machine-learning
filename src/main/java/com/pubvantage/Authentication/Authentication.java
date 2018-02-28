package com.pubvantage.Authentication;

import com.pubvantage.service.CoreOptimizationRuleServiceInterface;
import com.pubvantage.service.CoreOptimizationRuleService;

public class Authentication {

    private Long autoOptimizationRuleId;
    private String token;

    public Authentication(Long autoOptimizationRuleId, String token) {
        this.autoOptimizationRuleId = autoOptimizationRuleId;
        this.token = token;
    }

    public boolean authenticate() {
        CoreOptimizationRuleServiceInterface sqlService = new CoreOptimizationRuleService();
        return sqlService.checkToken(this.autoOptimizationRuleId, this.token);
    }
}

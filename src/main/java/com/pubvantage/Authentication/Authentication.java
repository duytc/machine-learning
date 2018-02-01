package com.pubvantage.Authentication;

import com.pubvantage.service.CoreAutoOptimizationConfigServiceInterface;
import com.pubvantage.service.CoreAutoOptimizationConfigService;

public class Authentication {

    private Long autoOptimizationConfigId;
    private String token;

    public Authentication(Long autoOptimizationConfigId, String token) {
        this.autoOptimizationConfigId = autoOptimizationConfigId;
        this.token = token;
    }

    public boolean authenticate() {
        CoreAutoOptimizationConfigServiceInterface sqlService = new CoreAutoOptimizationConfigService();
        return sqlService.checkToken(this.autoOptimizationConfigId, this.token);
    }
}

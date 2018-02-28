package com.pubvantage.service;

import scala.Long;

public class CoreOptimizationRuleService implements CoreOptimizationRuleServiceInterface {
    @Override
    public String[] getSegmentFields(long optimizationRuleId) {
        return new String[0];
    }

    @Override
    public String[] getOptimizeFields(long optimizationRuleId) {
        return new String[0];
    }

    @Override
    public String getMetrics(Long optimizationRuleId) {
        return null;
    }

    @Override
    public String[] getIdentifiers(long optimizationRuleId) {
        return null;
    }

    @Override
    public boolean checkToken(Long autoOptimizationConfigId, String token) {
        return false;
    }
}

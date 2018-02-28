package com.pubvantage.service;

public interface CoreOptimizationRuleServiceInterface {
    String[] getSegmentFields(long optimizationRuleId);

    String[] getOptimizeFields(long optimizationRuleId);

    String getMetrics(long optimizationRuleId);

    String[] getIdentifiers(long optimizationRuleId);

    boolean checkToken(long autoOptimizationConfigId, String token);
}

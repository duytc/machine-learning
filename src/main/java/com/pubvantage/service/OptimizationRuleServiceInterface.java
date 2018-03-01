package com.pubvantage.service;

import com.google.gson.JsonObject;
import com.pubvantage.entity.CoreOptimizationRule;

import java.util.List;

public interface OptimizationRuleServiceInterface {
    List<String> getSegmentFields(CoreOptimizationRule optimizationRule);

    String[] getOptimizeFields(long optimizationRuleId);

    String[] getMetrics(long optimizationRuleId);

    List<String> getIdentifiers(CoreOptimizationRule optimizationRule);

    boolean checkToken(long autoOptimizationConfigId, String token);

    List<String> getFactors(Long id);

    JsonObject getFieldType(Long id);

    CoreOptimizationRule findById(Long autoOptimizationConfigId);

    String[] getObjectiveAndFactors(long autoOptimizationId);

    List<String> getPositiveFactors(long autoOptimizationId);

    List<String> getNegativeFactors(long autoOptimizationId);
}

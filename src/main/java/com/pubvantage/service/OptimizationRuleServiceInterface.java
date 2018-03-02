package com.pubvantage.service;

import com.google.gson.JsonObject;
import com.pubvantage.entity.CoreOptimizationRule;

import java.util.List;

public interface OptimizationRuleServiceInterface {
    List<String> getSegmentFields(Long optimizationRuleId);

    List<String> getOptimizeFields(Long optimizationRuleId);

    List<String> getMetrics(Long optimizationRuleId);

    List<String> getIdentifiers(CoreOptimizationRule optimizationRule);

    boolean checkToken(Long autoOptimizationConfigId, String token);

    List<String> getFactors(Long optimizationRuleId);

    JsonObject getFieldType(Long optimizationRuleId);

    CoreOptimizationRule findById(Long optimizationRuleId);

    String[] getObjectiveAndFactors(Long autoOptimizationId);

    List<String> getPositiveFactors(Long autoOptimizationId);

    List<String> getNegativeFactors(Long autoOptimizationId);
}

package com.pubvantage.service;

import com.google.gson.JsonObject;
import com.pubvantage.entity.CoreAutoOptimizationConfig;

import java.util.List;

public interface CoreOptimizationRuleServiceInterface {
    String[] getSegmentFields(long optimizationRuleId);

    String[] getOptimizeFields(long optimizationRuleId);

    String[] getMetrics(long optimizationRuleId);

    String[] getIdentifiers(long optimizationRuleId);

    boolean checkToken(long autoOptimizationConfigId, String token);

    List<String> getFactors(Long id);

    JsonObject getFieldType(Long id);

    CoreAutoOptimizationConfig findById(Long autoOptimizationConfigId);

    String[] getObjectiveAndFactors(long autoOptimizationId);

    List<String> getPositiveFactors(long autoOptimizationId);

    List<String> getNegativeFactors(long autoOptimizationId);
}

package com.pubvantage.service;

import com.google.gson.JsonObject;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.OptimizeField;

import java.util.List;

public interface OptimizationRuleServiceInterface {
    List<String> getSegmentFields(Long optimizationRuleId);

    List<String> getColumnsForScoreTable(CoreOptimizationRule optimizationRule);

    List<OptimizeField> getOptimizeFields(Long optimizationRuleId);

    List<OptimizeField> getOptimizeFields(CoreOptimizationRule optimizationRule);

    List<String> getMetrics(Long optimizationRuleId);

    String getDateField(Long optimizationRuleId);

    List<String> getVectorFields(Long optimizationRuleId);

    List<String> getIdentifiers(CoreOptimizationRule optimizationRule);

    boolean checkToken(Long autoOptimizationConfigId, String token);

    List<String> getFactors(Long optimizationRuleId);

    JsonObject getFieldType(Long optimizationRuleId);

    CoreOptimizationRule findById(Long optimizationRuleId);

    String[] getObjectiveAndFactors(Long autoOptimizationId);

    List<String> getPositiveFactors(Long autoOptimizationId);

    List<String> getNegativeFactors(Long autoOptimizationId);
}

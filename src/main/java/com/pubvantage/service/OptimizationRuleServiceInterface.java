package com.pubvantage.service;

import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.OptimizeField;

import java.util.List;

public interface OptimizationRuleServiceInterface {

    List<String> getColumnsForScoreTable(CoreOptimizationRule optimizationRule);

    List<OptimizeField> getOptimizeFields(Long optimizationRuleId);

    List<OptimizeField> getOptimizeFields(CoreOptimizationRule optimizationRule);

    List<String> getMetrics(Long optimizationRuleId);

    String getDateField(Long optimizationRuleId);

    List<String> getIdentifiers(CoreOptimizationRule optimizationRule);

    boolean checkToken(Long autoOptimizationConfigId, String token);

    CoreOptimizationRule findById(Long optimizationRuleId);

    void setLoadingForOptimizationRule(Long optimizationRuleId, boolean finishLoading);
}

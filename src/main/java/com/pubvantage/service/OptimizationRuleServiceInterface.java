package com.pubvantage.service;

import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.OptimizeField;

import java.util.List;

public interface OptimizationRuleServiceInterface extends GenericServiceInterface<CoreOptimizationRule>{

    List<String> getColumnsForScoreTable(CoreOptimizationRule optimizationRule);


    List<OptimizeField> getOptimizeFields(CoreOptimizationRule optimizationRule);

    List<String> getIdentifiers(CoreOptimizationRule optimizationRule);

    boolean checkToken(Long autoOptimizationConfigId, String token);

    boolean checkOptimizeFieldIsDigit(CoreOptimizationRule optimizationRule, OptimizeField optimizeFieldName);

    void setLoadingForOptimizationRule(Long optimizationRuleId, boolean finishLoading);

    List<String> getSegments(CoreOptimizationRule optimizationRule);

    List<String> getDimensions(CoreOptimizationRule optimizationRule);

    List<String> getNoSpaceDimensions(CoreOptimizationRule optimizationRule);

}

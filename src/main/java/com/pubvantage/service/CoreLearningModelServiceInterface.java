package com.pubvantage.service;

import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.entity.PredictListData;

import java.util.List;

public interface CoreLearningModelServiceInterface {

    void saveListLearnerModel(List<CoreLearner> modelList, CoreOptimizationRule optimizationRule);

    CoreLearner getOneCoreLeaner(Long optimizationRule, String identifier,
                                 String optimizeFieldJson,
                                 String segmentGroup);

    PredictListData getPredictData(CoreOptimizationRule optimizationRule);

    List<String> getMetricsFromCoreLeaner(CoreLearner coreLearner);

    String getTextSegmentConvertedRule(Long optimizationRuleId, String segmentGroup, String identifier);


    List<String> getDistinctIdentifiers(Long optimizationRuleId, String segmentGroup);


}

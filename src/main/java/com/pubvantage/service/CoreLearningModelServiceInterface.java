package com.pubvantage.service;

import com.pubvantage.entity.CoreAutoOptimizationConfig;
import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.CoreLearningModel;
import com.pubvantage.entity.OptimizeField;

import java.util.List;
import java.util.Map;

public interface CoreLearningModelServiceInterface {

    void saveListLearnerModel(List<CoreLearner> modelList);

    CoreLearner getOneCoreLeaner(Long optimizationRule, String identifier, OptimizeField optimizeFields, Map<String, Object> segmentValues);

    void saveListModel(List<CoreLearningModel> modelList);

    String getModelPath(CoreAutoOptimizationConfig coreAutoOptimizationConfig, String identifier);

    CoreLearningModel findOne(Long autOptimizationConfigId, String identifier);

}

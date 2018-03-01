package com.pubvantage.service;

import com.pubvantage.entity.CoreAutoOptimizationConfig;
import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.CoreLearningModel;

import java.util.List;

public interface CoreLearningModelServiceInterface {

    void saveListLearnerModel(List<CoreLearner> modelList);

    void saveListModel(List<CoreLearningModel> modelList);

    String getModelPath(CoreAutoOptimizationConfig coreAutoOptimizationConfig, String identifier);

    CoreLearningModel findOne(Long autOptimizationConfigId, String identifier);

}

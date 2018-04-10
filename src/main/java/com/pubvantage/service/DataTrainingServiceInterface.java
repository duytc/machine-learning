package com.pubvantage.service;

import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.prediction.PredictDataWrapper;

import java.util.List;

public interface DataTrainingServiceInterface {

    Double getRealObjectiveValueFromDB(PredictDataWrapper predictDataWrapper,
                                       List<String> metrics,
                                       List<String> dimensions,
                                       CoreOptimizationRule optimizationRule);
}

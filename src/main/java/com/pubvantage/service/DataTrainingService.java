package com.pubvantage.service;

import com.pubvantage.dao.SparkDataTrainingDao;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.prediction.PredictDataWrapper;
import com.pubvantage.service.DataTrainingServiceInterface;

import java.util.List;

public class DataTrainingService implements DataTrainingServiceInterface {
    private SparkDataTrainingDao sparkDataTrainingDao = new SparkDataTrainingDao();

    public DataTrainingService() {
    }

    @Override
    public Double getRealObjectiveValueFromDB(PredictDataWrapper predictDataWrapper,
                                              List<String> metrics,
                                              List<String> dimensions,
                                              CoreOptimizationRule optimizationRule) {
        return sparkDataTrainingDao.getObjectiveFromDB(predictDataWrapper, metrics, dimensions, optimizationRule);
    }
}

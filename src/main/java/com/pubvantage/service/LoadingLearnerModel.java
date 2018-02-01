package com.pubvantage.service;

import com.pubvantage.entity.CoreAutoOptimizationConfig;
import org.apache.spark.ml.regression.LinearRegressionModel;

public class LoadingLearnerModel implements LoadingLearnerModelInterface {

    private final CoreAutoOptimizationConfig autoOptimizationConfig;
    private final String identifier;

    public LoadingLearnerModel(CoreAutoOptimizationConfig autoOptimizationConfig, String identifier) {
        this.autoOptimizationConfig = autoOptimizationConfig;
        this.identifier = identifier;
    }

    private String getModelPath() {
        CoreLearningModelService coreLearningModelService = new CoreLearningModelService();

        return coreLearningModelService.getModelPath(autoOptimizationConfig, identifier);
    }

    @Override
    public LinearRegressionModel loadLearnerModel() {
        String modelPath = getModelPath();

        return LinearRegressionModel.load(modelPath);
    }
}

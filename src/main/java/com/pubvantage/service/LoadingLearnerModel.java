package com.pubvantage.service;

import com.pubvantage.entity.CoreOptimizationRule;
import org.apache.spark.ml.regression.LinearRegressionModel;

public class LoadingLearnerModel implements LoadingLearnerModelInterface {

    private CoreOptimizationRule optimizationRule;
    private String identifier;

    public LoadingLearnerModel(CoreOptimizationRule optimizationRule, String identifier) {
        this.optimizationRule = optimizationRule;
        this.identifier = identifier;
    }

    private String getModelPath() {
        CoreLearningModelService coreLearningModelService = new CoreLearningModelService();

        return coreLearningModelService.getModelPath(optimizationRule, identifier);
    }

    @Override
    public LinearRegressionModel loadLearnerModel() {
        String modelPath = getModelPath();

        return LinearRegressionModel.load(modelPath);
    }
}

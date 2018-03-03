package com.pubvantage.service;

import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.CoreOptimizationRule;
import org.apache.spark.ml.regression.LinearRegressionModel;

public class LoadingLearnerModel implements LoadingLearnerModelInterface {

    private CoreLearner coreLearner;

    public LoadingLearnerModel(CoreLearner coreLearner) {
        this.coreLearner = coreLearner;
    }

    private String getModelPath() {
        return coreLearner.getModelPath();
    }

    @Override
    public LinearRegressionModel loadLearnerModel() {
        String modelPath = getModelPath();

        return LinearRegressionModel.load(modelPath);
    }


}

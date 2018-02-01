package com.pubvantage.service;

import org.apache.spark.ml.regression.LinearRegressionModel;

public interface LoadingLearnerModelInterface {
    LinearRegressionModel loadLearnerModel();
}

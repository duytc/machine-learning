package com.pubvantage.learner;


import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.sql.SparkSession;

import java.util.List;
import java.util.Map;

public interface LearnerInterface {

    LinearRegressionModel generateModel(SparkSession sparkSession);

    String getModelStringData(LinearRegressionModel linearRegressionModel);

}
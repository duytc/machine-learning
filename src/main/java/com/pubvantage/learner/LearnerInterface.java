package com.pubvantage.learner;


import com.pubvantage.entity.ConvertedDataWrapper;
import com.pubvantage.learner.Params.LinearRegressionDataProcess;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.sql.SparkSession;

public interface LearnerInterface {

    LinearRegressionDataProcess getLinearRegressionDataProcess();

    LinearRegressionModel generateModel(SparkSession sparkSession);
}
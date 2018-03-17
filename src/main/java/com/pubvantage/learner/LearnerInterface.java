package com.pubvantage.learner;


import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.sql.SparkSession;

public interface LearnerInterface {


    LinearRegressionModel generateModel(SparkSession sparkSession);
}
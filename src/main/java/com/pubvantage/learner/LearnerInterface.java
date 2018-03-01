package com.pubvantage.learner;


import com.pubvantage.entity.ConvertedDataWrapper;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.sql.SparkSession;

public interface LearnerInterface {

    long getOptimizationRuleId();

    String getIdentifier() ;

    LinearRegressionModel getLrModel() ;

    LinearRegressionModel generateModel(SparkSession sparkSession);

    ConvertedDataWrapper getConvertedDataWrapper();

}
package com.pubvantage.learner;

import com.pubvantage.utils.FilePathHelper;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class LinearRegressionLearner implements LearnerInterface {
    private static final double REG_PARAM = 0.3;
    private static final double ELASTIC_NET_PARAM = 0.8;
    private static final int MAX_ITER = 10;
    private long autoOptimizationConfigId;
    private String identifier;
    private LinearRegressionModel lrModel;

    public LinearRegressionLearner(long autoOptimizationConfigId, String identifier, SparkSession sparkSession) {
        this.autoOptimizationConfigId = autoOptimizationConfigId;
        this.identifier = identifier;
        this.lrModel = generateModel(sparkSession);
    }

    public long getAutoOptimizationConfigId() {
        return autoOptimizationConfigId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public LinearRegressionModel getLrModel() {
        return lrModel;
    }

    private String getTrainingDataPath() {
        String path = FilePathHelper.getConvertedDataFilePathFull(this.autoOptimizationConfigId, this.identifier);

        //path = "dev/sample_linear_regression_data.txt";
        return path;
    }

    public LinearRegressionModel generateModel(SparkSession sparkSession) {
        String trainingDataPath = getTrainingDataPath();
        Dataset<Row> training = sparkSession
                .read()
                .format("libsvm")
                .load(trainingDataPath);

        LinearRegression lr = new LinearRegression()
                .setMaxIter(MAX_ITER)
                .setRegParam(REG_PARAM)
                .setElasticNetParam(ELASTIC_NET_PARAM);

        // Fit the model.
        LinearRegressionModel lrModel = lr.fit(training);
        // Print the coefficients and intercept for linear regression.
        System.out.println("Coefficients: " + lrModel.coefficients().toString() + " Intercept: " + lrModel.intercept());

        return lrModel;
    }
}

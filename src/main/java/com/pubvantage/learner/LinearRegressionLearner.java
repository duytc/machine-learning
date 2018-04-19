package com.pubvantage.learner;

import com.google.gson.JsonObject;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.FilePathUtil;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.ml.regression.LinearRegressionTrainingSummary;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.List;

public class LinearRegressionLearner implements LearnerInterface {
    private static final double REG_PARAM = 0.3;
    private static final double ELASTIC_NET_PARAM = 0.8;
    private static final int MAX_ITER = 500;

    private LinearRegressionTrainingDataProcess linearRegressionDataProcess;

    public LinearRegressionLearner(LinearRegressionTrainingDataProcess linearRegressionDataProcess) {
        this.linearRegressionDataProcess = linearRegressionDataProcess;
    }

    @Override
    public LinearRegressionModel generateModel(SparkSession sparkSession) {
        Dataset<Row> training = linearRegressionDataProcess.getTrainingDataForLinearRegression();
        System.out.println("Structured Data training");
        training.show(100, false);
        LinearRegression lr = new LinearRegression()
                .setMaxIter(MAX_ITER)
                .setRegParam(REG_PARAM)
                .setElasticNetParam(ELASTIC_NET_PARAM);

        try {
            // Fit the model.
            LinearRegressionModel lrModel = lr.fit(training);
            // Print the coefficients and intercept for linear regression.
            System.out.println("Coefficients: " + lrModel.coefficients().toString() + " Intercept: " + lrModel.intercept());

            // Summarize the model over the training set and print out some metrics.
            LinearRegressionTrainingSummary trainingSummary = lrModel.summary();
            System.out.println("numIterations: " + trainingSummary.totalIterations());
            System.out.println("objectiveHistory: " + Vectors.dense(trainingSummary.objectiveHistory()));
            trainingSummary.residuals().show();
            System.out.println("RMSE: " + trainingSummary.rootMeanSquaredError());
            System.out.println("r2: " + trainingSummary.r2());

            String savePath = FilePathUtil.getLearnerModelPath(
                    linearRegressionDataProcess.getOptimizationRule().getId(),
                    linearRegressionDataProcess.getIdentifier(),
                    linearRegressionDataProcess.getOptimizeField().getField());
            lrModel.write().overwrite().save(savePath);

            return lrModel;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getModelStringData(LinearRegressionModel linearRegressionModel) {
        List<String> objectiveAndFields = linearRegressionDataProcess.getObjectiveAndFields();
        List<String> digitMetrics = linearRegressionDataProcess.getDigitMetrics();
//        List<String> segments = linearRegressionDataProcess.getSegments();
        JsonObject jsonObject = new JsonObject();
        //coefficient
        Vector vec = linearRegressionModel.coefficients();
        double[] coefficientsArray = vec.toArray();
        JsonObject coefficient = new JsonObject();

        int coefficientsLength = coefficientsArray.length;
        for (int i = coefficientsLength - 1; i >= 0; i--) {
            int distance = coefficientsLength - 1 - i;
            String fieldName = getFieldName(distance, objectiveAndFields, digitMetrics);

            if (Double.isNaN(coefficientsArray[i])) {
                coefficient.addProperty(fieldName, MyConstant.NULL_COEFFICIENT);
            } else {
                double value = ConvertUtil.convertObjectToDecimal(coefficientsArray[i]).doubleValue();
                coefficient.addProperty(fieldName, value);
            }
        }
        jsonObject.add(MyConstant.COEFFICIENT, coefficient);
        if (Double.isNaN(linearRegressionModel.intercept())) {
            jsonObject.addProperty(MyConstant.INTERCEPT, MyConstant.NULL_COEFFICIENT);
        } else {
            double value = ConvertUtil.convertObjectToDecimal(linearRegressionModel.intercept()).doubleValue();
            jsonObject.addProperty(MyConstant.INTERCEPT, value);
        }
        return jsonObject.toString();
    }

    private String getFieldName(int distance, List<String> objectiveAndFields, List<String> digitMetrics) {
        if (objectiveAndFields == null) return null;

        if (distance >= digitMetrics.size()) return MyConstant.SEGMENT_KEY;

        int size = objectiveAndFields.size();
        int index = size - 1 - distance;
        if (index > 0) {
            // skip first index of objective
            return objectiveAndFields.get(index);
        }
        return MyConstant.SEGMENT_KEY;
    }

}

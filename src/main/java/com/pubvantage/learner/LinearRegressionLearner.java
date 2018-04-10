package com.pubvantage.learner;

import com.google.gson.JsonObject;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.FilePathUtil;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.List;
import java.util.Map;

public class LinearRegressionLearner implements LearnerInterface {
    private static final double REG_PARAM = 0.3;
    private static final double ELASTIC_NET_PARAM = 0.8;
    private static final int MAX_ITER = 10;

    private LinearRegressionTrainingDataProcess linearRegressionDataProcess;

    public LinearRegressionLearner(LinearRegressionTrainingDataProcess linearRegressionDataProcess) {
        this.linearRegressionDataProcess = linearRegressionDataProcess;
    }

    @Override
    public LinearRegressionModel generateModel(SparkSession sparkSession) {
        Dataset<Row> training = linearRegressionDataProcess.getTrainingDataForLinearRegression();
        if (training == null) {
            return null;
        }
        LinearRegression lr = new LinearRegression()
                .setMaxIter(MAX_ITER)
                .setRegParam(REG_PARAM)
                .setElasticNetParam(ELASTIC_NET_PARAM);

        try {
            // Fit the model.
            LinearRegressionModel lrModel = lr.fit(training);
            // Print the coefficients and intercept for linear regression.
            System.out.println("Coefficients: " + lrModel.coefficients().toString() + " Intercept: " + lrModel.intercept());
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
        JsonObject jsonObject = new JsonObject();
        //coefficient
        Vector vec = linearRegressionModel.coefficients();
        double[] coefficientsArray = vec.toArray();
        JsonObject coefficient = new JsonObject();
        for (int i = 0; i < coefficientsArray.length; i++) {
            int factorIndex = i + 1;// index 0 is objective
            if (Double.isNaN(coefficientsArray[i])) {
                coefficient.addProperty(objectiveAndFields.get(factorIndex), MyConstant.NULL_COEFFICIENT);
            } else {
                double value = ConvertUtil.convertObjectToDecimal(coefficientsArray[i]).doubleValue();
                coefficient.addProperty(objectiveAndFields.get(factorIndex), value);
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

    public Map<String, List<String>> getConvertedRule() {
        return this.linearRegressionDataProcess.getConvertedRule();
    }

}

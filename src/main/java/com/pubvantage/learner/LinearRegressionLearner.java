package com.pubvantage.learner;

import com.google.gson.JsonObject;
import com.pubvantage.AppMain;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.FilePathUtil;
import org.apache.spark.ml.feature.*;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        training.show(20, false);
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

        int coefficientsLength = coefficientsArray.length;
        for (int i = coefficientsLength - 1; i >= 0; i--) {
            int distance = coefficientsLength - 1 - i;
            String fieldName = getFieldName(distance, objectiveAndFields);
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

    private String getFieldName(int distance, List<String> objectiveAndFields) {
        if (objectiveAndFields == null) return null;
        int size = objectiveAndFields.size();
        int index = size - 1 - distance;
        if (index > 0) {
            // skip first index of objective
            return objectiveAndFields.get(index);
        }
        return MyConstant.SEGMENT_KEY;
    }

    public Map<String, List<String>> getConvertedRule() {
        return this.linearRegressionDataProcess.getConvertedRule();
    }

}

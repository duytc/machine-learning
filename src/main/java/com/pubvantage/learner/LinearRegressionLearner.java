package com.pubvantage.learner;

import com.pubvantage.entity.ConvertedDataWrapper;
import com.pubvantage.learner.Params.LinearRegressionDataProcess;
import com.pubvantage.utils.FilePathUtil;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LinearRegressionLearner implements LearnerInterface {
    private static final double REG_PARAM = 0.3;
    private static final double ELASTIC_NET_PARAM = 0.8;
    private static final int MAX_ITER = 10;

    private  SparkSession sparkSession;
   private LinearRegressionDataProcess linearRegressionDataProcess;


    public LinearRegressionLearner(SparkSession sparkSession, LinearRegressionDataProcess linearRegressionDataProcess) {
        this.sparkSession = sparkSession;
        this.linearRegressionDataProcess = linearRegressionDataProcess;
    }

    public SparkSession getSparkSession() {
        return sparkSession;
    }

    public void setSparkSession(SparkSession sparkSession) {
        this.sparkSession = sparkSession;
    }

    @Override
    public LinearRegressionDataProcess getLinearRegressionDataProcess() {
        return linearRegressionDataProcess;
    }

    public void setLinearRegressionDataProcess(LinearRegressionDataProcess linearRegressionDataProcess) {
        this.linearRegressionDataProcess = linearRegressionDataProcess;
    }

    @Override
    public LinearRegressionModel generateModel(SparkSession sparkSession) {
        Dataset<Row> training = linearRegressionDataProcess.getTrainingDataForLinearRegression();

        LinearRegression lr = new LinearRegression()
                .setMaxIter(MAX_ITER)
                .setRegParam(REG_PARAM)
                .setElasticNetParam(ELASTIC_NET_PARAM);

        // Fit the model.
        LinearRegressionModel lrModel = lr.fit(training);
        // Print the coefficients and intercept for linear regression.
        System.out.println("Coefficients: " + lrModel.coefficients().toString() + " Intercept: " + lrModel.intercept());

        try {
            String savePath = FilePathUtil.getLearnerModelPath( linearRegressionDataProcess.getOptimizationRuleId(),
                                                                linearRegressionDataProcess.getIdentifier(),
                                                                linearRegressionDataProcess.getOneSegmentGroup(),
                                                                linearRegressionDataProcess.getUniqueValue());
            lrModel.write().overwrite().save(savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lrModel;
    }
}

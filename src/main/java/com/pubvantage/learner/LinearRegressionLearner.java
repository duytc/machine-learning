package com.pubvantage.learner;

import com.pubvantage.entity.ConvertedDataWrapper;
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
    private long autoOptimizationConfigId;
    private String identifier;
    private LinearRegressionModel lrModel;
    private ConvertedDataWrapper convertedDataWrapper;


    public LinearRegressionLearner(long autoOptimizationConfigId, String identifier, SparkSession sparkSession, ConvertedDataWrapper convertedDataWrapper) {
        this.autoOptimizationConfigId = autoOptimizationConfigId;
        this.identifier = identifier;
        this.convertedDataWrapper = convertedDataWrapper;
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

    public LinearRegressionModel generateModel(SparkSession sparkSession) {
        Dataset<Row> training = extractDataToLearn(convertedDataWrapper.getDataSet());

        LinearRegression lr = new LinearRegression()
                .setMaxIter(MAX_ITER)
                .setRegParam(REG_PARAM)
                .setElasticNetParam(ELASTIC_NET_PARAM);

        // Fit the model.
        LinearRegressionModel lrModel = lr.fit(training);
        // Print the coefficients and intercept for linear regression.
        System.out.println("Coefficients: " + lrModel.coefficients().toString() + " Intercept: " + lrModel.intercept());

        try {
            String savePath = FilePathUtil.getLearnerModelPath(autoOptimizationConfigId, identifier);
            lrModel.write().overwrite().save(savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lrModel;
    }

    /**
     * @return object contain converted data, forecast, category weight, factors and objective
     */
    @Override
    public ConvertedDataWrapper getConvertedDataWrapper() {
        return this.convertedDataWrapper;
    }

    /**
     * @param convertedData converted data (text -> number)
     * @return Data to learn
     */
    private Dataset<Row> extractDataToLearn(Dataset<Row> convertedData) {
        StructType schema = new StructType(new StructField[]{
                new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
                new StructField("features", new VectorUDT(), false, Metadata.empty()),
        });

        ExpressionEncoder<Row> encoder = RowEncoder.apply(schema);

        Dataset<Row> output = convertedData.flatMap((FlatMapFunction<Row, Row>) rowData -> {
            Double label = Double.parseDouble(rowData.get(0).toString());

            double[] features = new double[rowData.size() - 1];
            for (int i = 0; i < features.length; i++) {
                int factorIndex = i + 1;
                features[i] = Double.parseDouble(rowData.get(factorIndex).toString());
            }

            Vector featuresVector = Vectors.dense(features);
            Row rowOutput = RowFactory.create(label, featuresVector);

            ArrayList<Row> list = new ArrayList<>();
            list.add(rowOutput);
            return list.iterator();
        }, encoder);

        List<Row> result = output.collectAsList();
        for (Row row : result) {
            for (int i = 0; i < row.length(); i++) {
                System.out.print(" " + row.get(i).toString());
            }
            System.out.println();
        }

        return output;
    }
}

package com.pubvantage.learner.Params;

import com.pubvantage.dao.SparkDataTrainingDao;
import com.pubvantage.dao.SparkDataTrainingDaoInterface;
import com.pubvantage.service.OptimizationRuleService;
import com.pubvantage.service.OptimizationRuleServiceInterface;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LinearRegressionDataProcess {
    private OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();
    SparkDataTrainingDaoInterface sparkDataTrainingDao = new SparkDataTrainingDao();
    private Long optimizationRuleId;
    private String identifier;
    private List<String> oneSegmentGroup;
    private Map<String, Object> uniqueValue;
    private String optimizeField;

    public LinearRegressionDataProcess() {
    }

    public LinearRegressionDataProcess(Long optimizationRuleId, String identifier, List<String> oneSegmentGroup, Map<String, Object> uniqueValue, String optimizeField) {
        this.optimizationRuleId = optimizationRuleId;
        this.identifier = identifier;
        this.oneSegmentGroup = oneSegmentGroup;
        this.uniqueValue = uniqueValue;
        this.optimizeField = optimizeField;
    }


    public Dataset<Row> getTrainingDataForLinearRegression() {
        List<String> objectiveAndFields = this.createObjectiveAndFields();
        Dataset<Row> dataSet = sparkDataTrainingDao.getDataSet(optimizationRuleId, identifier, objectiveAndFields, uniqueValue, oneSegmentGroup);
        Dataset<Row> vectorDataSet = null;
        if (dataSet != null) {
            vectorDataSet = this.extractDataToLearn(dataSet);
        }
        return vectorDataSet;
    }

    public List<String> createObjectiveAndFields() {
        List<String> metrics = optimizationRuleService.getMetrics(this.optimizationRuleId);
        if (metrics != null) {
            int indexOfOptimizeField = metrics.indexOf(this.optimizeField);
            if (indexOfOptimizeField >= 0) {
                metrics.remove(indexOfOptimizeField);
            }
        }
        List<String> objectiveAndFields = new ArrayList<>();
        objectiveAndFields.add(optimizeField);
        if (metrics != null) {
            objectiveAndFields.addAll(metrics);
        }
        return objectiveAndFields;
    }

    /**
     * @param convertedData
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


    public Long getOptimizationRuleId() {
        return optimizationRuleId;
    }

    public void setOptimizationRuleId(Long optimizationRuleId) {
        this.optimizationRuleId = optimizationRuleId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<String> getOneSegmentGroup() {
        return oneSegmentGroup;
    }

    public void setOneSegmentGroup(List<String> oneSegmentGroup) {
        this.oneSegmentGroup = oneSegmentGroup;
    }

    public Map<String, Object> getUniqueValue() {
        return uniqueValue;
    }

    public void setUniqueValue(Map<String, Object> uniqueValue) {
        this.uniqueValue = uniqueValue;
    }

    public String getOptimizeField() {
        return optimizeField;
    }

    public void setOptimizeField(String optimizeField) {
        this.optimizeField = optimizeField;
    }
}

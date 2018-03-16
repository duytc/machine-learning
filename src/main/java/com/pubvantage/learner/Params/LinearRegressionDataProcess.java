package com.pubvantage.learner.Params;

import com.google.gson.JsonObject;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.dao.SparkDataTrainingDao;
import com.pubvantage.dao.SparkDataTrainingDaoInterface;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.CoreReportView;
import com.pubvantage.entity.FactorDataType;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.service.Learner.ReportViewService;
import com.pubvantage.service.OptimizationRuleService;
import com.pubvantage.service.OptimizationRuleServiceInterface;
import com.pubvantage.service.ReportViewServiceInterface;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.JsonUtil;
import org.apache.commons.lang3.ArrayUtils;
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
import org.json4s.jackson.Json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LinearRegressionDataProcess {
    private OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();
    SparkDataTrainingDaoInterface sparkDataTrainingDao = new SparkDataTrainingDao();
    private ReportViewServiceInterface reportViewService = new ReportViewService();

    private Long optimizationRuleId;
    private String identifier;
    private List<String> oneSegmentGroup;
    private Map<String, Object> uniqueValue;
    private OptimizeField optimizeField;
    private List<String> objectiveAndFields;
    private JsonObject metricsPredictiveValues;

    public LinearRegressionDataProcess() {
    }

    public LinearRegressionDataProcess(Long optimizationRuleId,
                                       String identifier,
                                       List<String> oneSegmentGroup,
                                       OptimizeField optimizeField) {
        this.optimizationRuleId = optimizationRuleId;
        this.identifier = identifier;
        this.oneSegmentGroup = oneSegmentGroup;
        this.optimizeField = optimizeField;
    }

    public LinearRegressionDataProcess(Long optimizationRuleId,
                                       String identifier,
                                       List<String> oneSegmentGroup,
                                       Map<String, Object> uniqueValue,
                                       OptimizeField optimizeField) {
        this.optimizationRuleId = optimizationRuleId;
        this.identifier = identifier;
        this.oneSegmentGroup = oneSegmentGroup;
        this.uniqueValue = uniqueValue;
        this.optimizeField = optimizeField;
    }


    public Dataset<Row> getTrainingDataForLinearRegression() {
        List<String> objectiveAndFields = this.createObjectiveAndFields();
        if (objectiveAndFields == null || objectiveAndFields.size() <= 1) {
            // missing optimize field or metrics
            return null;
        }
        String dateField = this.getDateField(optimizationRuleId);
        Dataset<Row> dataSet = sparkDataTrainingDao.getDataSet(optimizationRuleId, identifier, objectiveAndFields, uniqueValue, oneSegmentGroup, dateField);
//        dataSet.show();
        this.metricsPredictiveValues = createMetricsPredictiveValues(dataSet);
        Dataset<Row> vectorDataSet = null;
        if (dataSet != null) {
            vectorDataSet = this.extractDataToLearn(dataSet);
        }
        return vectorDataSet;
    }

    private String getDateField(Long optimizeRuleId) {
        return optimizationRuleService.getDateField(optimizeRuleId);
    }


    private List<String> createObjectiveAndFields() {
        List<String> metrics = optimizationRuleService.getMetrics(this.optimizationRuleId);
        if (metrics != null) {
            int indexOfOptimizeField = metrics.indexOf(this.optimizeField.getField());
            if (indexOfOptimizeField >= 0) {
                metrics.remove(indexOfOptimizeField);
            }
        }
        List<String> objectiveAndFields = new ArrayList<>();
        boolean isOptimizeFieldNumber = checkOptimizeFieldIsNumber(optimizationRuleId, optimizeField.getField());
        if (optimizeField != null && !isOptimizeFieldNumber) {
            return new ArrayList<>();
        }
        objectiveAndFields.add(optimizeField.getField());
        if (metrics != null) {
            objectiveAndFields.addAll(metrics);
        }
        this.objectiveAndFields = objectiveAndFields;
        return objectiveAndFields;
    }

    private boolean checkOptimizeFieldIsNumber(Long optimizeRuleId, String optimizeFieldName) {
        CoreOptimizationRule coreOptimizationRule = optimizationRuleService.findById(optimizeRuleId);
        if (coreOptimizationRule != null && coreOptimizationRule.getReportViewId() != null) {
            CoreReportView reportView = reportViewService.findById(coreOptimizationRule.getReportViewId());
            if (reportView != null && reportView.getId() != null) {
                String jsonFieldType = reportView.getFieldTypes();
                Map<String, String> fieldType = JsonUtil.jsonToMap(jsonFieldType);
                String optimizeType = fieldType.get(optimizeFieldName);
                if (MyConstant.DECIMAL_TYPE.equals(optimizeType) || MyConstant.NUMBER_TYPE.equals(optimizeType)) {
                    return true;
                }
            }

        }
        return false;
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

    private JsonObject createMetricsPredictiveValues(Dataset<Row> trainingDataSet) {

        double[] forecastFactorValues = new double[this.objectiveAndFields.size()];
        //forecast  number value factor. (avg)
        Dataset<Row> avgDataSet = avgNumberedData(trainingDataSet, this.objectiveAndFields);
        List<String> objectiveAndFields = this.objectiveAndFields;

        List<Row> data = avgDataSet.collectAsList();
        for (Row row : data) {
            int numberTypeFactorIndex = -1;
            for (int col = 0; col < objectiveAndFields.size(); col++) {
                numberTypeFactorIndex++;
                if (row.get(numberTypeFactorIndex) instanceof Number) {
                    forecastFactorValues[col] = ConvertUtil.convertObjectToDouble(row.get(numberTypeFactorIndex).toString());
                }
            }
        }

        JsonObject forecast = new JsonObject();
        //skip objective index 0
        for (int col = 1; col < objectiveAndFields.size(); col++) {
            forecast.addProperty(objectiveAndFields.get(col), ConvertUtil.convertObjectToDecimal(forecastFactorValues[col]));
        }
        return forecast;
    }

    /**
     * @param trainingDataSet training data
     * @return average value of each factor and objective
     */
    private Dataset<Row> avgNumberedData(Dataset<Row> trainingDataSet, List<String> objectiveAndFields) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String objectiveAndField : objectiveAndFields) {
            map.put(objectiveAndField, "avg");
        }

        Dataset<Row> avgData = trainingDataSet.agg(map);
//        avgData.show();
        return avgData;
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

    public OptimizeField getOptimizeField() {
        return optimizeField;
    }

    public void setOptimizeField(OptimizeField optimizeField) {
        this.optimizeField = optimizeField;
    }

    public List<String> getObjectiveAndFields() {
        return objectiveAndFields;
    }

    public JsonObject getMetricsPredictiveValues() {
        return metricsPredictiveValues;
    }
}

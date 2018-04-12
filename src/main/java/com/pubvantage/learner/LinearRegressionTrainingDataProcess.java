package com.pubvantage.learner;

import com.pubvantage.constant.MyConstant;
import com.pubvantage.dao.ReportViewDao;
import com.pubvantage.dao.SparkDataTrainingDao;
import com.pubvantage.dao.SparkDataTrainingDaoInterface;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.CoreReportView;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.service.OptimizationRuleService;
import com.pubvantage.service.OptimizationRuleServiceInterface;
import com.pubvantage.service.ReportViewService;
import com.pubvantage.service.ReportViewServiceInterface;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.JsonUtil;
import org.apache.spark.ml.feature.*;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.ml.linalg.SparseVector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.*;

import static org.apache.spark.sql.functions.col;

public class LinearRegressionTrainingDataProcess {
    private OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();
    private SparkDataTrainingDaoInterface sparkDataTrainingDao = new SparkDataTrainingDao();
    private ReportViewServiceInterface reportViewService = new ReportViewService();
    private CoreOptimizationRule optimizationRule;
    private String identifier;
    private OptimizeField optimizeField;
    private List<String> objectiveAndFields;
    private Map<String, Map<String, double[]>> predictiveValues;
    private CoreReportView reportView;

    private Map<String, List<String>> convertedRule = null;
    private String[] vectorColumns = null;
    private String objective = null;
    private Dataset<Row> fullDf = null;

    public LinearRegressionTrainingDataProcess() {
    }

    public LinearRegressionTrainingDataProcess(CoreOptimizationRule optimizationRule,
                                               String identifier, OptimizeField optimizeField) {
        this.optimizationRule = optimizationRule;
        this.identifier = identifier;
        this.optimizeField = optimizeField;
    }

    public Dataset<Row> getTrainingDataForLinearRegression() {
        List<String> objectiveAndFields = this.createObjectiveAndFields();
        if (objectiveAndFields == null || objectiveAndFields.size() <= 1)
            return null; // missing optimize field or metrics

        Dataset<Row> dataSet = sparkDataTrainingDao.getDataSet(optimizationRule, identifier, objectiveAndFields);
        List<String> segments = optimizationRuleService.getSegments(optimizationRule);
        dataSet = convertTextToDigit(dataSet, segments);

        Dataset<Row> oneHotVectorDf = getOneHotVectorDataSet(dataSet);

        Dataset<Row> selectedDataSet = oneHotVectorDf.select(ConvertUtil.removeSpace(this.objective), this.vectorColumns);

        Dataset<Row> learnData = this.extractDataToLearn(selectedDataSet);
        learnData.show(false);

        this.predictiveValues = createMetricsPredictiveValues(oneHotVectorDf, this.convertedRule);
        return learnData;
    }

    /**
     * @return [optimize field, segment 1, segment 2..., digit metric 1, digit metric 2, ...]
     * We always have GLOBAL value for segment -> list of segments is not null
     */
    private List<String> createObjectiveAndFields() {
        List<String> digitMetrics = reportViewService.getNoSpaceDigitMetrics(getReportView(), this.optimizeField.getField());
        boolean isDigitOptimizeField = checkOptimizeFieldIsNumber(optimizationRule, optimizeField);
        if (optimizeField == null || !isDigitOptimizeField)
            return new ArrayList<>();

        //optimize field
        List<String> objectiveAndFields = new ArrayList<>();
        objectiveAndFields.add(optimizeField.getField());
        //add segments
        List<String> segments = optimizationRuleService.getSegments(optimizationRule);
        if (segments != null)
            objectiveAndFields.addAll(segments);
        // digit metrics
        if (digitMetrics != null)
            objectiveAndFields.addAll(digitMetrics);

        this.objectiveAndFields = ConvertUtil.removeSpace(objectiveAndFields);

        return this.objectiveAndFields;
    }

    private boolean checkOptimizeFieldIsNumber(CoreOptimizationRule optimizationRule, OptimizeField optimizeField) {
        return optimizationRuleService.checkOptimizeFieldIsDigit(optimizationRule, optimizeField);
    }

    /**
     * @param digitDataSet data set contain digit value only
     * @return data set has [label, features] structure so that it can be used by Machine Learning model
     */
    private Dataset<Row> extractDataToLearn(Dataset<Row> digitDataSet) {
        String features = "features";
        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(this.vectorColumns)
                .setOutputCol(features);

        Dataset<Row> output = assembler.transform(digitDataSet);
        output.show(false);
        return output.select(col(this.objective).alias("label"), col(features));
    }

    /**
     * @param rowDataSet a data set
     * @return data set contains optimizeField value, digit segments values and digit metrics value
     */
    private Dataset<Row> getOneHotVectorDataSet(Dataset<Row> rowDataSet) {
        List<String> segments = optimizationRuleService.getSegments(optimizationRule);
        String[] inputColumns = ConvertUtil.concatIndexToArray(segments, MyConstant.INDEX);
        String[] outputColumns = ConvertUtil.concatIndexToArray(segments, MyConstant.VECTOR);
        Dataset<Row> oneHotVectorDf = applyOneHotVector(rowDataSet, inputColumns, outputColumns);

        String optimizeFieldName = this.optimizeField.getField();
        List<String> digitMetrics = reportViewService.getNoSpaceDigitMetrics(getReportView(), optimizeFieldName);
        List<String> mixFields = new ArrayList<>(ConvertUtil.concatIndex(segments, MyConstant.VECTOR));
        mixFields.addAll(digitMetrics);

        String[] segmentsAndDigitMetrics = new String[mixFields.size()];
        segmentsAndDigitMetrics = mixFields.toArray(segmentsAndDigitMetrics);

        this.vectorColumns = segmentsAndDigitMetrics;
        this.objective = ConvertUtil.removeSpace(optimizeFieldName);

        return oneHotVectorDf;
    }

    private Dataset<Row> applyOneHotVector(Dataset<Row> rowDataSet, String[] inputColumns, String[] outputColumns) {
        OneHotEncoderEstimator encoder = new OneHotEncoderEstimator()
                .setInputCols(inputColumns)
                .setOutputCols(outputColumns);

        OneHotEncoderModel model = encoder.fit(rowDataSet);

        return model.transform(rowDataSet);
    }


    /**
     * @param df         data set
     * @param textFields list of text fields
     * @return convert text value to digit base on frequency of text values
     */
    private Dataset<Row> convertTextToDigit(Dataset<Row> df, List<String> textFields) {
        this.convertedRule = new HashMap<>();
        for (String field : textFields) {
            String outputField = field + MyConstant.INDEX;
            StringIndexerModel indexModelCategory = new StringIndexer()
                    .setInputCol(field)
                    .setOutputCol(outputField)
                    .fit(df);
            df = indexModelCategory.transform(df);

            //Save converted rule
            String[] labels = indexModelCategory.labels();
            convertedRule.put(field, Arrays.asList(labels));
        }
        return df;
    }



    /**
     * @param trainingDataSet a data set
     * @param convertedRule
     * @return prediction data base on each segment group.
     * Each segment group has list value of each field
     */
    private Map<String, Map<String, double[]>> createMetricsPredictiveValues(Dataset<Row> trainingDataSet,
                                                                           Map<String, List<String>> convertedRule) {
        trainingDataSet.show();

        List<String> segments = optimizationRuleService.getSegments(optimizationRule);
        List<String> objectiveAndFields = this.objectiveAndFields;

        //forecast  number value factor. (avg)
        Dataset<Row> avgDataSet = avgNumberedData(trainingDataSet, objectiveAndFields, segments);
        avgDataSet.show();

        avgDataSet = applyIntegratedVector(avgDataSet, segments);
        avgDataSet.show(false);

        List<Row> data = avgDataSet.collectAsList();
        String[] orderedFields = avgDataSet.columns();
        Map<String, Map<String, double[]>> predictionMap = new HashMap<>();
        for (Row row : data) {
            Map<String, String> segmentMap = new HashMap<>();
            if (segments.isEmpty()) {
                //Handle null segment (key-value 'NO_SEGMENT'-'NO_SEGMENT'). segment value become {} when save in database
                segmentMap.put(MyConstant.NO_SEGMENT, MyConstant.NO_SEGMENT);
            } else {
                segmentMap = new HashMap<>();
                for (String segment : segments) {
                    String segmentValue = row.getAs(segment);
                    segmentMap.put(segment, segmentValue);
                }
            }
            int segmentCount = segments.size();
            Map<String, double[]> forecastMap = new LinkedHashMap<>();
            for (int col = 0; col < orderedFields.length - segmentCount; col++) {
                String fieldKey = orderedFields[col];
//                Double avg = 0D;
                double[] doubles = null;
                Object segmentValue = row.getAs(fieldKey);
                if (col < segmentCount) {
                    String integratedColName = fieldKey + MyConstant.VECTOR_INTEGRATED;
                    Object object = row.getAs(integratedColName);
                    if (object instanceof DenseVector) {
                        doubles = ((DenseVector) object).toArray();
                    } else if (object instanceof SparseVector) {
                        doubles = ((SparseVector) object).toArray();
                    }
                    forecastMap.put(fieldKey, doubles);

                } else {
                    if (segmentValue instanceof Number) {
                        Double avg = ConvertUtil.convertObjectToDouble(row.get(col).toString());
                        doubles = new double[1];
                        doubles[0] = avg;
                    }
                    forecastMap.put(ConvertUtil.removeAvg(fieldKey), doubles);
                }
            }
            String jsonKey = JsonUtil.mapToJson(segmentMap);
            predictionMap.put(jsonKey, forecastMap);
        }

        return predictionMap;
    }

    private Dataset<Row> applyIntegratedVector(Dataset<Row> avgDataSet, List<String> segments) {
        for (String segment : segments) {
            String colName = segment + MyConstant.VECTOR_INTEGRATED;
            String[] arr = new String[1];
            arr[0] = ConvertUtil.concatMax(segment + MyConstant.VECTOR);
            VectorAssembler assembler = new VectorAssembler()
                    .setInputCols(arr)
                    .setOutputCol(colName);

            avgDataSet = assembler.transform(avgDataSet);
            avgDataSet = avgDataSet.drop(col(arr[0]));
        }
        return avgDataSet;
    }

    private Double getDigitValueFromConvertedRule(Map<String, List<String>> convertedRule, String fieldKey, Object segmentValue) {
        if (convertedRule == null) return null;
        List<String> convertedVector = convertedRule.get(fieldKey);
        if (convertedVector == null) return null;
        int index = convertedVector.indexOf(segmentValue.toString());

        return ConvertUtil.convertObjectToDouble(index);
    }

    /**
     * @param trainingDataSet    training data
     * @param objectiveAndFields coefficients
     * @param segments           ["countries Index", "Domains Index"].The labels when change text to digit
     * @return average value of each factor and objective
     */
    private Dataset<Row> avgNumberedData(Dataset<Row> trainingDataSet, List<String> objectiveAndFields, List<String> segments) {
        if (objectiveAndFields == null || objectiveAndFields.isEmpty()) {
            return null;
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 1; i < objectiveAndFields.size(); i++) {
            // Skip optimization field
            String field = objectiveAndFields.get(i);
            if (segments.indexOf(field) < 0)
                map.put(field, "avg");
        }
        List<String> vectorLabel = ConvertUtil.concatIndex(segments, MyConstant.VECTOR);
        for (String label : vectorLabel) {
            map.put(label, "max");
        }

        String firstSegment = null;
        String[] remainsSegments = new String[0];

        if (segments.size() >= 1) {
            firstSegment = segments.get(0);
            List<String> subList = segments.subList(1, segments.size());
            remainsSegments = new String[subList.size()];
            remainsSegments = subList.toArray(remainsSegments);
        }

        if (firstSegment == null) {
            return trainingDataSet.agg(map);
        }
        Dataset<Row> dataset = trainingDataSet.groupBy(firstSegment, remainsSegments).agg(map);
        dataset.show();
        return dataset;

    }


    public CoreOptimizationRule getOptimizationRule() {
        return optimizationRule;
    }

    public void setOptimizationRule(CoreOptimizationRule optimizationRule) {
        this.optimizationRule = optimizationRule;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public OptimizeField getOptimizeField() {
        return optimizeField;
    }

    public void setOptimizeField(OptimizeField optimizeField) {
        this.optimizeField = optimizeField;
    }

    public Map<String, Map<String, double[]>> getPredictiveValues() {
        return predictiveValues;
    }

    public List<String> getObjectiveAndFields() {
        return objectiveAndFields;
    }

    private CoreReportView getReportView() {
        if (reportView == null)
            reportView = reportViewService.findById(optimizationRule.getReportViewId(), new ReportViewDao());
        return reportView;
    }

    public Map<String, List<String>> getConvertedRule() {
        return convertedRule;
    }
}

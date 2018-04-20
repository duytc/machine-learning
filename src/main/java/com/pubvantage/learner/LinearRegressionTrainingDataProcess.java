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
import org.apache.spark.sql.catalyst.expressions.Conv;

import java.util.*;

import static org.apache.spark.sql.functions.col;

public class LinearRegressionTrainingDataProcess {
    private OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();
    private SparkDataTrainingDaoInterface sparkDataTrainingDao = new SparkDataTrainingDao();
    private ReportViewServiceInterface reportViewService = new ReportViewService();
    private CoreOptimizationRule optimizationRule;
    private String identifier;
    private OptimizeField optimizeField;
    /**
     * [optimize field, segment 1, segment 2..., digit metric 1, digit metric 2, ...]
     */
    private List<String> objectiveAndFields;
    private List<String> segments;
    private List<String> digitMetrics;

    private Map<String, Map<String, double[]>> predictiveValues;
    private CoreReportView reportView;

    public LinearRegressionTrainingDataProcess() {
    }

    public LinearRegressionTrainingDataProcess(CoreOptimizationRule optimizationRule,
                                               String identifier, OptimizeField optimizeField) {
        this.optimizationRule = optimizationRule;
        this.identifier = identifier;
        this.optimizeField = optimizeField;
    }

    Dataset<Row> getTrainingDataForLinearRegression() {
        List<String> objectiveAndFields = createObjectiveAndFields();
        if (objectiveAndFields == null || objectiveAndFields.size() <= 1)
            return null; // missing optimize field or metrics

        Dataset<Row> dataSet = sparkDataTrainingDao.getDataSet(optimizationRule, identifier, objectiveAndFields);
        dataSet = convertTextToDigit(dataSet, this.segments);
        Dataset<Row> oneHotVectorDf = getOneHotVectorDataSet(dataSet, this.segments);
        Dataset<Row> selectedDataSet = oneHotVectorDf.select(getNoSpaceOptimizeFieldName(), getOneHotVectorColumns());
        Dataset<Row> learnData = this.extractDataToLearn(selectedDataSet);

        this.predictiveValues = createMetricsPredictiveValues(oneHotVectorDf, this.segments);
        return learnData;
    }

    /**
     * @return get segments, digit metrics, objective
     */
    private List<String> createObjectiveAndFields() {
        this.digitMetrics = reportViewService.getNoSpaceDigitMetrics(getReportView(), this.optimizeField.getField());
        boolean isDigitOptimizeField = checkOptimizeFieldIsNumber(this.optimizationRule, this.optimizeField);
        if (this.optimizeField == null || !isDigitOptimizeField)
            return new ArrayList<>();

        //optimize field
        List<String> objectiveAndFields = new ArrayList<>();
        objectiveAndFields.add(optimizeField.getField());
        //add segments
        this.segments = this.optimizationRuleService.getNoSpaceSegments(this.optimizationRule);
        if (this.segments != null)
            objectiveAndFields.addAll(segments);
        // digit metrics
        if (this.digitMetrics != null)
            objectiveAndFields.addAll(this.digitMetrics);

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
                .setInputCols(getOneHotVectorColumns())
                .setOutputCol(features);

        Dataset<Row> output = assembler.transform(digitDataSet);
        return output.select(col(getNoSpaceOptimizeFieldName()).alias("label"), col(features));
    }

    /**
     * @param rowDataSet a data set
     * @param segments   segments
     * @return data set contains optimizeField value, digit segments values and digit metrics value
     */
    private Dataset<Row> getOneHotVectorDataSet(Dataset<Row> rowDataSet, List<String> segments) {
        String[] inputColumns = ConvertUtil.concatIndexToArray(segments, MyConstant.INDEX);
        String[] outputColumns = ConvertUtil.concatIndexToArray(segments, MyConstant.VECTOR);

        return applyOneHotVector(rowDataSet, inputColumns, outputColumns);
    }

    private String[] getOneHotVectorColumns() {
        List<String> mixFields = new ArrayList<>(ConvertUtil.concatIndex(this.segments, MyConstant.VECTOR));
        mixFields.addAll(ConvertUtil.removeSpace(this.digitMetrics));

        String[] segmentsAndDigitMetrics = new String[mixFields.size()];
        segmentsAndDigitMetrics = mixFields.toArray(segmentsAndDigitMetrics);
        return segmentsAndDigitMetrics;
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
        for (String field : textFields) {
            String outputField = field + MyConstant.INDEX;
            StringIndexerModel indexModelCategory = new StringIndexer()
                    .setInputCol(field)
                    .setOutputCol(outputField)
                    .fit(df);
            df = indexModelCategory.transform(df);
        }
        return df;
    }


    /**
     * @param trainingDataSet a data set
     * @param segments
     * @return prediction data base on each segment group.
     * Each segment group has list value of each field
     */
    private Map<String, Map<String, double[]>> createMetricsPredictiveValues(Dataset<Row> trainingDataSet, List<String> segments) {

        List<String> objectiveAndFields = this.objectiveAndFields;

        //forecast  number value factor. (avg)
        Dataset<Row> avgDataSet = avgNumberedData(trainingDataSet, objectiveAndFields, this.segments);

        avgDataSet = applyIntegratedVector(avgDataSet, this.segments);

        List<Row> data = avgDataSet.collectAsList();
        String[] orderedFields = avgDataSet.columns();
        Map<String, Map<String, double[]>> predictionMap = new HashMap<>();
        for (Row row : data) {
            Map<String, String> segmentMap = new HashMap<>();
            if (this.segments.isEmpty()) {
                //Handle null segment (key-value 'NO_SEGMENT'-'NO_SEGMENT'). segment value become {} when save in database
                segmentMap.put(MyConstant.NO_SEGMENT, MyConstant.NO_SEGMENT);
            } else {
                segmentMap = new HashMap<>();
                for (String segment : this.segments) {
                    String segmentValue = row.getAs(segment);
                    segmentMap.put(segment, segmentValue);
                }
            }
            int segmentCount = this.segments.size();
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


    /**
     * @param trainingDataSet    training data
     * @param objectiveAndFields coefficients
     * @param segments   ["countries Index", "Domains Index"].The labels when change text to digit
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
        return trainingDataSet.groupBy(firstSegment, remainsSegments).agg(map);

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

    public Map<String, Map<String, double[]>> getPredictiveValues() {
        return predictiveValues;
    }

    List<String> getObjectiveAndFields() {
        return objectiveAndFields;
    }

    private CoreReportView getReportView() {
        if (reportView == null)
            reportView = reportViewService.findById(optimizationRule.getReportViewId(), new ReportViewDao());
        return reportView;
    }

    List<String> getSegments() {
        return segments;
    }


    List<String> getDigitMetrics() {
        return digitMetrics;
    }

    private String getNoSpaceOptimizeFieldName() {
        return ConvertUtil.removeSpace(this.optimizeField.getField());
    }

}

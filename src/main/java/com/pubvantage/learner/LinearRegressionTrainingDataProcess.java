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
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
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

import java.util.*;

public class LinearRegressionTrainingDataProcess {
    private OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();
    private SparkDataTrainingDaoInterface sparkDataTrainingDao = new SparkDataTrainingDao();
    private ReportViewServiceInterface reportViewService = new ReportViewService();
    private CoreOptimizationRule optimizationRule;
    private String identifier;
    private OptimizeField optimizeField;
    private List<String> objectiveAndFields;
    private Map<String, Map<String, Double>> predictiveValues;
    private CoreReportView reportView;

    private Map<String, List<String>> convertedRule = null;

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
        Dataset<Row> digitDataSet = getDigitDataSet(dataSet);
        Dataset<Row> learnData = this.extractDataToLearn(digitDataSet);
        this.predictiveValues = createMetricsPredictiveValues(dataSet);
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
        StructType schema = new StructType(new StructField[]{
                new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
                new StructField("features", new VectorUDT(), false, Metadata.empty()),
        });

        ExpressionEncoder<Row> encoder = RowEncoder.apply(schema);

        return digitDataSet.flatMap((FlatMapFunction<Row, Row>) rowData -> {
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
    }

    /**
     * @param rowDataSet a data set
     * @return data set contains optimizeField value, digit segments values and digit metrics value
     */
    private Dataset<Row> getDigitDataSet(Dataset<Row> rowDataSet) {
        List<String> segments = optimizationRuleService.getSegments(optimizationRule);
        rowDataSet = convertTextToDigit(rowDataSet, segments);

        String optimizeFieldName = this.optimizeField.getField();
        List<String> digitMetrics = reportViewService.getNoSpaceDigitMetrics(getReportView(), optimizeFieldName);
        List<String> mixFields = new ArrayList<>(ConvertUtil.concatIndex(segments, MyConstant.INDEX));
        mixFields.addAll(digitMetrics);

        String[] segmentsAndDigitMetrics = new String[mixFields.size()];
        segmentsAndDigitMetrics = mixFields.toArray(segmentsAndDigitMetrics);

//        rowDataSet.show();
        return rowDataSet.select(optimizeFieldName, segmentsAndDigitMetrics);
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
     * @return prediction data base on each segment group.
     * Each segment group has list value of each field
     */
    private Map<String, Map<String, Double>> createMetricsPredictiveValues(Dataset<Row> trainingDataSet) {
        List<String> segments = optimizationRuleService.getSegments(optimizationRule);
        List<String> objectiveAndFields = this.objectiveAndFields;

        //forecast  number value factor. (avg)
        Dataset<Row> avgDataSet = avgNumberedData(trainingDataSet, objectiveAndFields, segments);
        List<Row> data = avgDataSet.collectAsList();
        String[] orderedFields = avgDataSet.columns();
        Map<String, Map<String, Double>> predictionMap = new HashMap<>();
        for (Row row : data) {
            Map<String, String> segmentMap = new HashMap<>();
            if (segments == null || segments.isEmpty()) {
                //TODO (Comment) handle null segment (key-value 'NO_SEGMENT'-'NO_SEGMENT')
                segmentMap.put(MyConstant.NO_SEGMENT, MyConstant.NO_SEGMENT);
            } else {
                segmentMap = new HashMap<>();
                for (String segment : segments) {
                    String segmentValue = row.getAs(segment);
                    segmentMap.put(segment, segmentValue);
                }
            }
            int segmentCount = segments == null ? 0 : segments.size();
            Map<String, Double> forecastMap = new HashMap<>();
            for (int col = segmentCount; col < orderedFields.length; col++) {
                String fieldKey = orderedFields[col];
                Double avg = 0D;
                if (row.getAs(fieldKey) instanceof Number) {
                    avg = ConvertUtil.convertObjectToDouble(row.get(col).toString());
                }
                forecastMap.put(ConvertUtil.removeAvg(fieldKey), avg);
            }
            // TODO Need to ensure order segments is same {country: VN, domain: a.com}
            String jsonKey = JsonUtil.mapToJson(segmentMap);
            predictionMap.put(jsonKey, forecastMap);
        }

        return predictionMap;
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

    public void setOptimizeField(OptimizeField optimizeField) {
        this.optimizeField = optimizeField;
    }

    public Map<String, Map<String, Double>> getPredictiveValues() {
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

package com.pubvantage.prediction;

import com.pubvantage.constant.MyConstant;
import com.pubvantage.dao.ReportViewDao;
import com.pubvantage.entity.*;
import com.pubvantage.entity.prediction.IdentifierPredictWrapper;
import com.pubvantage.entity.prediction.PredictDataWrapper;
import com.pubvantage.entity.prediction.SegmentPredictWrapper;
import com.pubvantage.service.*;
import com.pubvantage.utils.JsonUtil;
import org.apache.log4j.Logger;
import org.apache.spark.ml.regression.LinearRegressionModel;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LinearRegressionScoring {
    private static Logger logger = Logger.getLogger(LinearRegressionScoring.class.getName());
    private static final double PREDICTION_DEFAULT_VALUE = 0d;
    private CoreLearningModelServiceInterface coreLearnerModelService = new CoreLearningModelService();
    private ReportViewServiceInterface reportViewService = new ReportViewService();
    private ScoreServiceInterface scoreService = new ScoreService();
    private DataTrainingServiceInterface dataTrainingService = new DataTrainingService();
    private OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();
    private CoreOptimizationRule coreOptimizationRule;
    private Map<String, Map<String, Map<String, Boolean>>> noHistorySegment = new HashMap<>();

    public LinearRegressionScoring(CoreOptimizationRule coreOptimizationRule) {
        this.coreOptimizationRule = coreOptimizationRule;
    }

    /**
     * predict score then save to database
     */
    public void predict() throws Exception {
        PredictListData predictListData = coreLearnerModelService.getPredictData(coreOptimizationRule);

        Map<String, Map<String, Map<String, Map<String, Double>>>> segmentsPredict = generatePrediction(predictListData);
        Map<String, Map<String, Map<String, Map<String, Double>>>> predictTransform = transformStructure(
                segmentsPredict, predictListData.getListDate(), predictListData.getSegmentGroups());
        Map<String, Map<String, Map<String, Map<String, Double>>>> scoreData = computeScore(predictTransform, noHistorySegment);
        Map<String, Map<String, Map<String, Map<String, Double>>>> standardizedScore = standardizeScore(scoreData, noHistorySegment);

        saveScore(standardizedScore, this.coreOptimizationRule, predictListData.getFutureDate());
    }

    /**
     * @param predictListData Contains data need for prediction
     * @return predicted data
     */
    private Map<String, Map<String, Map<String, Map<String, Double>>>> generatePrediction(PredictListData predictListData) throws Exception {
        Map<String, Map<String, Map<String, Map<String, Double>>>> segmentsPredict = new HashMap<>();

        for (String segmentJson : predictListData.getSegmentGroupJson()) {
            SegmentPredictWrapper segmentPredictWrapper = generateSegmentGroupPredict(segmentJson, predictListData);
            segmentsPredict.put(segmentJson, segmentPredictWrapper.getDateAndIdentifierAndOptimizePredict());
            noHistorySegment.put(segmentJson, segmentPredictWrapper.getNoHistoryIdentifier());
        }

        return segmentsPredict;
    }


    /**
     * @param listDate list of dates
     * @param i        index
     * @return return true if i is last index -> i is index of future date in list date
     */
    private boolean isPredict(List<String> listDate, int i) {
        return listDate != null && i == listDate.size() - 1;
    }

    /**
     * @param jsonSegmentGroup Example {country: US, domain: a.com} in Json type
     * @param predictListData  data need for prediction  @return prediction data for one segment group
     */
    private SegmentPredictWrapper generateSegmentGroupPredict(String jsonSegmentGroup, PredictListData predictListData) throws Exception {
        List<String> listIdentifier = coreLearnerModelService.getDistinctIdentifiers(predictListData.getRuleId(), jsonSegmentGroup);
        predictListData.setIdentifiers(listIdentifier);

        Map<String, Map<String, Map<String, Double>>> dateIdentifierOptimizePredict = new HashMap<>();
        Map<String, Map<String, Boolean>> noHistoryIdentifier = new HashMap<>();

        for (String identifier : listIdentifier) {
            IdentifierPredictWrapper identifierPredictWrapper = generateIdentifierPredict(jsonSegmentGroup, identifier, predictListData);
            dateIdentifierOptimizePredict.put(identifier, identifierPredictWrapper.getDateOptimizePredict());
            noHistoryIdentifier.put(identifier, identifierPredictWrapper.getNoHistoryDate());
        }
        return new SegmentPredictWrapper(dateIdentifierOptimizePredict, noHistoryIdentifier);
    }

    /**
     * @param jsonSegmentGroup Example: {country: US, domain: a.com} in json type
     * @param identifier       identifier
     * @param predictListData  data need for prediction   @return prediction data for one identifier
     */
    private IdentifierPredictWrapper generateIdentifierPredict(String jsonSegmentGroup,
                                                               String identifier,
                                                               PredictListData predictListData) throws Exception {
        List<String> listDate = predictListData.getListDate();
        List<String> listOptimizeFieldJson = predictListData.getOptimizeFieldsJson();

        Map<String, Map<String, Double>> dateOptimizePredict = new LinkedHashMap<>();
        Map<String, Boolean> noHistoryDate = new LinkedHashMap<>();

        // loop by date
        for (int i = 0; i < listDate.size(); i++) {
            String date = listDate.get(i);
            Map<String, Double> optimizeFieldPredictValues = new LinkedHashMap<>();
            boolean isPredict = isPredict(listDate, i);
            //loop by optimize field
            for (String optimizeFieldJson : listOptimizeFieldJson) {
                PredictDataWrapper dataWrapper = new PredictDataWrapper(optimizeFieldJson, identifier, jsonSegmentGroup, date, isPredict, predictListData.getRuleId());
                Double predictValue = getPredictionValue(dataWrapper);

                optimizeFieldPredictValues.put(optimizeFieldJson, predictValue);

                if (MyConstant.NO_HISTORY_PREDICTION_VALUE == predictValue) {
                    noHistoryDate.put(date, true);
                }
            }
            dateOptimizePredict.put(date, optimizeFieldPredictValues);
        }
        return new IdentifierPredictWrapper(dateOptimizePredict, noHistoryDate);
    }

    private Double getPredictionValue(PredictDataWrapper predictDataWrapper) throws Exception {
        CoreLearner coreLearner = coreLearnerModelService.getOneCoreLeaner(
                predictDataWrapper.getOptimizeRuleId(),
                predictDataWrapper.getIdentifier(),
                predictDataWrapper.getOptimizeFieldJson(),
                predictDataWrapper.getSegmentJson());
        if (coreLearner == null) {
            throw new Exception("Missing Model. Optimize Rule Id: " + predictDataWrapper.getOptimizeRuleId() +
                    " Identifier: " + predictDataWrapper.getIdentifier() +
                    "Optimize field: " + predictDataWrapper.getOptimizeFieldJson());
        }

        if (!predictDataWrapper.getIsPredict()) {
            //no need to predict. get real optimization field value in data base
            return getObjectiveFromDatabase(coreLearner, predictDataWrapper, this.coreOptimizationRule);
        }

        VectorBuilder vectorBuilder = new VectorBuilder(coreLearner, this.coreOptimizationRule);
        org.apache.spark.ml.linalg.Vector conditionVector = vectorBuilder.buildVector();
        if (conditionVector == null) {
            return PREDICTION_DEFAULT_VALUE;
//            throw new Exception("Prediction fail: ConditionVector is null ");
        }
        double predict;
        try {
            LinearRegressionModel linearRegressionModel = LinearRegressionModel.load(coreLearner.getModelPath());
            predict = linearRegressionModel.predict(conditionVector);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new Exception("Cant find learning model in " + coreLearner.getModelPath());
        }

        if (Double.isNaN(predict)) {
            return PREDICTION_DEFAULT_VALUE;
        }
        return handleNegativePredictValue(predict);
    }

    private Double getObjectiveFromDatabase(CoreLearner coreLearner,
                                            PredictDataWrapper predictDataWrapper,
                                            CoreOptimizationRule optimizationRule) {

        OptimizeField optimizeField = JsonUtil.jsonToObject(coreLearner.getOptimizeFields(), OptimizeField.class);
        CoreReportView reportView = reportViewService.findById(optimizationRule.getReportViewId(), new ReportViewDao());
        List<String> digitMetrics = reportViewService.getNoSpaceDigitMetrics(reportView, optimizeField.getField());
        List<String> dimensions = optimizationRuleService.getNoSpaceDimensions(optimizationRule);
        dimensions.remove(optimizationRule.getDateField());
        Double value = dataTrainingService.getRealObjectiveValueFromDB(predictDataWrapper, digitMetrics, dimensions, optimizationRule);
        return value == null ? MyConstant.NO_HISTORY_PREDICTION_VALUE : value;
    }

    /**
     * @param predict predict vale
     * @return 0 if predict value ids negative
     */
    private double handleNegativePredictValue(Double predict) {
        // TODO which is best value that doesn't have bad effect to score
        return predict < 0 ? 0 : predict;
    }


    /**
     * Avoid to small score.
     *
     * @param scoreData        scored data
     * @param noHistorySegment some date has no data in database depend on segment group and identifier
     * @return standardized Score. each score divide max value
     */
    private Map<String, Map<String, Map<String, Map<String, Double>>>>
    standardizeScore(Map<String, Map<String, Map<String, Map<String, Double>>>> scoreData,
                     Map<String, Map<String, Map<String, Boolean>>> noHistorySegment) {

        for (Map.Entry<String, Map<String, Map<String, Map<String, Double>>>> dateEntry : scoreData.entrySet()) {
            String date = dateEntry.getKey();
            Map<String, Map<String, Map<String, Double>>> dateMap = dateEntry.getValue();

            for (Map.Entry<String, Map<String, Map<String, Double>>> segmentEntry : dateMap.entrySet()) {
                String segment = segmentEntry.getKey();
                Map<String, Map<String, Double>> segmentMap = segmentEntry.getValue();
                double maxScore = getMaxScore(segmentMap, noHistorySegment.get(segment), date);
                divideScoreByMaxValue(maxScore, segmentMap, noHistorySegment.get(segment), date);
            }
        }
        return scoreData;
    }

    /**
     * @param maxScore         max score of identifiers
     * @param segmentMap       score data
     * @param noHistorySegment some date has no data in database  depend on segment group and identifier
     * @param date             current date
     */
    private void divideScoreByMaxValue(double maxScore, Map<String, Map<String, Double>> segmentMap,
                                       Map<String, Map<String, Boolean>> noHistorySegment, String date) {
        for (Map.Entry<String, Map<String, Double>> entry : segmentMap.entrySet()) {
            String identifier = entry.getKey();
            Map<String, Boolean> noHistoryDate = noHistorySegment.get(identifier);
            Boolean noHistory = noHistoryDate.get(date);
            if (noHistory != null && noHistory) continue;

            Map<String, Double> identifierMap = entry.getValue();
            double score = identifierMap.get(MyConstant.SCORE);
            score = maxScore == 0 ? 0 : score / maxScore;
            identifierMap.put(MyConstant.SCORE, score);
        }
    }

    /**
     * @param segmentMap       score data
     * @param noHistorySegment some date has no data in database  depend on segment group and identifier
     * @param date             date
     * @return max score in score data of date
     */
    private double getMaxScore(Map<String, Map<String, Double>> segmentMap,
                               Map<String, Map<String, Boolean>> noHistorySegment, String date) {
        double max = -Double.MAX_VALUE;
        for (Map.Entry<String, Map<String, Double>> entry : segmentMap.entrySet()) {
            String identifier = entry.getKey();
            Map<String, Boolean> noHistoryDate = noHistorySegment.get(identifier);
            Boolean noHistory = noHistoryDate.get(date);
            if (noHistory != null && noHistory) continue;

            Map<String, Double> identifierMap = entry.getValue();
            double score = identifierMap.get(MyConstant.SCORE);
            if (max < score)
                max = score;
        }
        return max;
    }


    /**
     * @param predictTransform predict data
     * @param noHistorySegment some date has no data in database  depend on segment group and identifier
     * @return score data
     */
    private Map<String, Map<String, Map<String, Map<String, Double>>>>
    computeScore(Map<String, Map<String, Map<String, Map<String, Double>>>> predictTransform,
                 Map<String, Map<String, Map<String, Boolean>>> noHistorySegment) {

        // TODO Need to refactor
        for (Map.Entry<String, Map<String, Map<String, Map<String, Double>>>> dateEntry : predictTransform.entrySet()) {
            String date = dateEntry.getKey();
            Map<String, Map<String, Map<String, Double>>> dateMap = dateEntry.getValue();
            for (Map.Entry<String, Map<String, Map<String, Double>>> segmentEntry : dateMap.entrySet()) {
                String segment = segmentEntry.getKey();
                Map<String, Map<String, Double>> segmentMap = segmentEntry.getValue();

                Map<String, Double> totalMap = getTotal(segmentMap);
                Map<String, Map<String, Double>> avgMap = getAvg(segmentMap, totalMap);

                Map<String, Double> maxAvgNegativeOptimize = getMaxAvgForNegativeOptimizeField(avgMap);
                Map<String, Map<String, Double>> divideMaxByEachAvgValue = divideMaxByEachAvgValue(avgMap, maxAvgNegativeOptimize);
                Map<String, Double> invertedDivideMaxByEachAvgValueTotal = getDivideMaxByEachAvgValueTotal(divideMaxByEachAvgValue);

                Map<String, Map<String, Double>> invertedAvgNegative = getInvertedAvgNegative(invertedDivideMaxByEachAvgValueTotal, divideMaxByEachAvgValue);
                Map<String, Double> scoreMap = multiOptimizeFieldWeight(invertedAvgNegative);

                Map<String, Map<String, Boolean>> noHistoryIdentifier = noHistorySegment.get(segment);

                for (Map.Entry<String, Map<String, Double>> identifierEntry : segmentMap.entrySet()) {
                    String identifier = identifierEntry.getKey();
                    Map<String, Double> optimizeMap = identifierEntry.getValue();
                    Map<String, Boolean> noHistoryDate = noHistoryIdentifier.get(identifier);
                    if (noHistoryDate.get(date) != null && noHistoryDate.get(date)) {
                        optimizeMap.put(MyConstant.SCORE, getScoreForNoHistoryData());
                        continue;
                    }
                    optimizeMap.put(MyConstant.SCORE, scoreMap.get(identifier));
                }
            }
        }
        return predictTransform;
    }

    /**
     * @return default predict value if there is no data correspond date, segment, identifier in data base
     */
    private Double getScoreForNoHistoryData() {
        return MyConstant.DEFAULT_SCORE_VALUE;
    }

    /**
     * @param invertedDivideMaxByEachAvgValueTotal total avg data
     * @param divideMaxByEachAvgValue              divideMaxByEachAvgValue
     * @return avg data of identifier if optimize goal is Min
     */
    private Map<String, Map<String, Double>> getInvertedAvgNegative
    (Map<String, Double> invertedDivideMaxByEachAvgValueTotal,
     Map<String, Map<String, Double>> divideMaxByEachAvgValue) {
        Map<String, Map<String, Double>> avgMap = new HashMap<>();

        for (Map.Entry<String, Map<String, Double>> identifierEntry : divideMaxByEachAvgValue.entrySet()) {
            String identifier = identifierEntry.getKey();
            Map<String, Double> optimizeAvgMap = identifierEntry.getValue();
            Map<String, Double> avgOptimize = new HashMap<>();
            for (Map.Entry<String, Double> optimizeEntry : optimizeAvgMap.entrySet()) {
                String optimize = optimizeEntry.getKey();
                OptimizeField optimizeField = JsonUtil.jsonToObject(optimize, OptimizeField.class);
                String goal = optimizeField.getGoal();
                Double avg = optimizeEntry.getValue();
                avgOptimize.put(optimize, avg);

                if (MyConstant.NO_HISTORY_PREDICTION_VALUE == avg) continue;

                if (MyConstant.MIN.equals(goal)) {
                    double total = invertedDivideMaxByEachAvgValueTotal.get(optimize);
                    double newAvg = total == 0 ? 0 : avg / total;
                    avgOptimize.put(optimize, newAvg);
                }
            }
            avgMap.put(identifier, avgOptimize);
        }
        return avgMap;
    }

    /**
     * @param divideMaxByEachAvgValue data after divide max value by each value of identifier
     * @return total data of each identifier
     */
    private Map<String, Double> getDivideMaxByEachAvgValueTotal(Map<String, Map<String, Double>> divideMaxByEachAvgValue) {
        Map<String, Double> totalMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> identifierEntry : divideMaxByEachAvgValue.entrySet()) {
            Map<String, Double> invertedOptimizeAvgMap = identifierEntry.getValue();
            for (Map.Entry<String, Double> optimizeEntry : invertedOptimizeAvgMap.entrySet()) {
                String optimize = optimizeEntry.getKey();
                OptimizeField optimizeField = JsonUtil.jsonToObject(optimize, OptimizeField.class);
                String goal = optimizeField.getGoal();
                Double value = optimizeEntry.getValue();
                if (MyConstant.NO_HISTORY_PREDICTION_VALUE == value) continue;

                if (MyConstant.MIN.equals(goal)) {
                    if (totalMap.get(optimize) == null) {
                        totalMap.put(optimize, value);
                    } else {
                        double newValue = totalMap.get(optimize) + value;
                        totalMap.put(optimize, newValue);
                    }
                }
            }
        }
        return totalMap;
    }

    /**
     * @param avgMap                 avg data
     * @param maxAvgNegativeOptimize max avg value of identifier
     * @return divide each value by max avg value
     */

    private Map<String, Map<String, Double>> divideMaxByEachAvgValue(Map<String, Map<String, Double>> avgMap,
                                                                     Map<String, Double> maxAvgNegativeOptimize) {
        Map<String, Map<String, Double>> invertMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> identifierEntry : avgMap.entrySet()) {
            String identifier = identifierEntry.getKey();
            Map<String, Double> optimizeAvgMap = identifierEntry.getValue();
            Map<String, Double> invertedOptimizeMap = new HashMap<>();

            for (Map.Entry<String, Double> optimizeEntry : optimizeAvgMap.entrySet()) {
                String optimize = optimizeEntry.getKey();
                OptimizeField optimizeField = JsonUtil.jsonToObject(optimize, OptimizeField.class);
                String goal = optimizeField.getGoal();
                Double avg = optimizeEntry.getValue();
                if (MyConstant.NO_HISTORY_PREDICTION_VALUE == avg) continue;

                invertedOptimizeMap.put(optimize, avg);
                if (MyConstant.MIN.equals(goal)) {
                    double maxAvg = maxAvgNegativeOptimize.get(optimize);
                    double invertedValue = avg == 0 ? 0 : maxAvg / avg;
                    invertedOptimizeMap.put(optimize, invertedValue);
                }
            }
            invertMap.put(identifier, invertedOptimizeMap);
        }
        return invertMap;
    }

    /**
     * @param avgMap avg data
     * @return max value of each group identifiers in a segment
     */
    private Map<String, Double> getMaxAvgForNegativeOptimizeField(Map<String, Map<String, Double>> avgMap) {
        Map<String, Double> maxAvgMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> identifierEntry : avgMap.entrySet()) {
            Map<String, Double> optimizeAvgMap = identifierEntry.getValue();
            for (Map.Entry<String, Double> optimizeEntry : optimizeAvgMap.entrySet()) {
                String optimize = optimizeEntry.getKey();
                OptimizeField optimizeField = JsonUtil.jsonToObject(optimize, OptimizeField.class);
                String goal = optimizeField.getGoal();
                Double avg = optimizeEntry.getValue();
                if (MyConstant.NO_HISTORY_PREDICTION_VALUE == avg) continue;
                if (MyConstant.MIN.equals(goal)) {
                    Double maxAvg = maxAvgMap.get(optimize);
                    if (maxAvg == null) {
                        maxAvgMap.put(optimize, avg);
                    } else {
                        if (maxAvg < avg) {
                            maxAvgMap.put(optimize, avg);
                        }
                    }
                }
            }
        }
        return maxAvgMap;
    }

    /**
     * @param avgMap avg data
     * @return score
     */
    private Map<String, Double> multiOptimizeFieldWeight(Map<String, Map<String, Double>> avgMap) {
        Map<String, Double> scoreMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> identifierEntry : avgMap.entrySet()) {
            String identifier = identifierEntry.getKey();
            Double score = 0d;
            Map<String, Double> optimizeMap = identifierEntry.getValue();
            for (Map.Entry<String, Double> optimizeEntry : optimizeMap.entrySet()) {
                String optimize = optimizeEntry.getKey();
                OptimizeField optimizeField = JsonUtil.jsonToObject(optimize, OptimizeField.class);
                Double avg = optimizeEntry.getValue();
                if (MyConstant.NO_HISTORY_PREDICTION_VALUE == avg) continue;
                Double weight = optimizeField.getWeight();
                score += weight * avg;
            }
            scoreMap.put(identifier, score);
        }
        return scoreMap;
    }

    /**
     * @param segmentMap predict data
     * @return total data
     */
    private Map<String, Double> getTotal(Map<String, Map<String, Double>> segmentMap) {
        Map<String, Double> totalMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> identifierEntry : segmentMap.entrySet()) {
            Map<String, Double> optimizeMap = identifierEntry.getValue();
            for (Map.Entry<String, Double> optimizeEntry : optimizeMap.entrySet()) {
                String optimize = optimizeEntry.getKey();
                Double predictValue = optimizeEntry.getValue();
                if (MyConstant.NO_HISTORY_PREDICTION_VALUE == predictValue) continue;
                if (totalMap.get(optimize) == null) {
                    totalMap.put(optimize, predictValue);
                } else {
                    double original = totalMap.get(optimize);
                    totalMap.put(optimize, original + predictValue);
                }
            }
        }

        return totalMap;
    }

    /**
     * @param segmentMap segment data
     * @param totalMap   total data
     * @return average score
     */
    private Map<String, Map<String, Double>> getAvg(Map<String, Map<String, Double>> segmentMap, Map<String, Double> totalMap) {
        Map<String, Map<String, Double>> identifierAvgMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> identifierEntry : segmentMap.entrySet()) {
            String identifier = identifierEntry.getKey();
            Map<String, Double> optimizeMap = identifierEntry.getValue();
            Map<String, Double> avgMap = new HashMap<>();
            for (Map.Entry<String, Double> optimizeEntry : optimizeMap.entrySet()) {
                String optimize = optimizeEntry.getKey();
                Double predictValue = optimizeEntry.getValue();
                if (MyConstant.NO_HISTORY_PREDICTION_VALUE == predictValue) continue;
                Double avg = totalMap.get(optimize) == 0 ? 0 : predictValue / totalMap.get(optimize);
                avgMap.put(optimize, avg);
            }
            identifierAvgMap.put(identifier, avgMap);
        }

        return identifierAvgMap;
    }

    /**
     * @param segmentsPredict predict data
     * @param listDate        list of dates
     * @param listSegments    list of segments
     * @return date to out position
     */
    private Map<String, Map<String, Map<String, Map<String, Double>>>> transformStructure(
            Map<String, Map<String, Map<String, Map<String, Double>>>> segmentsPredict,
            List<String> listDate,
            List<Map<String, String>> listSegments) {

        Map<String, Map<String, Map<String, Map<String, Double>>>> datePredict = new LinkedHashMap<>();

        for (String date : listDate) {
            Map<String, Map<String, Map<String, Double>>> segmentPredict = new LinkedHashMap<>();
            for (Map<String, String> segment : listSegments) {
                String keySegment = segment == null ? MyConstant.GLOBAL_KEY : JsonUtil.mapToJson(segment);
                Map<String, Map<String, Map<String, Double>>> mapSegment = segmentsPredict.get(keySegment);
                Map<String, Map<String, Double>> identifierPredict = new HashMap<>();
                for (Map.Entry<String, Map<String, Map<String, Double>>> entry2 : mapSegment.entrySet()) {
                    String identifier = entry2.getKey();
                    Map<String, Map<String, Double>> dateAndOptimizePredict = entry2.getValue();
                    Map<String, Double> optimizePredict = dateAndOptimizePredict.get(date);
                    identifierPredict.put(identifier, optimizePredict);
                }
                segmentPredict.put(keySegment, identifierPredict);
            }
            datePredict.put(date, segmentPredict);
        }

        return datePredict;
    }


    private void saveScore(Map<String, Map<String, Map<String, Map<String, Double>>>> scoreMap,
                           CoreOptimizationRule optimizationRule, String futureDate) {
        this.scoreService.saveScore(scoreMap, optimizationRule, futureDate);
    }


}

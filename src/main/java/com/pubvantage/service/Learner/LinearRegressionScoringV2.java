package com.pubvantage.service.Learner;

import com.pubvantage.AppMain;
import com.pubvantage.conditionprocessor.ConditionConverter;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.entity.*;
import com.pubvantage.service.CoreLearningModelService;
import com.pubvantage.service.CoreLearningModelServiceInterface;
import com.pubvantage.service.DataTraning.DataTrainingService;
import com.pubvantage.service.DataTraning.DataTrainingServiceInterface;
import com.pubvantage.service.score.ScoreService;
import com.pubvantage.service.score.ScoreServiceInterface;
import com.pubvantage.utils.ConfigLoaderUtil;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.JsonUtil;
import com.pubvantage.utils.ThreadUtil;
import org.apache.log4j.Logger;
import org.apache.spark.ml.regression.LinearRegressionModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LinearRegressionScoringV2 {
    private static Logger logger = Logger.getLogger(AppMain.class.getName());
    private static final double PREDICTION_DEFAULT_VALUE = 0d;
    private CoreLearningModelServiceInterface coreLearnerModelService = new CoreLearningModelService();
    private ScoreServiceInterface scoreService = new ScoreService();
    private DataTrainingServiceInterface dataTrainingService = new DataTrainingService();

    private CoreOptimizationRule coreOptimizationRule;
    private List<String> listDate;
    private List<Object> listSegments;
    private String futureDate;
    private Map<String, Map<String, Map<String, Boolean>>> noHistorySegment = new ConcurrentHashMap<>();

    public LinearRegressionScoringV2(CoreOptimizationRule coreOptimizationRule,
                                     List<String> listDate) {
        this.coreOptimizationRule = coreOptimizationRule;
        this.listDate = listDate;
    }


    public Map<String, Map<String, Map<String, Map<String, Double>>>> predict() {
        addFutureDate(this.listDate);

        ExecutorService executorService = Executors.newFixedThreadPool(ConfigLoaderUtil.getExecuteServiceThreadLeaner());
        Map<String, Map<String, Map<String, Map<String, Double>>>> segmentsPredict = generatePredictValue(
                executorService, this.listDate);
        if (executorService.isShutdown()) {
            Map<String, Map<String, Map<String, Map<String, Double>>>> predictTransform = transformStructure(
                    segmentsPredict, this.listDate, this.listSegments);
            Map<String, Map<String, Map<String, Map<String, Double>>>> scoreData = computeScore(
                    predictTransform, noHistorySegment);
            Map<String, Map<String, Map<String, Map<String, Double>>>> standardizedScore = standardizeScore(
                    scoreData, noHistorySegment);

            saveScore(standardizedScore, this.coreOptimizationRule, futureDate);
        }
        return null;
    }

    private Map<String, Map<String, Map<String, Map<String, Double>>>>
    standardizeScore(Map<String, Map<String, Map<String, Map<String, Double>>>> scoreTransform,
                     Map<String, Map<String, Map<String, Boolean>>> noHistorySegment) {
        for (Map.Entry<String, Map<String, Map<String, Map<String, Double>>>> dateEntry : scoreTransform.entrySet()) {
            String date = dateEntry.getKey();
            Map<String, Map<String, Map<String, Double>>> dateMap = dateEntry.getValue();
            for (Map.Entry<String, Map<String, Map<String, Double>>> segmentEntry : dateMap.entrySet()) {
                String segment = segmentEntry.getKey();
                Map<String, Map<String, Double>> segmentMap = segmentEntry.getValue();
                double maxScore = getMaxScore(segmentMap, noHistorySegment.get(segment), date);
                transformScore(maxScore, segmentMap, noHistorySegment.get(segment), date);
            }
        }
        return scoreTransform;
    }

    private void transformScore(double maxScore, Map<String, Map<String, Double>> segmentMap,
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

    private void addFutureDate(List<String> listDate) {
        try {
            if (listDate == null || listDate.isEmpty()) {
                Date today = new Date();
                Date nextDay = ConvertUtil.nextDay(today);
                String nextDayString = ConvertUtil.dateToString(nextDay, MyConstant.DATE_FORMAT_JAVA);
                listDate = new ArrayList<>();
                listDate.add(nextDayString);
                this.futureDate = nextDayString;
                return;
            }
            java.util.Collections.sort(listDate);
            String latestDate = listDate.get(listDate.size() - 1);
            Date date1 = new SimpleDateFormat(MyConstant.DATE_FORMAT_JAVA).parse(latestDate);
            Date nextDay = ConvertUtil.nextDay(date1);
            String nextDayString = ConvertUtil.dateToString(nextDay, MyConstant.DATE_FORMAT_JAVA);
            listDate.add(nextDayString);
            this.futureDate = nextDayString;
        } catch (ParseException e) {
            e.printStackTrace();
            Date today = new Date();
            Date nextDay = ConvertUtil.nextDay(today);
            String nextDayString = ConvertUtil.dateToString(nextDay, MyConstant.DATE_FORMAT_JAVA);
            listDate.add(nextDayString);
            this.futureDate = nextDayString;
        }
    }

    private Map<String, Map<String, Map<String, Map<String, Double>>>>
    computeScore(Map<String, Map<String, Map<String, Map<String, Double>>>> predictTransform,
                 Map<String, Map<String, Map<String, Boolean>>> noHistorySegment) {

        for (Map.Entry<String, Map<String, Map<String, Map<String, Double>>>> dateEntry : predictTransform.entrySet()) {
            String date = dateEntry.getKey();
            Map<String, Map<String, Map<String, Double>>> dateMap = dateEntry.getValue();
            for (Map.Entry<String, Map<String, Map<String, Double>>> segmentEntry : dateMap.entrySet()) {
                String segment = segmentEntry.getKey();
                Map<String, Map<String, Double>> segmentMap = segmentEntry.getValue();

                Map<String, Double> totalMap = getTotal(segmentMap);
                Map<String, Map<String, Double>> avgMap = getAvg(segmentMap, totalMap);
                Map<String, Double> maxAvgNegativeOptimize = getMaxAvgForNegativeOptimizeField(avgMap);
                Map<String, Map<String, Double>> invertMapNegative_1 = getInvertMapNegativeOptimizeField_1(avgMap, maxAvgNegativeOptimize);
                Map<String, Double> invertedTotalNegative = getInvertedTotal(invertMapNegative_1);
                Map<String, Map<String, Double>> invertedAvgNegative_2 = getInvertedAvgNegative_2(invertedTotalNegative, invertMapNegative_1);
                Map<String, Double> scoreMap = getScore(invertedAvgNegative_2);

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

    private Double getScoreForNoHistoryData() {
        return MyConstant.DEFAULT_SCORE_VALUE;
    }

    private Map<String, Map<String, Double>> getInvertedAvgNegative_2
            (Map<String, Double> invertedTotalMap,
             Map<String, Map<String, Double>> invertMapNegative_1) {
        Map<String, Map<String, Double>> avgMap = new HashMap<>();

        for (Map.Entry<String, Map<String, Double>> identifierEntry : invertMapNegative_1.entrySet()) {
            String identifier = identifierEntry.getKey();
            Map<String, Double> optimizeAvgMap = identifierEntry.getValue();
            Map<String, Double> avgOptimize = new HashMap<>();
            for (Map.Entry<String, Double> optimizeEntry : optimizeAvgMap.entrySet()) {
                String optimize = optimizeEntry.getKey();
                OptimizeField optimizeField = JsonUtil.jsonToObject(optimize, OptimizeField.class);
                String goal = optimizeField.getGoal();
                Double avg = optimizeEntry.getValue();
                avgOptimize.put(optimize, avg);

                if (MyConstant.NULL_PREDICT_VALUE == avg) continue;

                if (MyConstant.MIN.equals(goal)) {
                    double total = invertedTotalMap.get(optimize);
                    double newAvg = total == 0 ? 0 : avg / total;
                    avgOptimize.put(optimize, newAvg);
                }
            }
            avgMap.put(identifier, avgOptimize);
        }
        return avgMap;
    }

    private Map<String, Double> getInvertedTotal(Map<String, Map<String, Double>> getInvertMapNegativeOptimizeField) {
        Map<String, Double> totalMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> identifierEntry : getInvertMapNegativeOptimizeField.entrySet()) {
            Map<String, Double> invertedOptimizeAvgMap = identifierEntry.getValue();
            for (Map.Entry<String, Double> optimizeEntry : invertedOptimizeAvgMap.entrySet()) {
                String optimize = optimizeEntry.getKey();
                OptimizeField optimizeField = JsonUtil.jsonToObject(optimize, OptimizeField.class);
                String goal = optimizeField.getGoal();
                Double value = optimizeEntry.getValue();
                if (MyConstant.NULL_PREDICT_VALUE == value) continue;

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

    private Map<String, Map<String, Double>> getInvertMapNegativeOptimizeField_1(Map<String, Map<String, Double>> avgMap, Map<String, Double> maxAvgForNegativeOptimizeField) {
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
                if (MyConstant.NULL_PREDICT_VALUE == avg) continue;

                invertedOptimizeMap.put(optimize, avg);
                if (MyConstant.MIN.equals(goal)) {
                    double maxAvg = maxAvgForNegativeOptimizeField.get(optimize);
                    double invertedValue = avg == 0 ? 0 : maxAvg / avg;
                    invertedOptimizeMap.put(optimize, invertedValue);
                }
            }
            invertMap.put(identifier, invertedOptimizeMap);
        }
        return invertMap;
    }

    private Map<String, Double> getMaxAvgForNegativeOptimizeField(Map<String, Map<String, Double>> avgMap) {
        Map<String, Double> maxAvgMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> identifierEntry : avgMap.entrySet()) {
            Map<String, Double> optimizeAvgMap = identifierEntry.getValue();
            for (Map.Entry<String, Double> optimizeEntry : optimizeAvgMap.entrySet()) {
                String optimize = optimizeEntry.getKey();
                OptimizeField optimizeField = JsonUtil.jsonToObject(optimize, OptimizeField.class);
                String goal = optimizeField.getGoal();
                Double avg = optimizeEntry.getValue();
                if (MyConstant.NULL_PREDICT_VALUE == avg) continue;
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

    private Map<String, Double> getScore(Map<String, Map<String, Double>> avgMap) {
        Map<String, Double> scoreMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> identifierEntry : avgMap.entrySet()) {
            String identifier = identifierEntry.getKey();
            Double score = 0d;
            Map<String, Double> optimizeMap = identifierEntry.getValue();
            for (Map.Entry<String, Double> optimizeEntry : optimizeMap.entrySet()) {
                String optimize = optimizeEntry.getKey();
                OptimizeField optimizeField = JsonUtil.jsonToObject(optimize, OptimizeField.class);
                Double avg = optimizeEntry.getValue();
                if (MyConstant.NULL_PREDICT_VALUE == avg) continue;
                Double weight = optimizeField.getWeight();
                score += weight * avg;
            }
            scoreMap.put(identifier, score);
        }
        return scoreMap;
    }

    private Map<String, Double> getTotal(Map<String, Map<String, Double>> segmentMap) {
        Map<String, Double> totalMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> identifierEntry : segmentMap.entrySet()) {
            Map<String, Double> optimizeMap = identifierEntry.getValue();
            for (Map.Entry<String, Double> optimizeEntry : optimizeMap.entrySet()) {
                String optimize = optimizeEntry.getKey();
                Double predictValue = optimizeEntry.getValue();
                if (MyConstant.NULL_PREDICT_VALUE == predictValue) continue;
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

    private Map<String, Map<String, Double>> getAvg(Map<String, Map<String, Double>> segmentMap, Map<String, Double> totalMap) {
        Map<String, Map<String, Double>> identifierAvgMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> identifierEntry : segmentMap.entrySet()) {
            String identifier = identifierEntry.getKey();
            Map<String, Double> optimizeMap = identifierEntry.getValue();
            Map<String, Double> avgMap = new HashMap<>();
            for (Map.Entry<String, Double> optimizeEntry : optimizeMap.entrySet()) {
                String optimize = optimizeEntry.getKey();
                Double predictValue = optimizeEntry.getValue();
                if (MyConstant.NULL_PREDICT_VALUE == predictValue) continue;
                Double avg = totalMap.get(optimize) == 0 ? 0 : predictValue / totalMap.get(optimize);
                avgMap.put(optimize, avg);
            }
            identifierAvgMap.put(identifier, avgMap);
        }

        return identifierAvgMap;
    }

    private Map<String, Map<String, Map<String, Map<String, Double>>>> transformStructure(
            Map<String, Map<String, Map<String, Map<String, Double>>>> segmentsPredict,
            List<String> listDate,
            List<Object> listSegments) {

        Map<String, Map<String, Map<String, Map<String, Double>>>> datePredict = new LinkedHashMap<>();

        for (String date : listDate) {
            Map<String, Map<String, Map<String, Double>>> segmentPredict = new LinkedHashMap<>();
            for (Object segment : listSegments) {
                String keySegment = segment == null ? MyConstant.GLOBAL_KEY : segment.toString();
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

    private Map<String, Map<String, Map<String, Map<String, Double>>>>
    generatePredictValue(ExecutorService executorService, List<String> listDate) {
        Long ruleId = this.coreOptimizationRule.getId();
        this.listSegments = coreLearnerModelService.getDistinctSegmentsByRuleId(ruleId);
        Map<String, Map<String, Map<String, Map<String, Double>>>> segmentsPredict = new ConcurrentHashMap<>();
        if (this.listSegments == null)
            return new ConcurrentHashMap<>();
        for (Object segment : listSegments) {
            Map<String, Object> segmentMap = null;
            if (segment != null) {
                segmentMap = JsonUtil.jsonToMap(segment.toString());
            }
            Map<String, Object> finalSegmentMap = segmentMap;

//            executorService.execute(() -> {
            logger.error("executorService execute");
            List<String> listIdentifier = coreLearnerModelService.getDistinctIdentifiersBySegment(finalSegmentMap, ruleId);
            Map<String, Map<String, Map<String, Double>>> dateAndIdentifierAndOptimizePredict = new LinkedHashMap<>();
            Map<String, Map<String, Boolean>> noHistoryIdentifier = new LinkedHashMap<>();

            for (String identifier : listIdentifier) {
                List<OptimizeField> listOptimizeField = coreLearnerModelService.getDistinctOptimizeBySegmentAndIdentifier(finalSegmentMap, identifier, ruleId);
                Map<String, Map<String, Double>> dateAndOptimizePredict = new LinkedHashMap<>();
                Map<String, Boolean> noHistoryDate = new LinkedHashMap<>();

                for (int i = 0; i < listDate.size(); i++) {
                    String date = this.listDate.get(i);
                    Map<String, Double> optimizeFieldPredictValues = new LinkedHashMap<>();
                    boolean isPredict = false;
                    if (i == listDate.size() - 1) {
                        isPredict = true;
                    }
                    for (OptimizeField optimizeField : listOptimizeField) {
                        Double predictValue = computePredict(optimizeField, identifier, finalSegmentMap, date, isPredict);
                        optimizeFieldPredictValues.put(JsonUtil.toJson(optimizeField), predictValue);
                        if (MyConstant.NULL_PREDICT_VALUE == predictValue) {
                            noHistoryDate.put(date, true);
                        }
                    }
                    dateAndOptimizePredict.put(date, optimizeFieldPredictValues);
                }
                dateAndIdentifierAndOptimizePredict.put(identifier, dateAndOptimizePredict);
                noHistoryIdentifier.put(identifier, noHistoryDate);
            }
            String key = segment == null ? MyConstant.GLOBAL_KEY : segment.toString();
            segmentsPredict.put(key, dateAndIdentifierAndOptimizePredict);
            noHistorySegment.put(key, noHistoryIdentifier);
//            });

        }
        logger.error("executorService awaitTerminationAfterShutdown");
        ThreadUtil.awaitTerminationAfterShutdown(executorService);
        return segmentsPredict;
    }

    private void saveScore(Map<String, Map<String, Map<String, Map<String, Double>>>> scoreMap,
                           CoreOptimizationRule optimizationRule, String futureDate) {
        this.scoreService.saveScore(scoreMap, optimizationRule, futureDate);
    }

    private Double computePredict(OptimizeField optimizeField, String identifier, Map<String, Object> segment, String date, boolean isPredict) {
        Long ruleId = this.coreOptimizationRule.getId();
        CoreLearner coreLearner = coreLearnerModelService.getOneCoreLeaner(ruleId, identifier, optimizeField, segment);
        if (coreLearner == null || coreLearner.getId() == null || coreLearner.getOptimizationRuleId() == null) {
            return 0D;
        }

        if (!isPredict) {
            //no need to predict
            return getObjectiveFromDatabase(coreLearner, optimizeField, identifier, segment, date, this.coreOptimizationRule);
        }

        FactorValues factorValues = new FactorValues();
        factorValues.setIsPredictive(true);
        ConditionConverter conditionConverter = new ConditionConverter(identifier, factorValues, coreLearner,
                this.coreOptimizationRule, date, isPredict);
        org.apache.spark.ml.linalg.Vector conditionVector = conditionConverter.buildVectorV2();
        if (conditionVector == null) {
            return PREDICTION_DEFAULT_VALUE;
        }
        double predict = 0d;
        try {
            LinearRegressionModel linearRegressionModel = LinearRegressionModel.load(coreLearner.getModelPath());
            predict = linearRegressionModel.predict(conditionVector);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (Double.isNaN(predict)) {
            return PREDICTION_DEFAULT_VALUE;
        }
        return getFinalPredict(predict);
    }

    private Double getObjectiveFromDatabase(CoreLearner coreLearner, OptimizeField optimizeField,
                                            String identifier,
                                            Map<String, Object> segment,
                                            String date,
                                            CoreOptimizationRule coreOptimizationRule) {
        List<String> metrics = coreLearnerModelService.getMetricsFromCoreLeaner(coreLearner);
        Double value = dataTrainingService.getObjectiveFromDB(identifier, segment, metrics, optimizeField, coreOptimizationRule, date);
        return value == null ? MyConstant.NULL_PREDICT_VALUE : value;
    }

    private double getFinalPredict(Double predict) {
        return predict < 0 ? 0 : predict;
    }
}

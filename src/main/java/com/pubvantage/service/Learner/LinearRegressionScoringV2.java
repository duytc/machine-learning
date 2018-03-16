package com.pubvantage.service.Learner;

import com.pubvantage.AppMain;
import com.pubvantage.ConditionProcessor.ConditionConverter;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.FactorValues;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.service.CoreLearningModelService;
import com.pubvantage.service.CoreLearningModelServiceInterface;
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
import java.util.concurrent.TimeUnit;

public class LinearRegressionScoringV2 {
    private static Logger logger = Logger.getLogger(AppMain.class.getName());
    private static final String GLOBAL_KEY = "NULL";
    private static final double PREDICTION_DEFAULT_VALUE = 0d;
    private CoreLearningModelServiceInterface coreLearnerModelService = new CoreLearningModelService();
    private ScoreServiceInterface scoreService = new ScoreService();

    private CoreOptimizationRule coreOptimizationRule;
    private List<String> listDate;
    private List<CoreLearner> coreLearnerList;
    List<Object> listSegments;
    private String futureDate;
    private ExecutorService executorService = Executors.newFixedThreadPool(ConfigLoaderUtil.getExecuteServiceThreadScore());

    public LinearRegressionScoringV2(CoreOptimizationRule coreOptimizationRule,
                                     List<String> listDate,
                                     List<CoreLearner> coreLearnerList) {
        this.coreOptimizationRule = coreOptimizationRule;
        this.listDate = listDate;
        this.coreLearnerList = coreLearnerList;
    }


    public Map<String, Map<String, Map<String, Map<String, Double>>>> predict() {

        addFutureDate(this.listDate);

        Map<String, Map<String, Map<String, Map<String, Double>>>> segmentsPredict = generatePredictValue();
        if (executorService.isShutdown()) {
            logger.error("executorService isShutdown");
            Map<String, Map<String, Map<String, Map<String, Double>>>> predictTransform = transform2(segmentsPredict, listDate, this.listSegments);
            Map<String, Map<String, Map<String, Map<String, Double>>>> scoreTransform = computeScore(predictTransform);
            saveScore(scoreTransform, this.coreOptimizationRule, futureDate);
        }
        return null;
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

    private Map<String, Map<String, Map<String, Map<String, Double>>>> computeScore(Map<String, Map<String, Map<String, Map<String, Double>>>> predictTransform) {
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

                for (Map.Entry<String, Map<String, Double>> identifierEntry : segmentMap.entrySet()) {
                    String identifier = identifierEntry.getKey();
                    Map<String, Double> optimizeMap = identifierEntry.getValue();
                    optimizeMap.put(MyConstant.SCORE, scoreMap.get(identifier));
                }
            }
        }
        return predictTransform;
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
                double goalValue = 1;
//                double goalValue = MyConstant.MAX.equals(optimizeField.getGoal()) ? 1 : -1;

                Double weight = optimizeField.getWeight();
                score += goalValue * weight * avg;
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
                Double avg = totalMap.get(optimize) == 0 ? 0 : predictValue / totalMap.get(optimize);
                avgMap.put(optimize, avg);
            }
            identifierAvgMap.put(identifier, avgMap);
        }

        return identifierAvgMap;
    }

    private Map<String, Map<String, Map<String, Map<String, Double>>>> transform2(
            Map<String, Map<String, Map<String, Map<String, Double>>>> segmentsPredict,
            List<String> listDate,
            List<Object> listSegments) {

        Map<String, Map<String, Map<String, Map<String, Double>>>> datePredict = new LinkedHashMap<>();

        for (String date : listDate) {
            Map<String, Map<String, Map<String, Double>>> segmentPredict = new LinkedHashMap<>();
            for (Object segment : listSegments) {
                String keySegment = segment == null ? GLOBAL_KEY : segment.toString();
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

    private Map<String, Map<String, Map<String, Map<String, Double>>>> generatePredictValue() {
        Long ruleId = this.coreOptimizationRule.getId();
        this.listSegments = coreLearnerModelService.getDistinctSegmentsByRuleId(ruleId);
        Map<String, Map<String, Map<String, Map<String, Double>>>> segmentsPredict = new ConcurrentHashMap<>();

        if (listSegments == null)
            return new ConcurrentHashMap<>();

        for (Object segment : listSegments) {
            Map<String, Object> segmentMap = null;
            if (segment != null) {
                segmentMap = JsonUtil.jsonToMap(segment.toString());
            }
            Map<String, Object> finalSegmentMap = segmentMap;
            executorService = Executors.newFixedThreadPool(ConfigLoaderUtil.getExecuteServiceThreadLeaner());
            executorService.execute(() -> {
                logger.error("executorService execute");
                List<String> listIdentifier = coreLearnerModelService.getDistinctIdentifiersBySegment(finalSegmentMap, ruleId);
                Map<String, Map<String, Map<String, Double>>> dateAndIdentifierAndOptimizePredict = new LinkedHashMap<>();

                for (String identifier : listIdentifier) {
                    List<OptimizeField> listOptimizeField = coreLearnerModelService.getDistinctOptimizeBySegmentAndIdentifier(finalSegmentMap, identifier, ruleId);

                    Map<String, Map<String, Double>> dateAndOptimizePredict = new LinkedHashMap<>();
                    for (int i = 0; i < this.listDate.size(); i++) {
                        String date = this.listDate.get(i);
                        Map<String, Double> optimizeFieldPredictValues = new LinkedHashMap<>();
                        boolean isPredict = false;
                        if (i == this.listDate.size() - 1) {
                            //future date
                            isPredict = true;
                        }
                        for (OptimizeField optimizeField : listOptimizeField) {
                            Double predictValue = computePredict(optimizeField, identifier, finalSegmentMap, date, isPredict);
                            optimizeFieldPredictValues.put(JsonUtil.toJson(optimizeField), predictValue);
                        }
                        dateAndOptimizePredict.put(date, optimizeFieldPredictValues);
                    }
                    dateAndIdentifierAndOptimizePredict.put(identifier, dateAndOptimizePredict);
                }
                String key;
                if (segment == null) {
                    key = GLOBAL_KEY;
                } else {
                    key = segment.toString();
                }
                segmentsPredict.put(key, dateAndIdentifierAndOptimizePredict);

            });

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
        FactorValues factorValues = new FactorValues();
        factorValues.setIsPredictive(true);
        ConditionConverter conditionConverter = new ConditionConverter(identifier, factorValues, coreLearner,
                optimizeField, this.coreOptimizationRule, date, isPredict);
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
        return predict;
    }

}

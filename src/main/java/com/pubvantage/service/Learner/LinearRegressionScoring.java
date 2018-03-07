package com.pubvantage.service.Learner;

import com.pubvantage.AppMain;
import com.pubvantage.ConditionProcessor.ConditionConverter;
import com.pubvantage.ConditionProcessor.ConditionGenerator;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.entity.*;
import com.pubvantage.service.CoreLearningModelService;
import com.pubvantage.service.CoreLearningModelServiceInterface;
import com.pubvantage.service.OptimizationRuleService;
import com.pubvantage.service.OptimizationRuleServiceInterface;
import org.apache.log4j.Logger;
import org.apache.spark.ml.regression.LinearRegressionModel;

import java.util.*;

public class LinearRegressionScoring implements ScoringServiceInterface {
    private static Logger logger = Logger.getLogger(AppMain.class.getName());

    private OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();
    private CoreLearningModelServiceInterface coreLearningModelService = new CoreLearningModelService();
    private static final double PREDICTION_DEFAULT_VALUE = 0d;
    private CoreOptimizationRule coreOptimizationRule;
    private List<String> identifiers;
    private Condition conditions;


    public LinearRegressionScoring(CoreOptimizationRule coreAutoOptimizationRule, List<String> identifiers, Condition conditions) {
        this.coreOptimizationRule = coreAutoOptimizationRule;
        this.identifiers = identifiers;
        this.conditions = conditions;
    }

    /**
     * @return score of multiple condition
     */
    public ResponsePredict predict() {
        ConditionGenerator conditionGenerator = new ConditionGenerator(coreOptimizationRule, conditions);
        List<Map<String, Object>> multipleSegmentGroupValues = conditionGenerator.generateMultipleSegmentGroupValues();

        ResponsePredict predictions = new ResponsePredict();
        predictions.setId(coreOptimizationRule.getId());
        List<PredictScore> predictScoreList = new ArrayList<>();
        FactorValues factorValues = conditionGenerator.getFactorValues();

        //run global
        if (null == multipleSegmentGroupValues) {
            PredictScore predictionsOfOneCondition = makeMultiplePredictionsWithOneSegmentGroupValue(coreOptimizationRule, identifiers, null, factorValues);
            predictScoreList.add(predictionsOfOneCondition);

            predictions.setInfo(predictScoreList);
            return predictions;
        }

        for (Map<String, Object> segmentGroupValue : multipleSegmentGroupValues) {
            PredictScore predictionsOfOneCondition = makeMultiplePredictionsWithOneSegmentGroupValue(coreOptimizationRule, identifiers, segmentGroupValue, factorValues);
            predictScoreList.add(predictionsOfOneCondition);
        }
        predictions.setInfo(predictScoreList);
        return predictions;
    }

    /**
     * @param coreOptimizationRule
     * @param identifier
     * @param segmentValues
     * @return
     */
    private Double makeOnePrediction(CoreOptimizationRule coreOptimizationRule,
                                     String identifier,
                                     Map<String, Object> segmentValues,
                                     OptimizeField optimizeField,
                                     FactorValues factorValues) {

        CoreLearner coreLearner = coreLearningModelService.getOneCoreLeaner(
                coreOptimizationRule.getId(),
                identifier,
                optimizeField,
                segmentValues);
        if (coreLearner == null || coreLearner.getId() == null || coreLearner.getOptimizationRuleId() == null) {
            return 0D;
        }

        ConditionConverter conditionConverter = new ConditionConverter(identifier, factorValues, coreLearner, optimizeField);
        org.apache.spark.ml.linalg.Vector conditionVector = conditionConverter.buildVector();

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

    /**
     * @param coreOptimizationRule
     * @param identifiers
     * @param segmentGroupValue
     * @return
     */
    private PredictScore makeMultiplePredictionsWithOneSegmentGroupValue(CoreOptimizationRule coreOptimizationRule,
                                                                         List<String> identifiers,
                                                                         Map<String, Object> segmentGroupValue,
                                                                         FactorValues factorValues) {
        Map<String, Double> scoreData = new LinkedHashMap<>();
        Map<String, Map<String, Double>> optimizeMap = new LinkedHashMap<>();
        List<OptimizeField> optimizeFieldList = optimizationRuleService.getOptimizeFields(coreOptimizationRule);


        for (OptimizeField optimizeField : optimizeFieldList) {
            Map<String, Double> predictByOptimizeFieldAndFactorValues = new LinkedHashMap<>();

            for (String identifier : identifiers) {
                Double prediction = makeOnePrediction(coreOptimizationRule, identifier, segmentGroupValue, optimizeField, factorValues);
                predictByOptimizeFieldAndFactorValues.put(identifier, prediction);
            }

            optimizeMap.put(optimizeField.getField(), predictByOptimizeFieldAndFactorValues);

            Double total = 0D;
            for (Map.Entry<String, Double> entry : predictByOptimizeFieldAndFactorValues.entrySet()) {
                total += entry.getValue();
            }
            Map<String, Double> scoreValueByOptimizeField = new LinkedHashMap<>();
            for (Map.Entry<String, Double> entry : predictByOptimizeFieldAndFactorValues.entrySet()) {
                Double avg = total == 0 ? 0D : entry.getValue() / total;
                String identifier = entry.getKey();
                scoreValueByOptimizeField.put(identifier, avg);
            }
            for (Map.Entry<String, Double> entry : scoreValueByOptimizeField.entrySet()) {
                Double avg = entry.getValue();
                Double weight = optimizeField.getWeight();
                String goal = optimizeField.getGoal();
                double goalValue = MyConstant.MAX.equals(goal) ? 1 : -1;
                String identifier = entry.getKey();

                if (scoreData.get(identifier) == null) {
                    Double localScoreValue = goalValue * weight * avg;

                    scoreData.put(identifier, localScoreValue);
                } else {
                    Double localScoreValue = goalValue * weight * avg;
                    localScoreValue += scoreData.get(identifier);

                    scoreData.put(identifier, localScoreValue);
                }
            }
        }
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();
        for (String identifier : identifiers) {
            Map<String, Double> doubleMap = new LinkedHashMap<>();
            doubleMap.put(MyConstant.OVERALL_SCORE, scoreData.get(identifier));
            for (Map.Entry<String, Map<String, Double>> entry : optimizeMap.entrySet()) {
                String optimizeField = entry.getKey();
                Map<String, Double> predicts = entry.getValue();
                doubleMap.put(optimizeField, predicts.get(identifier));
            }
            result.put(identifier, doubleMap);
        }

        PredictScore predictScore = new PredictScore();
        predictScore.setScores(result);
        if (segmentGroupValue == null) {
            segmentGroupValue = new HashMap<>();
        }
        predictScore.setFactorValues(segmentGroupValue);
        return predictScore;
    }

}

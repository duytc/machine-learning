package com.pubvantage.service.Learner;

import com.pubvantage.ConditionProcessor.ConditionConverter;
import com.pubvantage.ConditionProcessor.ConditionGenerator;
import com.pubvantage.RestParams.FactorConditionData;
import com.pubvantage.entity.*;
import com.pubvantage.service.*;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.JsonUtil;
import org.apache.spark.ml.regression.LinearRegressionModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LinearRegressionScoring implements ScoringServiceInterface {

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
    public Map<String, Map<String, Double>> predict() {
        Map<String, Map<String, Double>> predictions = new LinkedHashMap<>();
        ConditionGenerator conditionGenerator = new ConditionGenerator(coreOptimizationRule, conditions);
        List<Map<String, Object>> multipleSegmentGroupValues = conditionGenerator.generateMultipleSegmentGroupValues();
        FactorValues factorValues = conditionGenerator.getFactorValues();

        for (Map<String, Object> segmentGroupValue : multipleSegmentGroupValues) {
//            String key = buildSegmentInfo(segmentGroupValue);
            Map<String, Double> predictionsOfOneCondition = makeMultiplePredictionsWithOneSegmentGroupValue(coreOptimizationRule, identifiers, segmentGroupValue, factorValues);
//            predictions.put(key, predictionsOfOneCondition);
        }

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
        LinearRegressionModel linearRegressionModel = LinearRegressionModel.load(coreLearner.getModelPath());
        double predict = linearRegressionModel.predict(conditionVector);
        if (Double.isNaN(predict)) {
            return PREDICTION_DEFAULT_VALUE;
        }

        return predict;
    }

    /**
     * @param coreOptimizationRule
     * @param identifiers
     * @param condition
     * @return
     */
    private Map<String, Double> makeMultiplePredictionsWithOneSegmentGroupValue(CoreOptimizationRule coreOptimizationRule, List<String> identifiers, Map<String, Object> condition, FactorValues factorValues) {
        Map<String, Double> predictions = new LinkedHashMap<>();
        List<OptimizeField> optimizeFieldList = optimizationRuleService.getOptimizeFields(coreOptimizationRule);
        for (OptimizeField optimizeField : optimizeFieldList) {
            identifiers.forEach(identifier -> {
                Double prediction = makeOnePrediction(coreOptimizationRule, identifier, condition, optimizeField, factorValues);
                predictions.put(identifier, prediction);

            });

        }


        return predictions;
    }

    /**
     * Build key from one condition
     *
     * @param condition condition
     * @return key
     */
    private String buildSegmentInfo(List<SegmentField> condition) {
        List<String> conditionArray = new ArrayList<>();
//        condition.forEach((factorName, value) -> conditionArray.add(String.valueOf(value)));

        return String.join(",", conditionArray);
    }

    private Map<String, Double> sortPredictionsByExpectedObjective(String expectedObjective, Map<String, Double> predictions) {
        if ("min".equals(expectedObjective))
            return ConvertUtil.ascendingSortMapByValue(predictions);

        return ConvertUtil.descendingSortMapByValue(predictions);
    }
}

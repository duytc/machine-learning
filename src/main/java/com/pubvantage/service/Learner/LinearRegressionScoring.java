package com.pubvantage.service.Learner;

import com.pubvantage.ConditionProcessor.ConditionGenerator;
import com.pubvantage.RestParams.FactorConditionData;
import com.pubvantage.entity.Condition;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.FactorValues;
import com.pubvantage.entity.SegmentField;
import com.pubvantage.utils.ConvertUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LinearRegressionScoring implements ScoringServiceInterface {

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
     * @param condition
     * @return
     */
    private Double makeOnePrediction(CoreOptimizationRule coreOptimizationRule, String identifier, Map<String, Object> condition, FactorValues factorValues) {

        // Step 1: Get all optimize fields
        // Step 2: Loop optimize field.
        // Step 3: Get learner model from data base
        // Step 4: build vector for learn model from factor values +  learner model
        // step 5: call predict function of spark

        return null;
    }

    /**
     * @param coreOptimizationRule
     * @param identifiers
     * @param condition
     * @return
     */
    private Map<String, Double> makeMultiplePredictionsWithOneSegmentGroupValue(CoreOptimizationRule coreOptimizationRule, List<String> identifiers, Map<String, Object> condition, FactorValues factorValues) {
        Map<String, Double> predictions = new LinkedHashMap<>();

        identifiers.forEach(identifier -> {
            Double prediction = makeOnePrediction(coreOptimizationRule, identifier, condition, factorValues);
            predictions.put(identifier, prediction);

        });

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

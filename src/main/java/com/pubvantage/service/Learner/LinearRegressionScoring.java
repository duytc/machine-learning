package com.pubvantage.service.Learner;

import com.google.gson.JsonArray;
import com.pubvantage.ConditionProcessor.ConditionConverter;
import com.pubvantage.ConditionProcessor.ConditionGenerator;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.service.LoadingLearnerModel;
import com.pubvantage.utils.ConvertUtil;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.regression.LinearRegressionModel;

import java.util.*;

public class LinearRegressionScoring implements ScoringServiceInterface {

    private static final double PREDICTION_DEFAULT_VALUE = 0d;
    private final CoreOptimizationRule coreOptimizationRule;
    private final List<String> identifiers;
    private final JsonArray conditions;

    public LinearRegressionScoring(CoreOptimizationRule coreAutoOptimizationRule, List<String> identifiers, JsonArray conditions) {
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
        List<Map<String, Object>> multipleConditions = conditionGenerator.generateMultipleConditions();

        for (Map<String, Object> conditions : multipleConditions) {
            String key = buildSegmentInfo(conditions);
            Map<String, Double> predictionsOfOneCondition = makeMultiplePredictionsWithOneCondition(coreOptimizationRule, identifiers, conditions);
            predictions.put(key, predictionsOfOneCondition);
        }

        return predictions;
    }

    /**
     *
     * @param coreOptimizationRule
     * @param identifier
     * @param condition
     * @return
     */
    private Double makeOnePrediction(CoreOptimizationRule coreOptimizationRule, String identifier, Map<String, Object> condition) {

        return  null;
    }

    /**
     *
     * @param coreOptimizationRule
     * @param identifiers
     * @param condition
     * @return
     */
    private Map<String, Double> makeMultiplePredictionsWithOneCondition(CoreOptimizationRule coreOptimizationRule, List<String> identifiers, Map<String, Object> condition) {
        Map<String, Double> predictions = new LinkedHashMap<>();

        identifiers.forEach(identifier -> {
            Double prediction = makeOnePrediction(coreOptimizationRule, identifier, condition);
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
    private String buildSegmentInfo(Map<String, Object> condition) {
        List<String> conditionArray = new ArrayList<>();
        condition.forEach((factorName, value) -> conditionArray.add(String.valueOf(value)));

        return String.join(",", conditionArray);
    }

    private Map<String, Double> sortPredictionsByExpectedObjective(String expectedObjective, Map<String, Double> predictions) {
        if ("min".equals(expectedObjective))
            return ConvertUtil.ascendingSortMapByValue(predictions);

        return ConvertUtil.descendingSortMapByValue(predictions);
    }
}

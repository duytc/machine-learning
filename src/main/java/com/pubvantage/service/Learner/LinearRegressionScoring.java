package com.pubvantage.service.Learner;

import com.google.gson.JsonArray;
import com.pubvantage.ConditionProcessor.ConditionConverter;
import com.pubvantage.ConditionProcessor.ConditionGenerator;
import com.pubvantage.entity.CoreAutoOptimizationConfig;
import com.pubvantage.service.LoadingLearnerModel;
import com.pubvantage.utils.ConvertUtil;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.regression.LinearRegressionModel;

import java.util.*;

public class LinearRegressionScoring implements ScoringServiceInterface {

    private static final double PREDICTION_DEFAULT_VALUE = 0d;
    private final CoreAutoOptimizationConfig coreAutoOptimizationConfig;
    private final List<String> identifiers;
    private final JsonArray conditions;

    public LinearRegressionScoring(CoreAutoOptimizationConfig coreAutoOptimizationConfig, List<String> identifiers, JsonArray conditions) {
        this.coreAutoOptimizationConfig = coreAutoOptimizationConfig;
        this.identifiers = identifiers;
        this.conditions = conditions;
    }

    /**
     * @return score of multiple condition
     */
    public Map<String, Map<String, Double>> predict() {
        Map<String, Map<String, Double>> predictions = new LinkedHashMap<>();
        ConditionGenerator conditionGenerator = new ConditionGenerator(coreAutoOptimizationConfig, conditions);
        List<Map<String, Object>> multipleConditions = conditionGenerator.generateMultipleConditions();

        for (Map<String, Object> conditions : multipleConditions) {
            String key = buildKey(conditions);
            Map<String, Double> predictionsOfOneCondition = makeMultiplePredictionsWithOneCondition(coreAutoOptimizationConfig, identifiers, conditions);
            predictions.put(key, predictionsOfOneCondition);
        }

        return predictions;
    }

    /**
     * make one prediction with one condition of one learner model.
     *
     * @param coreAutoOptimizationConfig id of auto optimization config
     * @param identifier                 identifier
     * @param condition                  one condition
     * @return
     */
    private Double makeOnePrediction(CoreAutoOptimizationConfig coreAutoOptimizationConfig, String identifier, Map<String, Object> condition) {
        ConditionConverter conditionConverter = new ConditionConverter(coreAutoOptimizationConfig, identifier, condition);
        Vector conditionVector = conditionConverter.buildVector();

        if (conditionVector == null) {
            return PREDICTION_DEFAULT_VALUE;
        }

        LoadingLearnerModel loadingLearnerModel = new LoadingLearnerModel(coreAutoOptimizationConfig, identifier);
        LinearRegressionModel linearRegressionModel = loadingLearnerModel.loadLearnerModel();

        double predict = linearRegressionModel.predict(conditionVector);
        if (Double.isNaN(predict)) {
            return PREDICTION_DEFAULT_VALUE;
        }

        return predict;
    }

    /**
     * Make many predictions for many identifiers
     *
     * @param coreAutoOptimizationConfig coreAutoOptimizationConfig
     * @param identifiers                identifiers
     * @param condition                  condition
     * @return
     */
    private Map<String, Double> makeMultiplePredictionsWithOneCondition(CoreAutoOptimizationConfig coreAutoOptimizationConfig, List<String> identifiers, Map<String, Object> condition) {
        Map<String, Double> predictions = new LinkedHashMap<>();

        identifiers.forEach(identifier -> {
            Double prediction = makeOnePrediction(coreAutoOptimizationConfig, identifier, condition);
            predictions.put(identifier, prediction);

        });

        String expectedObjective = coreAutoOptimizationConfig.getExpectedObjective();

        return sortPredictionsByExpectedObjective(expectedObjective, predictions);
    }

    /**
     * Build key from one condition
     *
     * @param condition condition
     * @return key
     */
    private String buildKey(Map<String, Object> condition) {
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

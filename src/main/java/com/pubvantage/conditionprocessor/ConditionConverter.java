package com.pubvantage.conditionprocessor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubvantage.entity.*;
import com.pubvantage.service.DataTraning.DataTrainingService;
import com.pubvantage.service.DataTraning.DataTrainingServiceInterface;
import com.pubvantage.service.OptimizationRuleService;
import com.pubvantage.service.OptimizationRuleServiceInterface;
import com.pubvantage.utils.JsonUtil;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConditionConverter {
    DataTrainingServiceInterface dataTrainingService = new DataTrainingService();
    private String identifier;
    private FactorValues factorValues;
    private CoreLearner coreLearner;
    private OptimizeField optimizeField;
    private OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();
    private CoreOptimizationRule optimizationRule;
    private String date;
    private boolean isPredict;

    public ConditionConverter(String identifier, FactorValues factorValues, CoreLearner coreLearner, OptimizeField optimizeField) {
        this.identifier = identifier;
        this.factorValues = factorValues;
        this.coreLearner = coreLearner;
        this.optimizeField = optimizeField;
    }

    public ConditionConverter(String identifier,
                              FactorValues factorValues,
                              CoreLearner coreLearner,
                              OptimizeField optimizeField,
                              CoreOptimizationRule optimizationRule,
                              String date,
                              boolean isPredict) {
        this.identifier = identifier;
        this.factorValues = factorValues;
        this.coreLearner = coreLearner;
        this.optimizeField = optimizeField;
        this.optimizationRule = optimizationRule;
        this.date = date;
        this.isPredict = isPredict;
    }

    public Vector buildVectorV2() {
        List<String> metrics = getMetricsFromCoreLeaner(coreLearner);
        double[] doubleValue = new double[metrics.size()];
        if (!isPredict) {
            List<Double> doubleFromDB = dataTrainingService.getVectorData(metrics, this.optimizationRule, this.date);
            for (int i = 0; i < doubleValue.length; i++) {
                doubleValue[i] = doubleFromDB.get(i);
            }
        } else {
            LinkedHashMap<String, Double> metricsPredictiveValues = getMetricsPredictionValues(coreLearner);
            for (int index = 0; index < metrics.size(); index++) {
                String fieldName = metrics.get(index);
                Double metricPredictValue = metricsPredictiveValues.get(fieldName);
                doubleValue[index] = metricPredictValue == null ? 0D : metricPredictValue;
            }
        }
        return Vectors.dense(doubleValue);
    }

    /**
     * Build vector that is the input of linear regression model
     *
     * @return input vector for linear regression model
     */
    public Vector buildVector() {
        List<String> metrics = getMetricsFromCoreLeaner(coreLearner);
        // prepare array of double value for vector
        LinkedHashMap<String, Double> metricsPredictiveValues = getMetricsPredictionValues(coreLearner);

        double[] doubleValue = new double[metrics.size()];
        if (factorValues != null) {
            for (int index = 0; index < metrics.size(); index++) {
                String fieldName = metrics.get(index);

                if (factorValues.getIsPredictive()) {
                    Double metricPredictValue = metricsPredictiveValues.get(fieldName);
                    doubleValue[index] = metricPredictValue == null ? 0D : metricPredictValue;
                } else {
                    JsonObject values = factorValues.getValues().get(identifier);
                    if (values != null || values.isJsonNull()) {
                        JsonElement value = values.get(fieldName);
                        if (value == null || value.isJsonNull()) {
                            Double metricPredictValue = metricsPredictiveValues.get(fieldName);
                            doubleValue[index] = metricPredictValue == null ? 0D : metricPredictValue;
                        } else {
                            doubleValue[index] = value.getAsDouble();
                        }

                    }
                }
            }
        }
        return Vectors.dense(doubleValue);
    }

    private LinkedHashMap<String, Double> getMetricsPredictionValues(CoreLearner coreLearner) {
        return JsonUtil.jsonToLinkedHashMap(coreLearner.getMetricsPredictiveValues());
    }

    private List<String> getMetricsFromCoreLeaner(CoreLearner coreLearner) {
        List<String> list = new ArrayList<>();
        MathModel mathModel = JsonUtil.jsonToObject(coreLearner.getMathModel(), MathModel.class);
        if (mathModel != null && mathModel.getCoefficients() != null && !mathModel.getCoefficients().isEmpty()) {
            for (Map.Entry<String, Double> entry : mathModel.getCoefficients().entrySet()) {
                list.add(entry.getKey());
            }
        }
        return list;
    }

}
package com.pubvantage.conditionprocessor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.FactorValues;
import com.pubvantage.service.CoreLearningModelService;
import com.pubvantage.service.CoreLearningModelServiceInterface;
import com.pubvantage.service.DataTraning.DataTrainingService;
import com.pubvantage.service.DataTraning.DataTrainingServiceInterface;
import com.pubvantage.utils.JsonUtil;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;

import java.util.LinkedHashMap;
import java.util.List;

public class ConditionConverter {
    private DataTrainingServiceInterface dataTrainingService = new DataTrainingService();
    private CoreLearningModelServiceInterface coreLearnerModelService = new CoreLearningModelService();
    private String identifier;
    private FactorValues factorValues;
    private CoreLearner coreLearner;
    private CoreOptimizationRule optimizationRule;
    private String date;
    private boolean isPredict;

    public ConditionConverter(String identifier, FactorValues factorValues, CoreLearner coreLearner) {
        this.identifier = identifier;
        this.factorValues = factorValues;
        this.coreLearner = coreLearner;
    }

    public ConditionConverter(String identifier,
                              FactorValues factorValues,
                              CoreLearner coreLearner,
                              CoreOptimizationRule optimizationRule,
                              String date,
                              boolean isPredict) {
        this.identifier = identifier;
        this.factorValues = factorValues;
        this.coreLearner = coreLearner;
        this.optimizationRule = optimizationRule;
        this.date = date;
        this.isPredict = isPredict;
    }

    public Vector buildVectorV2() {
        List<String> metrics = coreLearnerModelService.getMetricsFromCoreLeaner(coreLearner);
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
        List<String> metrics = coreLearnerModelService.getMetricsFromCoreLeaner(coreLearner);
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


}

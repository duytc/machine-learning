package com.pubvantage.ConditionProcessor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jsoniter.any.Any;
import com.pubvantage.entity.*;
import com.pubvantage.service.OptimizationRuleService;
import com.pubvantage.service.OptimizationRuleServiceInterface;
import com.pubvantage.utils.JsonUtil;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConditionConverter {
    private String identifier;
    private FactorValues factorValues;
    private CoreLearner coreLearner;
    private OptimizeField optimizeField;
    private OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();

    public ConditionConverter(String identifier, FactorValues factorValues, CoreLearner coreLearner, OptimizeField optimizeField) {
        this.identifier = identifier;
        this.factorValues = factorValues;
        this.coreLearner = coreLearner;
        this.optimizeField = optimizeField;
    }

    /**
     * Build vector that is the input of linear regression model
     *
     * @return input vector for linear regression model
     */
    public Vector buildVector() {
        List<String> metrics = getMetrics(coreLearner);
        // prepare array of double value for vector
        JsonObject metricsPredictiveValues = new JsonObject();
        double[] doubleValue = new double[metrics.size()];
        if (factorValues != null) {
            for (int index = 0; index < metrics.size(); index++) {
                String fieldName = metrics.get(index);

                if (factorValues.getIsPredictive()) {
                    doubleValue[index] = metricsPredictiveValues.get(fieldName).getAsDouble();
                } else {
                    JsonObject values = factorValues.getValues();
                    if (values != null) {
                        JsonElement value = values.get(fieldName);
                        if (value == null) {
                            doubleValue[index] = metricsPredictiveValues.get(fieldName) != null ? metricsPredictiveValues.get(fieldName).getAsDouble() : 0D;
                        } else {
                            doubleValue[index] = value.getAsDouble();
                        }

                    }
                }
            }
        }
        return Vectors.dense(doubleValue);
    }

    private List<String> getMetrics(CoreLearner coreLearner) {
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

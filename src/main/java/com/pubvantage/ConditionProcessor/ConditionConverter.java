package com.pubvantage.ConditionProcessor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubvantage.entity.CoreAutoOptimizationConfig;
import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.FactorValues;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.service.CoreLearningModelService;
import com.pubvantage.service.CoreLearningModelServiceInterface;
import com.pubvantage.service.OptimizationRuleService;
import com.pubvantage.service.OptimizationRuleServiceInterface;
import com.pubvantage.utils.ConvertUtil;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConditionConverter {
    private CoreAutoOptimizationConfig coreAutoOptimizationConfig;
    private String identifier;
    private Map<String, Object> condition = new LinkedHashMap<>();
    private CoreLearningModelServiceInterface coreLearningModelService = new CoreLearningModelService();


    private FactorValues factorValues;
    private CoreLearner coreLearner;
    private OptimizeField optimizeField;
    private OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();

    public ConditionConverter(CoreAutoOptimizationConfig coreAutoOptimizationConfig, String identifier, Map<String, Object> condition) {
        this.coreAutoOptimizationConfig = coreAutoOptimizationConfig;
        this.identifier = identifier;
        this.condition = condition;
    }

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
        List<String> metrics = optimizationRuleService.getMetrics(coreLearner.getOptimizationRuleId());
        // prepare array of double value for vector
        JsonObject metricsPredictiveValues = new JsonObject();
        double[] doubleValue = new double[metrics.size() - 1]; //skip optimize field
        if (factorValues != null) {
            for (int index = 0; index < metrics.size(); index++) {
                String fieldName = metrics.get(index);
                if (fieldName.equals(optimizeField.getField())) {
                    //skip optimizeField
                    continue;
                }
                if (factorValues.getIsPredictive()) {
                    doubleValue[index] = metricsPredictiveValues.get(fieldName).getAsDouble();
                } else {
                    JsonObject values = factorValues.getValues();
                    if (values != null) {
                        Double value = values.get(fieldName).getAsDouble();
                        if (value == null) {
                            doubleValue[index] = metricsPredictiveValues.get(fieldName).getAsDouble();
                        } else {
                            doubleValue[index] = value;
                        }

                    }
                }
            }
        }
        return Vectors.dense(doubleValue);
    }


    private Double getValue(FactorValues factorValues, CoreLearner coreLearner, String fieldName) {

        Object value = factorValues.getValues().get(fieldName);
        if (value != null) {

        }
        return 0D;
    }

    /**
     * @param factorName          factor's name
     * @param forecast            forecast data
     * @param categoryFieldWeighs category field weigh data
     * @param condition           condition data
     * @param fieldType           factors's data
     * @return Double value for a factor
     */
    private Double getValueForFactor(String factorName, JsonObject forecast, JsonObject categoryFieldWeighs, Map<String, Object> condition, JsonObject fieldType) {
        Object conditionData = null;
        if (condition != null) {
            conditionData = condition.get(factorName);
        }
        if (conditionData == null) {
            return getVectorValueFromForecast(factorName, forecast);
        }
        if (ConvertUtil.isNumberFactor(fieldType.get(factorName).getAsString())) {
            return Double.parseDouble(conditionData.toString());
        }
        return getVectorValueFromCategoryFieldWeigh(factorName, conditionData, categoryFieldWeighs, forecast);
    }

    /**
     * Return Double value for factor in category field weigh.
     * If this Double value not exist then return Double value from forecast
     *
     * @param factorName          factor's name
     * @param conditionData       condition data
     * @param categoryFieldWeighs category field weigh data
     * @param forecast            forecast data
     * @return Double value
     */
    private Double getVectorValueFromCategoryFieldWeigh(String factorName, Object conditionData, JsonObject categoryFieldWeighs, JsonObject forecast) {
        JsonElement categoryFieldWeighData = null;
        if (categoryFieldWeighs != null) {
            categoryFieldWeighData = categoryFieldWeighs.get(factorName);
        }
        if (categoryFieldWeighData == null) {
            return getVectorValueFromForecast(factorName, forecast);
        }
        JsonObject categoryFieldWeighDataObject = new Gson().fromJson(categoryFieldWeighData, JsonObject.class);
        Object categoryFieldWeighValue = categoryFieldWeighDataObject.get(conditionData.toString());
        if (categoryFieldWeighValue != null) {
            return Double.parseDouble(categoryFieldWeighValue.toString());
        }
        return getVectorValueFromForecast(factorName, forecast);
    }

    /**
     * @param factorName factor's name
     * @param forecast   forecast data
     * @return Double value for factor from forecast data
     */
    private Double getVectorValueFromForecast(String factorName, JsonObject forecast) {
        if (forecast == null)
            return 0d;

        Object forecastValue = forecast.get(factorName);
        if (forecastValue != null) {
            return Double.parseDouble(forecastValue.toString());
        }
        return 0d;
    }
}

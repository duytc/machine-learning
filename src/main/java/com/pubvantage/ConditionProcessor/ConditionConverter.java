package com.pubvantage.ConditionProcessor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pubvantage.entity.CoreAutoOptimizationConfig;
import com.pubvantage.entity.CoreLearningModel;
import com.pubvantage.service.CoreAutoOptimizationConfigService;
import com.pubvantage.service.CoreAutoOptimizationConfigServiceInterface;
import com.pubvantage.service.CoreLearningModelService;
import com.pubvantage.service.CoreLearningModelServiceInterface;
import com.pubvantage.utils.ConvertUtil;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;

import java.util.List;
import java.util.Map;

public class ConditionConverter {
    private final CoreAutoOptimizationConfig coreAutoOptimizationConfig;
    private final String identifier;
    private final Map<String, Object> condition;
    private CoreLearningModelServiceInterface coreLearningModelService = new CoreLearningModelService();

    public ConditionConverter(CoreAutoOptimizationConfig coreAutoOptimizationConfig, String identifier, Map<String, Object> condition) {
        this.coreAutoOptimizationConfig = coreAutoOptimizationConfig;
        this.identifier = identifier;
        this.condition = condition;
    }

    /**
     *  Build vector that is the input of linear regression model
     * @return input vector for linear regression model
     */
    public Vector buildVector() {
        CoreLearningModel coreLearningModel = coreLearningModelService.findOne(coreAutoOptimizationConfig.getId(), identifier);
        if (coreLearningModel == null) {
            return null;
        }
        JsonParser parser = new JsonParser();
        //get category field weigh
        String categoryFieldWeighString = coreLearningModel.getCategoricalFieldWeights();
        if (categoryFieldWeighString == null) {
            return null;
        }
        JsonObject categoryFieldWeighs = parser.parse(categoryFieldWeighString).getAsJsonObject();
        //get forecast
        String forecastString = coreLearningModel.getForecastFactorValues();
        if (forecastString == null) {
            return null;
        }
        JsonObject forecast = parser.parse(forecastString).getAsJsonObject();
        //get list of factors and fields type
        CoreAutoOptimizationConfigServiceInterface coreAutoOptimizationConfigService = new CoreAutoOptimizationConfigService();
        List<String> factors = coreAutoOptimizationConfigService.getFactors(coreAutoOptimizationConfig.getId());
        JsonObject fieldType = coreAutoOptimizationConfigService.getFieldType(coreAutoOptimizationConfig.getId());

        // prepare array of double value for vector
        double[] doubleValue = new double[factors.size()];
        for (int factorIndex = 0; factorIndex < factors.size(); factorIndex++) {
            String factorName = factors.get(factorIndex);
            Double value = getValueForFactor(factorName, forecast, categoryFieldWeighs, condition, fieldType);
            if (value == null) {
                value = 0d;
            }
            doubleValue[factorIndex] = value;
        }
        return Vectors.dense(doubleValue);
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

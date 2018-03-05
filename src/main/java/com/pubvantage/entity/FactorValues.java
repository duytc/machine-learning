package com.pubvantage.entity;

import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Created by quyendq on 03/03/2018.
 */
public class FactorValues {
    private boolean isPredictive;
    private Map<String, JsonObject> values;

    public FactorValues() {
    }

    public FactorValues(boolean isPredictive, Map<String, JsonObject> values) {
        this.isPredictive = isPredictive;
        this.values = values;
    }

    public boolean getIsPredictive() {
        return isPredictive;
    }

    public void setIsPredictive(boolean predictive) {
        isPredictive = predictive;
    }

    public Map<String, JsonObject> getValues() {
        return values;
    }

    public void setValues(Map<String, JsonObject> values) {
        this.values = values;
    }
}

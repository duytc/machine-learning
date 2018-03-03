package com.pubvantage.entity;

import com.google.gson.JsonObject;

/**
 * Created by quyendq on 03/03/2018.
 */
public class FactorValues {
    private boolean isPredictive;
    private JsonObject values;

    public FactorValues() {
    }

    public FactorValues(boolean isPredictive, JsonObject values) {
        this.isPredictive = isPredictive;
        this.values = values;
    }

    public boolean getIsPredictive() {
        return isPredictive;
    }

    public void setIsPredictive(boolean predictive) {
        isPredictive = predictive;
    }

    public JsonObject getValues() {
        return values;
    }

    public void setValues(JsonObject values) {
        this.values = values;
    }
}

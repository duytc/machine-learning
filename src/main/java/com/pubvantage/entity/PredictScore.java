package com.pubvantage.entity;

import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Created by quyendq on 04/03/2018.
 */
public class PredictScore {
    private JsonObject factorValues;
    private Map<String, Double> score;

    public PredictScore() {
    }

    public PredictScore(JsonObject factorValues, Map<String, Double> score) {
        this.factorValues = factorValues;
        this.score = score;
    }


    public JsonObject getFactorValues() {
        return factorValues;
    }

    public void setFactorValues(JsonObject factorValues) {
        this.factorValues = factorValues;
    }

    public Map<String, Double> getScore() {
        return score;
    }

    public void setScore(Map<String, Double> score) {
        this.score = score;
    }
}

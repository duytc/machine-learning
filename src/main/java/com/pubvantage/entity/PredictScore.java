package com.pubvantage.entity;

import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Created by quyendq on 04/03/2018.
 */
public class PredictScore {
    private Map<String, Object> factorValues;
    private Map<String, Map<String, Double>> score;

    public PredictScore() {
    }

    public PredictScore(Map<String, Object> factorValues, Map<String, Map<String, Double>> score) {
        this.factorValues = factorValues;
        this.score = score;
    }

    public Map<String, Object> getFactorValues() {
        return factorValues;
    }

    public void setFactorValues(Map<String, Object> factorValues) {
        this.factorValues = factorValues;
    }

    public Map<String, Map<String, Double>> getScore() {
        return score;
    }

    public void setScore(Map<String, Map<String, Double>> score) {
        this.score = score;
    }
}

package com.pubvantage.entity;

import java.util.Map;

/**
 * Created by quyendq on 04/03/2018.
 */
public class PredictScore {
    private Map<String, Object> factorValues;
    private Map<String, Map<String, Double>> scores;

    public PredictScore() {
    }

    public PredictScore(Map<String, Object> factorValues, Map<String, Map<String, Double>> scores) {
        this.factorValues = factorValues;
        this.scores = scores;
    }

    public Map<String, Object> getFactorValues() {
        return factorValues;
    }

    public void setFactorValues(Map<String, Object> factorValues) {
        this.factorValues = factorValues;
    }

    public Map<String, Map<String, Double>> getScores() {
        return scores;
    }

    public void setScores(Map<String, Map<String, Double>> scores) {
        this.scores = scores;
    }
}

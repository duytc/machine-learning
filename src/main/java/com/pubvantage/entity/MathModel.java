package com.pubvantage.entity;

import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Created by quyendq on 04/03/2018.
 */
public class MathModel {
    private Map<String, Double> Coefficients;
    private Double Intercept;

    public MathModel() {
    }

    public MathModel(Map<String, Double> coefficients, Double intercept) {
        Coefficients = coefficients;
        Intercept = intercept;
    }

    public Map<String, Double> getCoefficients() {
        return Coefficients;
    }

    public void setCoefficients(Map<String, Double> coefficients) {
        Coefficients = coefficients;
    }

    public Double getIntercept() {
        return Intercept;
    }

    public void setIntercept(Double intercept) {
        Intercept = intercept;
    }
}

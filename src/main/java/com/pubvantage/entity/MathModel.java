package com.pubvantage.entity;

import java.util.LinkedHashMap;

/**
 * Created by quyendq on 04/03/2018.
 */
public class MathModel {
    /**
     * need to be LinkedHashMap to keep order
     */
    private LinkedHashMap<String, Double> Coefficients;
    private Double Intercept;

    public MathModel() {
    }

    public MathModel(LinkedHashMap<String, Double> coefficients, Double intercept) {
        Coefficients = coefficients;
        Intercept = intercept;
    }

    public LinkedHashMap<String, Double> getCoefficients() {
        return Coefficients;
    }

    public void setCoefficients(LinkedHashMap<String, Double> coefficients) {
        Coefficients = coefficients;
    }

    public Double getIntercept() {
        return Intercept;
    }

    public void setIntercept(Double intercept) {
        Intercept = intercept;
    }
}

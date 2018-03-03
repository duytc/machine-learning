package com.pubvantage.entity;

/**
 * Created by quyendq on 03/03/2018.
 */
public class OptimizeField {
    private String field;
    private Double weight;
    private String goal;

    public OptimizeField() {
    }

    public OptimizeField(String field, Double weight, String goal) {
        this.field = field;
        this.weight = weight;
        this.goal = goal;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }


}

package com.pubvantage.entity;

import com.google.gson.JsonObject;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;

public class ConvertedDataWrapper {
    private List<String> objectiveAndFactors;
    private Dataset<Row> dataSet;
    private JsonObject categoryWeight;
    private JsonObject forecast;


    public ConvertedDataWrapper() {
    }

    public List<String> getObjectiveAndFactors() {
        return objectiveAndFactors;
    }

    public void setObjectiveAndFactors(List<String> objectiveAndFactors) {
        this.objectiveAndFactors = objectiveAndFactors;
    }

    public Dataset<Row> getDataSet() {
        return dataSet;
    }

    public void setDataSet(Dataset<Row> dataSet) {
        this.dataSet = dataSet;
    }

    public JsonObject getCategoryWeight() {
        return categoryWeight;
    }

    public void setCategoryWeight(JsonObject categoryWeight) {
        this.categoryWeight = categoryWeight;
    }

    public JsonObject getForecast() {
        return forecast;
    }

    public void setForecast(JsonObject forecast) {
        this.forecast = forecast;
    }
}

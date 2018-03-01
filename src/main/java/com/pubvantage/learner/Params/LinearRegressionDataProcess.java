package com.pubvantage.learner.Params;

import com.google.gson.JsonObject;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;

public class LinearRegressionDataProcess {
    private Long optimizationRuleId;
    private String identifier;
    private List<String> oneSegmentGroup;
    private Object uniqueValue;
    private String optimizeField;

    public LinearRegressionDataProcess(Long optimizationRuleId, String identifier, List<String> oneSegmentGroup, Object uniqueValue, String optimizeField) {
        this.optimizationRuleId = optimizationRuleId;
        this.identifier = identifier;
        this.oneSegmentGroup = oneSegmentGroup;
        this.uniqueValue = uniqueValue;
        this.optimizeField = optimizeField;
    }

    public Long getOptimizationRuleId() {
        return optimizationRuleId;
    }

    public void setOptimizationRuleId(Long optimizationRuleId) {
        this.optimizationRuleId = optimizationRuleId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<String> getOneSegmentGroup() {
        return oneSegmentGroup;
    }

    public void setOneSegmentGroup(List<String> oneSegmentGroup) {
        this.oneSegmentGroup = oneSegmentGroup;
    }

    public Object getUniqueValue() {
        return uniqueValue;
    }

    public void setUniqueValue(JsonObject uniqueValue) {
        this.uniqueValue = uniqueValue;
    }

    public String getOptimizeField() {
        return optimizeField;
    }

    public void setOptimizeField(String optimizeField) {
        this.optimizeField = optimizeField;
    }

    public Dataset<Row> getTrainingDataForLinearRegression()
    {
        return  null;
    }

}

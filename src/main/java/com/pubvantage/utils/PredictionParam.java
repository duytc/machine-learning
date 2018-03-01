package com.pubvantage.utils;

import java.util.ArrayList;
import java.util.List;

public class PredictionParam {
    private long autoOptimizationId;
    private String identifier;
    private List<String> segmentField;

    public PredictionParam(long autoOptimizationId, String identifier, List<String> segmentField) {
        this.autoOptimizationId = autoOptimizationId;
        this.identifier = identifier;
        this.segmentField = segmentField;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public long getAutoOptimizationId() {
        return autoOptimizationId;
    }

    public void setAutoOptimizationId(long autoOptimizationId) {
        this.autoOptimizationId = autoOptimizationId;
    }



    public List<String> getSegmentField() {
        return segmentField;
    }

    public void setSegmentField(ArrayList<String> segmentField) {
        this.segmentField = segmentField;
    }

    /**
     * Return segment field groups
     * @return
     */
    public ArrayList<ArrayList<String>> generateMultipleSegmentFieldGroups() {

        return null;
    }
}

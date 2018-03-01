package com.pubvantage.utils;

import java.util.ArrayList;
import java.util.List;

public class PredictionParam {
    private long autoOptimizationId;
    private String identifier;
    private List<String> segmentFields;

    public PredictionParam() {
    }

    public PredictionParam(long autoOptimizationId, String identifier, List<String> segmentFields) {
        this.autoOptimizationId = autoOptimizationId;
        this.identifier = identifier;
        this.segmentFields = segmentFields;
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



    public List<String> getSegmentFields() {
        return segmentFields;
    }

    public void setSegmentFields(ArrayList<String> segmentFields) {
        this.segmentFields = segmentFields;
    }

    /**
     * Return segment field groups
     * @return
     */
    public List<List<String>> generateMultipleSegmentFieldGroups() {

        return null;
    }
}

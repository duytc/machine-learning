package com.pubvantage.utils;

import java.util.ArrayList;
import java.util.List;

public class SegmentFieldGroup {
    private long autoOptimizationId;
    private String identifier;
    private List<String> segmentFieldGroup;

    public SegmentFieldGroup(long autoOptimizationId, String identifier, List<String> segmentFieldGroup) {
        this.autoOptimizationId = autoOptimizationId;
        this.identifier = identifier;
        this.segmentFieldGroup = segmentFieldGroup;
    }

    public long getAutoOptimizationId() {
        return autoOptimizationId;
    }

    public void setAutoOptimizationId(long autoOptimizationId) {
        this.autoOptimizationId = autoOptimizationId;
    }



    public List<String> getSegmentFieldGroup() {
        return segmentFieldGroup;
    }

    public void setSegmentFieldGroup(ArrayList<String> segmentFieldGroup) {
        this.segmentFieldGroup = segmentFieldGroup;
    }

    /**
     * Return segment field groups
     * @return
     */
    public ArrayList<ArrayList<String>> getSegmentFieldGroups() {

        return null;
    }
}

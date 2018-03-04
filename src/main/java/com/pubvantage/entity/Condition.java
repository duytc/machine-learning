package com.pubvantage.entity;

import java.util.List;

/**
 * Created by quyendq on 03/03/2018.
 */
public class Condition {
    private List<SegmentField> segmentFields;
    private FactorValues factorValues;
    private Boolean isGlobal;

    public Condition() {
    }

    public Condition(FactorValues factorValues) {
        this.factorValues = factorValues;
    }

    public List<SegmentField> getSegmentFields() {
        return segmentFields;
    }

    public void setSegmentFields(List<SegmentField> segmentFields) {
        this.segmentFields = segmentFields;
    }

    public FactorValues getFactorValues() {
        return factorValues;
    }

    public void setFactorValues(FactorValues factorValues) {
        this.factorValues = factorValues;
    }

    public Boolean getGlobal() {
        return isGlobal;
    }

    public void setGlobal(Boolean global) {
        if (global == null)
            isGlobal = false;
        isGlobal = global;
    }
}

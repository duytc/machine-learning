package com.pubvantage.entity;

import java.util.List;

/**
 * Created by quyendq on 03/03/2018.
 */
public class Condition {
    private List<SegmentField> segmentFields;
    private FactorValues factorValues;

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
}

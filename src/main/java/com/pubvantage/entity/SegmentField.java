package com.pubvantage.entity;

import java.util.List;

/**
 * Created by quyendq on 03/03/2018.
 */
public class SegmentField {
    private String segmentField;
    private List<Object> values;
    private Boolean isAll;

    public SegmentField() {
    }

    public SegmentField(String segmentField, List<Object> values, Boolean isAll) {
        this.segmentField = segmentField;
        this.values = values;
        this.isAll = isAll;
    }

    public String getSegmentField() {
        return segmentField;
    }

    public void setSegmentField(String segmentField) {
        this.segmentField = segmentField;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }

    public Boolean getAll() {
        return isAll;
    }

    public void setAll(Boolean all) {
        isAll = all;
    }
}

package com.pubvantage.entity;

/**
 * Created by quyendq on 14/03/2018.
 */
public class SegmentAndIOptimizeField {
    private String optimize_field;
    private String segment_values;

    public SegmentAndIOptimizeField(String optimize_field, String segment_values) {
        this.optimize_field = optimize_field;
        this.segment_values = segment_values;
    }

    public SegmentAndIOptimizeField() {
    }

    public String getOptimize_field() {
        return optimize_field;
    }

    public void setOptimize_field(String optimize_field) {
        this.optimize_field = optimize_field;
    }

    public String getSegment_values() {
        return segment_values;
    }

    public void setSegment_values(String segment_values) {
        this.segment_values = segment_values;
    }
}

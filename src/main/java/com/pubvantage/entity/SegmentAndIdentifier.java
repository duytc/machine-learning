package com.pubvantage.entity;

/**
 * Created by quyendq on 14/03/2018.
 */
public class SegmentAndIdentifier {
    private String identifier;
    private String segment_values;

    public SegmentAndIdentifier(String identifier, String segment_values) {
        this.identifier = identifier;
        this.segment_values = segment_values;
    }

    public SegmentAndIdentifier() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSegment_values() {
        return segment_values;
    }

    public void setSegment_values(String segment_values) {
        this.segment_values = segment_values;
    }
}

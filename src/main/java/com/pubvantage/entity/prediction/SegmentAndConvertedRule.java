package com.pubvantage.entity.prediction;

import java.util.List;
import java.util.Map;

/**
 * Created by quyendq on 10/04/2018.
 */
public class SegmentAndConvertedRule {
    private List<Map<String, String>> segmentData;
    private Map<String, List<String>> convertedRule;
    private String jsonSegmentData;

    public SegmentAndConvertedRule(List<Map<String, String>> segmentData, Map<String, List<String>> convertedRule, String jsonSegmentData) {
        this.segmentData = segmentData;
        this.convertedRule = convertedRule;
        this.jsonSegmentData = jsonSegmentData;
    }

    public SegmentAndConvertedRule() {
    }

    public String getJsonSegmentData() {
        return jsonSegmentData;
    }

    public void setJsonSegmentData(String jsonSegmentData) {
        this.jsonSegmentData = jsonSegmentData;
    }

    public List<Map<String, String>> getSegmentData() {
        return segmentData;
    }

    public void setSegmentData(List<Map<String, String>> segmentData) {
        this.segmentData = segmentData;
    }

    public Map<String, List<String>> getConvertedRule() {
        return convertedRule;
    }

    public void setConvertedRule(Map<String, List<String>> convertedRule) {
        this.convertedRule = convertedRule;
    }
}

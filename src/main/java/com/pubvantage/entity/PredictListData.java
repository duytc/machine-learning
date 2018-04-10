package com.pubvantage.entity;

import java.util.List;
import java.util.Map;

/**
 * Created by quyendq on 02/04/2018.
 */
public class PredictListData {
    private List<Map<String, String>> segmentGroups;
    private List<String> segmentGroupJson;
    private List<String> identifiers;
    private List<OptimizeField> optimizeFields;
    private List<String> listDate;
    private Long ruleId;

    public PredictListData() {
    }

    public String getFutureDate() {
        if (listDate != null && !listDate.isEmpty())
            return listDate.get(listDate.size() - 1);
        return null;
    }

    public List<Map<String, String>> getSegmentGroups() {
        return segmentGroups;
    }

    public void setSegmentGroups(List<Map<String, String>> segmentGroups) {
        this.segmentGroups = segmentGroups;
    }

    public List<String> getSegmentGroupJson() {
        return segmentGroupJson;
    }

    public void setSegmentGroupJson(List<String> segmentGroupJson) {
        this.segmentGroupJson = segmentGroupJson;
    }

    public PredictListData(List<Map<String, String>> segmentGroups,
                           List<String> segmentGroupJson,
                           List<String> identifiers,
                           List<OptimizeField> optimizeFields,
                           List<String> listDate,
                           Long ruleId) {
        this.segmentGroups = segmentGroups;
        this.segmentGroupJson = segmentGroupJson;
        this.identifiers = identifiers;
        this.optimizeFields = optimizeFields;
        this.listDate = listDate;
        this.ruleId = ruleId;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }


    public List<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<String> identifiers) {
        this.identifiers = identifiers;
    }

    public List<OptimizeField> getOptimizeFields() {
        return optimizeFields;
    }

    public void setOptimizeFields(List<OptimizeField> optimizeFields) {
        this.optimizeFields = optimizeFields;
    }

    public List<String> getListDate() {
        return listDate;
    }

    public void setListDate(List<String> listDate) {
        this.listDate = listDate;
    }
}

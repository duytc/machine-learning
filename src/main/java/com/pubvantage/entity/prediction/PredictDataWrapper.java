package com.pubvantage.entity.prediction;

public class PredictDataWrapper {
    private String optimizeFieldJson;
    private String identifier;
    private String segmentJson;
    private String date;
    private boolean isPredict;
    private Long optimizeRuleId;


    public PredictDataWrapper() {
    }

    public PredictDataWrapper(String optimizeFieldJson, String identifier, String segmentJson, String date,
                              boolean isPredict, Long optimizeRuleId) {
        this.optimizeFieldJson = optimizeFieldJson;
        this.identifier = identifier;
        this.segmentJson = segmentJson;
        this.date = date;
        this.isPredict = isPredict;
        this.optimizeRuleId = optimizeRuleId;
    }

    public String getOptimizeFieldJson() {
        return optimizeFieldJson;
    }

    public void setOptimizeFieldJson(String optimizeFieldJson) {
        this.optimizeFieldJson = optimizeFieldJson;
    }

    public String getSegmentJson() {
        return segmentJson;
    }

    public void setSegmentJson(String segmentJson) {
        this.segmentJson = segmentJson;
    }

    public boolean isPredict() {
        return isPredict;
    }

    public void setPredict(boolean predict) {
        isPredict = predict;
    }


    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean getIsPredict() {
        return isPredict;
    }

    public void setIsPredict(boolean predict) {
        isPredict = predict;
    }

    public Long getOptimizeRuleId() {
        return optimizeRuleId;
    }

    public void setOptimizeRuleId(Long optimizeRuleId) {
        this.optimizeRuleId = optimizeRuleId;
    }
}

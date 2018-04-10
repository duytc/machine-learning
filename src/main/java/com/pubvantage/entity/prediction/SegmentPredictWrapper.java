package com.pubvantage.entity.prediction;

import java.util.Map;

public class SegmentPredictWrapper {
    Map<String, Map<String, Map<String, Double>>> dateAndIdentifierAndOptimizePredict;
    Map<String, Map<String, Boolean>> noHistoryIdentifier;

    public SegmentPredictWrapper() {
    }

    public SegmentPredictWrapper(Map<String, Map<String, Map<String, Double>>> dateAndIdentifierAndOptimizePredict,
                                 Map<String, Map<String, Boolean>> noHistoryIdentifier) {
        this.dateAndIdentifierAndOptimizePredict = dateAndIdentifierAndOptimizePredict;
        this.noHistoryIdentifier = noHistoryIdentifier;
    }

    public Map<String, Map<String, Map<String, Double>>> getDateAndIdentifierAndOptimizePredict() {
        return dateAndIdentifierAndOptimizePredict;
    }

    public void setDateAndIdentifierAndOptimizePredict(Map<String, Map<String, Map<String, Double>>> dateAndIdentifierAndOptimizePredict) {
        this.dateAndIdentifierAndOptimizePredict = dateAndIdentifierAndOptimizePredict;
    }

    public Map<String, Map<String, Boolean>> getNoHistoryIdentifier() {
        return noHistoryIdentifier;
    }

    public void setNoHistoryIdentifier(Map<String, Map<String, Boolean>> noHistoryIdentifier) {
        this.noHistoryIdentifier = noHistoryIdentifier;
    }
}

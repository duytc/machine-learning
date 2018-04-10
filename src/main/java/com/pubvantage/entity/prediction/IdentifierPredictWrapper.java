package com.pubvantage.entity.prediction;

import java.util.Map;

public class IdentifierPredictWrapper {
    Map<String, Map<String, Double>> dateOptimizePredict;
    Map<String, Boolean> noHistoryDate;

    public IdentifierPredictWrapper() {
    }

    public IdentifierPredictWrapper(Map<String, Map<String, Double>> dateOptimizePredict,
                                    Map<String, Boolean> noHistoryDate) {
        this.dateOptimizePredict = dateOptimizePredict;
        this.noHistoryDate = noHistoryDate;
    }

    public Map<String, Map<String, Double>> getDateOptimizePredict() {
        return dateOptimizePredict;
    }

    public void setDateOptimizePredict(Map<String, Map<String, Double>> dateOptimizePredict) {
        this.dateOptimizePredict = dateOptimizePredict;
    }

    public Map<String, Boolean> getNoHistoryDate() {
        return noHistoryDate;
    }

    public void setNoHistoryDate(Map<String, Boolean> noHistoryDate) {
        this.noHistoryDate = noHistoryDate;
    }
}

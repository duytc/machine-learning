package com.pubvantage.service;

import com.pubvantage.entity.CoreOptimizationRule;

import java.util.Map;

public interface ScoreServiceInterface {
    void saveScore(Map<String, Map<String, Map<String, Map<String, Double>>>> scoreMap,
                   CoreOptimizationRule coreOptimizationRule,
                   String futureDate);
}

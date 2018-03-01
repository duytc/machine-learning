package com.pubvantage.service;


import com.google.gson.JsonObject;
import com.pubvantage.entity.CoreAutoOptimizationConfig;

import java.util.List;

public class CoreOptimizationRuleService implements CoreOptimizationRuleServiceInterface {
    @Override
    public String[] getSegmentFields(long optimizationRuleId) {
        return new String[0];
    }

    @Override
    public String[] getOptimizeFields(long optimizationRuleId) {
        return new String[0];
    }

    @Override
    public String[] getMetrics(long optimizationRuleId) {
        return new String[0];
    }

    @Override
    public String[] getIdentifiers(long optimizationRuleId) {
        return null;
    }

    @Override
    public boolean checkToken(long autoOptimizationConfigId, String token) {
        return false;
    }

    @Override
    public List<String> getFactors(Long id) {
        return null;
    }

    @Override
    public JsonObject getFieldType(Long id) {
        return null;
    }

    @Override
    public CoreAutoOptimizationConfig findById(Long autoOptimizationConfigId) {
        return null;
    }

    @Override
    public String[] getObjectiveAndFactors(long autoOptimizationId) {
        return new String[0];
    }

    @Override
    public List<String> getPositiveFactors(long autoOptimizationId) {
        return null;
    }

    @Override
    public List<String> getNegativeFactors(long autoOptimizationId) {
        return null;
    }

}

package com.pubvantage.service;

import com.google.gson.JsonObject;
import com.pubvantage.entity.CoreAutoOptimizationConfig;

import java.util.List;

public interface CoreAutoOptimizationConfigServiceInterface {


    String[] getObjectiveAndFactors(Long autoOptimizationId);

    JsonObject getFieldType(Long autoOptimizationId);

    List<String> getPositiveFactors(Long autoOptimizationId);

    List<String> getNegativeFactors(Long autoOptimizationId);

    boolean checkToken(Long autoOptimizationConfigId, String token);

    List<String> getFactors(Long autoOptimizationId);

    CoreAutoOptimizationConfig findById(Long autoOptimizationId);
}

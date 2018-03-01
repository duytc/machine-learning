package com.pubvantage.ConditionProcessor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubvantage.RestParams.FactorConditionData;
import com.pubvantage.entity.CoreAutoOptimizationConfig;
import com.pubvantage.service.OptimizationRuleService;
import com.pubvantage.service.DataTrainingServiceOld;
import com.pubvantage.service.DataTrainingServiceInterface;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConditionGenerator {

    private OptimizationRuleService optimizationConfigService = new OptimizationRuleService();
    private DataTrainingServiceInterface trainingService = new DataTrainingServiceOld();
    private CoreAutoOptimizationConfig coreAutoOptimizationConfig;
    private JsonArray conditions;


    public ConditionGenerator(CoreAutoOptimizationConfig coreAutoOptimizationConfig, JsonArray conditions) {
        this.coreAutoOptimizationConfig = coreAutoOptimizationConfig;
        this.conditions = conditions;
    }

    /**
     * @return list filtered factor condition data. exclude factor that is not in database
     */
    private List<FactorConditionData> validateConditions() {
        List<String> factorsFromDB = optimizationConfigService.getFactors(coreAutoOptimizationConfig.getId());

        if (factorsFromDB == null || factorsFromDB.isEmpty()) {
            return new ArrayList<>();
        }
        return filterFactors(conditions, factorsFromDB);
    }

    /**
     * @param conditions    condition data
     * @param factorsFromDB list factors from database
     * @return list filtered factor condition data. exclude factor that is not in database
     */
    private List<FactorConditionData> filterFactors(JsonArray conditions, List<String> factorsFromDB) {
        List<FactorConditionData> factorConditionDataList = new ArrayList<>();

        for (JsonElement factorData : conditions) {
            JsonObject factorDataJsonObject = factorData.getAsJsonObject();
            FactorConditionData factorConditionData = new Gson().fromJson(factorDataJsonObject, FactorConditionData.class);
            if (factorConditionData == null)
                continue;

            String factorName = factorConditionData.getFactor();
            if (null == factorName || factorName.isEmpty())
                continue;

            if (factorsFromDB.contains(factorName)) {
                factorConditionDataList.add(factorConditionData);
            }
        }
        return factorConditionDataList;
    }

    /**
     * @return multiple condition data
     */
    public List<Map<String, Object>> generateMultipleConditions() {
        List<FactorConditionData> factorConditionDataList = validateConditions();
        LinkedHashMap<String, List<Object>> filteredConditionsDataMap = trainingService.getFilteredConditions(coreAutoOptimizationConfig.getId(), factorConditionDataList);
        List<List<Object>> filteredConditionsDataList = new ArrayList<>(filteredConditionsDataMap.values());

        List<List<Object>> multipleConditionList = generateListConditions(filteredConditionsDataList);

        List<String> listFactorName = new ArrayList<>();
        filteredConditionsDataMap.forEach((factorName, value) -> listFactorName.add(factorName));
        List<Map<String, Object>> multipleConditionMap = new ArrayList<>();
        for (List<Object> conditionList : multipleConditionList) {
            Map<String, Object> conditionMap = new LinkedHashMap<>();
            for (int i = 0; i < conditionList.size(); i++) {
                conditionMap.put(listFactorName.get(i), conditionList.get(i));
            }
            multipleConditionMap.add(conditionMap);
        }

        return multipleConditionMap;
    }

    /**
     * @param listConditionData list full factor condition data
     * @return multiple condition data in List type
     */
    private List<List<Object>> generateListConditions(List<List<Object>> listConditionData) {
        List<List<Object>> resultLast = new ArrayList<>();
        List<Object> resultTemp = new ArrayList<>();
        int size = listConditionData.size();
        backTrack(0, resultTemp, size, listConditionData, resultLast);
        return resultLast;
    }

    /**
     * @param index             step of back tracking
     * @param resultTemp        result of each step
     * @param size              size of a result
     * @param listConditionData condition data
     * @param resultLast        final result
     */
    private void backTrack(int index, List<Object> resultTemp, int size, List<List<Object>> listConditionData, List<List<Object>> resultLast) {
        if (index == size) {
            List<Object> list = new ArrayList<>(resultTemp);
            resultLast.add(list);
            return;
        }
        List<Object> valueByFactor = listConditionData.get(index);

        for (int i = 0; i < valueByFactor.size(); i++) {
            resultTemp.add(valueByFactor.get(i));
            index++;
            backTrack(index, resultTemp, size, listConditionData, resultLast);
            index--;
            resultTemp.remove(index);
        }
    }
}

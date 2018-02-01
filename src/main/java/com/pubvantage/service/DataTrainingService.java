package com.pubvantage.service;

import com.google.gson.JsonObject;
import com.pubvantage.RestParams.FactorConditionData;
import com.pubvantage.dao.SparkDataTrainingDao;
import com.pubvantage.dao.SparkDataTrainingDaoInterface;
import com.pubvantage.utils.ConvertUtil;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.LinkedHashMap;
import java.util.List;

public class DataTrainingService implements DataTrainingServiceInterface {
    private SparkDataTrainingDaoInterface sparkDataTrainingDaoInterface = new SparkDataTrainingDao();

    /**
     * @param autoOptimizationConfigId auto optimization config id
     * @param identifier               identifier
     * @param objectiveAndFactor       training data from database
     */
    @Override
    public Dataset<Row> getDataSetByTable(Long autoOptimizationConfigId, String identifier, String[] objectiveAndFactor) {
        return sparkDataTrainingDaoInterface.getDataSetByTable(autoOptimizationConfigId, identifier, objectiveAndFactor);
    }

    /**
     * @param autoOptimizationConfigId auto optimization config id
     * @param conditionDataList        conditions data
     * @return data contains set of values for factors
     */
    @Override
    public LinkedHashMap<String, List<Object>> getFilteredConditions(Long autoOptimizationConfigId, List<FactorConditionData> conditionDataList) {
        CoreAutoOptimizationConfigServiceInterface coreAutoOptimizationConfigService = new CoreAutoOptimizationConfigService();
        JsonObject fieldType = coreAutoOptimizationConfigService.getFieldType(autoOptimizationConfigId);

        LinkedHashMap<String, List<Object>> filteredConditions = new LinkedHashMap<>();
        if (conditionDataList == null || conditionDataList.isEmpty())
            return filteredConditions;

        for (FactorConditionData conditionData : conditionDataList) {
            String factorName = conditionData.getFactor();
            //text factors
            if (ConvertUtil.isTextOrDate(fieldType.get(factorName).getAsString())) {
                List<Object> values = filterTextValueCondition(conditionData, autoOptimizationConfigId);
                filteredConditions.put(factorName, values);
            } else {
                List<Object> values = filterNumberValueCondition(conditionData);
                filteredConditions.put(factorName, values);
            }
        }

        return filteredConditions;
    }

    /**
     * @param conditionData condition data
     * @return list of values for number type factor
     */
    private List<Object> filterNumberValueCondition(FactorConditionData conditionData) {
        boolean isAll = conditionData.getIsAll();
        if (isAll) {
            return null;
        }
        return conditionData.getValues();
    }

    /**
     * @param conditionData            condition data
     * @param autoOptimizationConfigId auto optimization config id
     * @return list of values for text type factor
     */
    private List<Object> filterTextValueCondition(FactorConditionData conditionData, Long autoOptimizationConfigId) {
        boolean isAll = conditionData.getIsAll();
        if (!isAll) {
            return conditionData.getValues();
        }
        return getDistinctFromDB(conditionData.getFactor(), autoOptimizationConfigId);
    }

    /**
     * @param factorName               factor's name
     * @param autoOptimizationConfigId auto optimization config id
     * @return list of different values of factor from database
     */
    private List<Object> getDistinctFromDB(String factorName, Long autoOptimizationConfigId) {
        return sparkDataTrainingDaoInterface.getDistinctByFactor(factorName, autoOptimizationConfigId);
    }

    /**
     * @param autoOptimizationConfigId auto optimization config id
     * @return list of identifiers
     */
    @Override
    public String[] getIdentifiers(Long autoOptimizationConfigId) {
        List<Row> resultList = sparkDataTrainingDaoInterface.getIdentifiers(autoOptimizationConfigId);

        String[] identifiers = new String[0];

        int index = 0;
        if (resultList != null && !resultList.isEmpty()) {
            int size = resultList.size();
            identifiers = new String[size];
            for (Row aResultList : resultList) {
                identifiers[index++] = aResultList.get(0).toString();
            }
        }

        return identifiers;
    }

}

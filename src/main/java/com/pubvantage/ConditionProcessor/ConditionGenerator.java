package com.pubvantage.ConditionProcessor;

import com.pubvantage.RestParams.FactorConditionData;
import com.pubvantage.entity.Condition;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.FactorValues;
import com.pubvantage.entity.SegmentField;
import com.pubvantage.service.DataTrainingServiceInterface;
import com.pubvantage.service.DataTrainingServiceOld;
import com.pubvantage.service.OptimizationRuleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConditionGenerator {

    private OptimizationRuleService optimizationConfigService = new OptimizationRuleService();
    private DataTrainingServiceInterface trainingService = new DataTrainingServiceOld();
    private CoreOptimizationRule coreOptimizationRule;
    private Condition conditions;

    public ConditionGenerator(CoreOptimizationRule coreOptimizationRule, Condition conditions) {
        this.coreOptimizationRule = coreOptimizationRule;
        this.conditions = conditions;
    }

    /**
     * @return list filtered factor condition data. exclude factor that is not in database
     */
    private List<FactorConditionData> validateConditions() {
        return null;
    }

    /**
     * @return multiple condition data
     */
    public List<Map<String, Object>> generateMultipleSegmentGroupValues() {

        return null;
    }

    public FactorValues getFactorValues() {
        return this.conditions.getFactorValues();
    }

    public List<String> getInputSegmentFields() {

        return null;
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

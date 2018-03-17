package com.pubvantage.conditionprocessor;

import com.pubvantage.entity.Condition;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.FactorValues;
import com.pubvantage.entity.SegmentField;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConditionGenerator {

    private Condition conditions;

    public ConditionGenerator(Condition conditions) {
        this.conditions = conditions;
    }

    /**
     * @return multiple condition data
     */
    public List<Map<String, Object>> generateMultipleSegmentGroupValues() {
        boolean isGlobal = this.conditions.getGlobal();
        if (isGlobal) {
            return null;
        }

        List<SegmentField> segmentFieldList = this.conditions.getSegmentFields();
        List<List<Object>> filteredConditionsDataList = new ArrayList();
        for (SegmentField segmentField : segmentFieldList) {
            filteredConditionsDataList.add(segmentField.getValues());
        }

        List<List<Object>> multipleConditionList = generateListConditions(filteredConditionsDataList);

        List<String> listSegmentField = new ArrayList<>();
        segmentFieldList.forEach(segmentField -> listSegmentField.add(segmentField.getSegmentField()));

        List<Map<String, Object>> multipleConditionMap = new ArrayList<>();
        for (List<Object> conditionList : multipleConditionList) {
            Map<String, Object> conditionMap = new LinkedHashMap<>();
            for (int i = 0; i < conditionList.size(); i++) {
                conditionMap.put(listSegmentField.get(i), conditionList.get(i));
            }
            multipleConditionMap.add(conditionMap);
        }
        return multipleConditionMap;
    }

    public FactorValues getFactorValues() {
        return this.conditions.getFactorValues();
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

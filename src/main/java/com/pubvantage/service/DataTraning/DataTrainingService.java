package com.pubvantage.service.DataTraning;

import com.pubvantage.dao.SparkDataTrainingDao;
import com.pubvantage.entity.CoreOptimizationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataTrainingService implements DataTrainingServiceInterface {
    private Long optimizationRuleId;
    private String identifier;
    private List<String> oneSegmentFieldGroup;
    SparkDataTrainingDao sparkDataTrainingDao = new SparkDataTrainingDao();

    public DataTrainingService() {
    }

    public DataTrainingService(Long optimizationRuleId, String identifier, List<String> oneSegmentFieldGroup) {
        this.optimizationRuleId = optimizationRuleId;
        this.identifier = identifier;
        this.oneSegmentFieldGroup = oneSegmentFieldGroup;
    }


    @Override
    public Long getOptimizationRuleId() {
        return null;
    }

    @Override
    public void setOptimizationRuleId(Long optimizationRuleId) {

    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public void setIdentifier(String identifier) {

    }

    @Override
    public List<String> getOneSegmentFieldGroup() {
        return null;
    }

    @Override
    public void setOneSegmentFieldGroup(List<String> oneSegmentFieldGroup) {

    }

    @Override
    public List<Map<String, Object>> getAllUniqueValuesForOneSegmentFieldGroup() {
        List<Map<String, Object>> objects = sparkDataTrainingDao.getAllUniqueValuesForOneSegmentFieldGroup(optimizationRuleId, identifier, oneSegmentFieldGroup);
        return objects;
    }

    @Override
    public List<Double> getVectorData(List<String> optimizeFieldAndMetrics, CoreOptimizationRule optimizationRule, String dateValue) {
        return sparkDataTrainingDao.getVectorData(optimizeFieldAndMetrics, optimizationRule,dateValue);
    }
}

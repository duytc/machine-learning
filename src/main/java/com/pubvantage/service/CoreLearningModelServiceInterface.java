package com.pubvantage.service;

import com.pubvantage.entity.CoreAutoOptimizationConfig;
import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.CoreLearningModel;
import com.pubvantage.entity.OptimizeField;
import org.hibernate.Session;

import java.util.List;
import java.util.Map;

public interface CoreLearningModelServiceInterface {

    void saveListLearnerModel(List<CoreLearner> modelList);

    CoreLearner getOneCoreLeaner(Long optimizationRule, String identifier, OptimizeField optimizeFields, Map<String, Object> segmentValues);



    List<CoreLearner> findListByRuleId(Long autOptimizationConfigId);

    List<Object> getDistinctSegmentsByRuleId(Long optimizationRuleId);

    List<String> getDistinctIdentifiersBySegment(Map<String, Object> segments, Long optimizationRuleId);

    List<OptimizeField> getDistinctOptimizeBySegmentAndIdentifier(Map<String, Object> segments, String identifier, Long optimizationRuleId);

}

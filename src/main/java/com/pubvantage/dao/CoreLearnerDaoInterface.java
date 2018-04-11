package com.pubvantage.dao;

import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.OptimizeField;
import org.hibernate.Session;

import java.util.List;

public interface CoreLearnerDaoInterface extends GenericDaoInterface<CoreLearner> {

    CoreLearner getOne(Session session, Long optimizationRuleId, String identifier, String optimizeFieldJson, String segmentGroup);

    List<String> getDistinctIdentifiers(Session session, Long optimizationRuleId, String segmentGroup);

    String getTextSegmentConvertedRule(Session session, Long optimizationRuleId, String segmentGroup, String identifier);

    List<String> getDistinctSegmentValues(Session session, Long optimizationRuleId);

    List<String> getOptimizeFieldsJson(Session session, Long optimizationRuleId);

    boolean deleteAllByRuleId(Session session, Long optimizationRuleId);

}

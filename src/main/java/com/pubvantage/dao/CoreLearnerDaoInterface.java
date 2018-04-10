package com.pubvantage.dao;

import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.OptimizeField;
import org.hibernate.Session;

import java.util.List;

public interface CoreLearnerDaoInterface extends GenericDaoInterface<CoreLearner> {
    // TODO able to refactor getList optimize field
    List<CoreLearner> getList(Session session, Long autOptimizationConfigId, String identifier, String segmentGroup);

    List<String> getDistinctIdentifiers(Session session, Long optimizationRuleId, String segmentGroup);

    List<String> getIdentifiersBySegmentGroup(Session session, Long optimizationRuleId, String segmentGroup);

    String getTextSegmentConvertedRule(Session session, Long optimizationRuleId, String segmentGroup, String identifier);

    List<String> getDistinctSegmentValues(Session session, Long optimizationRuleId);

    List<OptimizeField> getOptimizeFields(Session session, Long optimizationRuleId);

    boolean deleteAllByRuleId(Session session, Long optimizationRuleId);

}

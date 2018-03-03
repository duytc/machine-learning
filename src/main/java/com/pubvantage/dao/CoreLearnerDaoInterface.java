package com.pubvantage.dao;

import com.pubvantage.entity.CoreLearner;
import org.hibernate.Session;

import java.util.List;
import java.util.Map;

public interface CoreLearnerDaoInterface extends GenericDaoInterface<CoreLearner> {
    CoreLearner findOne(Session session, Long autOptimizationConfigId, String identifier, String segmentValues, String optimizeField);

    CoreLearner findOne(Session session, Long autOptimizationConfigId, String identifier, Map<String, Object> segmentValues, String optimizeField);

    List<CoreLearner> findList(Session session, Long autOptimizationConfigId, String identifier);
}

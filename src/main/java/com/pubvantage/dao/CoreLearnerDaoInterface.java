package com.pubvantage.dao;

import com.pubvantage.entity.CoreLearner;
import org.hibernate.Session;

public interface CoreLearnerDaoInterface extends GenericDaoInterface<CoreLearner> {
    CoreLearner findOne(Session session, Long autOptimizationConfigId, String identifier);
}

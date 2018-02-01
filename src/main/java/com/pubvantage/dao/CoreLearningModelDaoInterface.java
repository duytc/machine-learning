package com.pubvantage.dao;

import com.pubvantage.entity.CoreLearningModel;
import org.hibernate.Session;

public interface CoreLearningModelDaoInterface extends GenericDaoInterface<CoreLearningModel> {

    CoreLearningModel findOne(Session session, Long autOptimizationConfigId, String identifier);

    String getModelPath(Session session, Long autOptimizationConfigId, String identifier);
}

package com.pubvantage.dao;

import com.pubvantage.entity.CoreLearningModel;
import org.hibernate.Session;

public interface DataTrainingDAO {
    boolean saveModel(Session session, CoreLearningModel model);

    CoreLearningModel findModelByIdentifierAndAutoOptimizationId(Session session, CoreLearningModel model);
}

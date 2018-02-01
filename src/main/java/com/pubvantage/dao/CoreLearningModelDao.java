package com.pubvantage.dao;

import com.pubvantage.entity.CoreLearningModel;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.List;

public class CoreLearningModelDao extends AbstractGenericDao<CoreLearningModel> implements CoreLearningModelDaoInterface {
    private static Logger logger = Logger.getLogger(CoreLearningModelDao.class.getName());

    /**
     * @param session                 hibernate session
     * @param autOptimizationConfigId auto optimization id
     * @param identifier              identifier
     * @return data from core_learner_model table
     */
    @Override
    public CoreLearningModel findOne(Session session, Long autOptimizationConfigId, String identifier) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("FROM CoreLearningModel WHERE autoOptimizationConfigId = :autoOptimizationConfigId AND identifier = :identifier");

            Query query = session.createQuery(stringBuilder.toString());
            query.setParameter("autoOptimizationConfigId", autOptimizationConfigId);
            query.setParameter("identifier", identifier);

            List<CoreLearningModel> list = query.list();
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    @Override
    public String getModelPath(Session session, Long autOptimizationConfigId, String identifier) {
        CoreLearningModel coreLearningModel = findOne(session, autOptimizationConfigId, identifier);
        return coreLearningModel.getModelPath();
    }

}

package com.pubvantage.dao;

import com.pubvantage.entity.CoreLearner;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CoreLearnerDao extends AbstractGenericDao<CoreLearner> implements CoreLearnerDaoInterface {
    private static Logger logger = Logger.getLogger(CoreLearningModelDao.class.getName());

    @Override
    public CoreLearner findOne(Session session, Long optimizationRuleId, String identifier, String segmentValues, String optimizeField) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("FROM CoreLearner WHERE optimizationRuleId = :optimizationRuleId AND identifier = :identifier AND segmentValues = :segmentValues AND optimizeFields = :optimizeFields");

            Query query = session.createQuery(stringBuilder.toString());
            query.setParameter("optimizationRuleId", optimizationRuleId);
            query.setParameter("identifier", identifier);
            query.setParameter("segmentValues", segmentValues);
            query.setParameter("optimizeFields", segmentValues);

            List<CoreLearner> list = query.list();
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    @Override
    public CoreLearner findOne(Session session, Long autOptimizationConfigId, String identifier, Map<String, Object> segmentValues, String optimizeField) {
        return null;
    }

    @Override
    public List<CoreLearner> findList(Session session, Long optimizationRuleId, String identifier) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("FROM CoreLearner WHERE optimizationRuleId = :optimizationRuleId AND identifier = :identifier");

            Query query = session.createQuery(stringBuilder.toString());
            query.setParameter("optimizationRuleId", optimizationRuleId);
            query.setParameter("identifier", identifier);
            List<CoreLearner> list = query.list();
            if (list != null && !list.isEmpty()) {
                return list;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return new ArrayList<>();
    }
}

package com.pubvantage.dao;

import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.utils.JsonUtil;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class CoreLearnerDao extends AbstractGenericDao<CoreLearner> implements CoreLearnerDaoInterface {
    private static Logger logger = Logger.getLogger(CoreLearningModelDao.class.getName());

    @Override
    public List<CoreLearner> getList(Session session, Long optimizationRuleId, String identifier, String segmentGroup) {
        try {
            String stringBuilder = "FROM CoreLearner WHERE optimizationRuleId = :optimizationRuleId AND identifier = :identifier AND segmentValues = :segmentGroup";
            Query query = session.createQuery(stringBuilder);
            query.setParameter("optimizationRuleId", optimizationRuleId);
            query.setParameter("identifier", identifier);
            query.setParameter("segmentGroup", segmentGroup);
            List<CoreLearner> list = query.list();
            if (list != null && !list.isEmpty()) {
                return list;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }


    @Override
    public List<String> getDistinctIdentifiers(Session session, Long optimizationRuleId, String segmentGroup) {
        try {
            String string = "SELECT DISTINCT identifier FROM CoreLearner WHERE optimizationRuleId = :optimizationRuleId AND segmentValues = :segmentGroup";
            Query query = session.createQuery(string);
            query.setParameter("optimizationRuleId", optimizationRuleId);
            query.setParameter("segmentGroup", segmentGroup);
            List<String> list = query.list();
            if (list != null && !list.isEmpty()) {
                return list;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<String> getIdentifiersBySegmentGroup(Session session, Long optimizationRuleId, String segmentGroup) {
        try {
            String string = "SELECT DISTINCT identifier FROM CoreLearner WHERE optimizationRuleId = :optimizationRuleId AND segmentValues = :segmentValues";
            Query query = session.createQuery(string);
            query.setParameter("optimizationRuleId", optimizationRuleId);
            query.setParameter("segmentValues", segmentGroup);
            List<String> list = query.list();
            if (list != null && !list.isEmpty()) {
                return list;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String getTextSegmentConvertedRule(Session session, Long optimizationRuleId, String segmentGroup, String identifier) {
        try {
            String string = "SELECT DISTINCT textConvertedRule FROM CoreLearner WHERE optimizationRuleId = :optimizationRuleId AND identifier = :identifier AND segmentValues = :segmentGroup";
            Query query = session.createQuery(string);
            query.setParameter("optimizationRuleId", optimizationRuleId);
            query.setParameter("identifier", identifier);
            query.setParameter("segmentGroup", segmentGroup);
            List<String> list = query.list();
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<String> getDistinctSegmentValues(Session session, Long optimizationRuleId) {
        try {
            String string = "SELECT DISTINCT segmentValues FROM CoreLearner WHERE optimizationRuleId = :optimizationRuleId";
            Query query = session.createQuery(string);
            query.setParameter("optimizationRuleId", optimizationRuleId);
            return (List<String>) query.list();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<OptimizeField> getOptimizeFields(Session session, Long optimizationRuleId) {
        try {
            String string = "SELECT DISTINCT optimizeFields FROM CoreLearner WHERE optimizationRuleId = :optimizationRuleId";
            Query query = session.createQuery(string);
            query.setParameter("optimizationRuleId", optimizationRuleId);
            List<String> list = query.list();
            if (list != null && !list.isEmpty()) {
                List<OptimizeField> listOptimizeField = new ArrayList<>();
                for (String json : list) {
                    OptimizeField optimizeField = JsonUtil.jsonToObject(json, OptimizeField.class);
                    listOptimizeField.add(optimizeField);
                }
                return listOptimizeField;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean deleteAllByRuleId(Session session, Long optimizationRuleId) {
        try {
            String string = "DELETE FROM CoreLearner WHERE optimizationRuleId = :optimizationRuleId";
            Query query = session.createQuery(string);
            query.setParameter("optimizationRuleId", optimizationRuleId);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }


}

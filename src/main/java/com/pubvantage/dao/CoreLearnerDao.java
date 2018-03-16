package com.pubvantage.dao;

import com.google.gson.JsonObject;
import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.entity.SegmentAndIOptimizeField;
import com.pubvantage.entity.SegmentAndIdentifier;
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

    @Override
    public List<CoreLearner> findListByRuleId(Session session, Long optimizationRuleId) {
        try {
            String string = "FROM CoreLearner WHERE optimizationRuleId = :optimizationRuleId";
            Query query = session.createQuery(string);
            query.setParameter("optimizationRuleId", optimizationRuleId);
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
    public List<Object> getDistinctSegmentsByRuleId(Session session, Long optimizationRuleId) {
        try {
            String string = "SELECT DISTINCT segment_values FROM core_learner WHERE optimization_rule_id = :optimizationRuleId";
            Query query = session.createSQLQuery(string);
            query.setParameter("optimizationRuleId", optimizationRuleId);
            List<Object> list = query.list();
            if (list != null && !list.isEmpty()) {
                return list;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<SegmentAndIdentifier> getDistinctIdentifiersByRuleId(Session session, Long optimizationRuleId) {
        try {
            String string = "SELECT DISTINCT identifier, segment_values FROM core_learner WHERE optimization_rule_id = :optimizationRuleId";
            Query query = session.createSQLQuery(string);
            query.setParameter("optimizationRuleId", optimizationRuleId);
            List<SegmentAndIdentifier> resultlist = new ArrayList<>();
            List<Object[]> list = query.list();
            if (list != null && !list.isEmpty()) {
                for (Object[] object : list) {
                    String identifier = object[0] == null ? null : object[0].toString();
                    String segment = object[1] == null ? null : object[1].toString();
                    SegmentAndIdentifier segmentAndIdentifier = new SegmentAndIdentifier(identifier, segment);
                    resultlist.add(segmentAndIdentifier);
                }
            }
            return resultlist;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<SegmentAndIOptimizeField> getDistinctOptimizeByRuleIdAndIdentifier(Session session, String identifier, Long optimizationRuleId) {
        try {
            String string = "SELECT DISTINCT optimize_field, segment_values FROM core_learner WHERE optimization_rule_id = :optimizationRuleId AND identifier = :identifier";
            Query query = session.createSQLQuery(string);
            query.setParameter("optimizationRuleId", optimizationRuleId);
            query.setParameter("identifier", identifier);
            List<Object[]> list = query.list();
            List<SegmentAndIOptimizeField> resultlist = new ArrayList<>();
            if (list != null && !list.isEmpty()) {
                for (Object[] object : list) {
                    String optimizeField = object[0] == null ? null : object[0].toString();
                    String segment = object[1] == null ? null : object[1].toString();
                    SegmentAndIOptimizeField segmentAndIdentifier = new SegmentAndIOptimizeField(optimizeField, segment);
                    resultlist.add(segmentAndIdentifier);
                }
            }
            return resultlist;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}

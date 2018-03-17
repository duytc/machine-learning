package com.pubvantage.service;

import com.google.gson.JsonObject;
import com.pubvantage.dao.CoreLearnerDao;
import com.pubvantage.dao.CoreLearnerDaoInterface;
import com.pubvantage.dao.CoreLearningModelDao;
import com.pubvantage.dao.CoreLearningModelDaoInterface;
import com.pubvantage.entity.*;
import com.pubvantage.utils.HibernateUtil;
import com.pubvantage.utils.JsonUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CoreLearningModelService implements CoreLearningModelServiceInterface {
    private static Logger logger = Logger.getLogger(CoreLearningModelService.class.getName());
    private CoreLearningModelDaoInterface coreLearningModelDAO = new CoreLearningModelDao();
    private CoreLearnerDaoInterface coreLearnerDao = new CoreLearnerDao();

    @Override
    public void saveListLearnerModel(List<CoreLearner> modelList) {
        if (modelList == null || modelList.isEmpty()) {
            return;
        }
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            for (CoreLearner aModelList : modelList) {
                if (aModelList == null) {
                    continue;
                }
                OptimizeField optimizeField = JsonUtil.jsonToObject(aModelList.getOptimizeFields(), OptimizeField.class);
                Map<String, Object> segmentValues = JsonUtil.jsonToMap(aModelList.getSegmentValues());
                CoreLearner foundModel = this.checkExist(session,
                        aModelList.getOptimizationRuleId(),
                        aModelList.getIdentifier(),
                        optimizeField
                        , segmentValues);
                if (null == foundModel || foundModel.getId() == null) {
                    //add new
                    aModelList.setCreatedDate(new Date());
                    aModelList.setUpdatedDate(new Date());
                    coreLearnerDao.save(aModelList, session);
                } else {
                    //update
                    foundModel.setMathModel(aModelList.getMathModel());
                    foundModel.setModelPath(aModelList.getModelPath());
                    foundModel.setMetricsPredictiveValues(aModelList.getMetricsPredictiveValues());
                    foundModel.setSegmentValues(aModelList.getSegmentValues());
                    foundModel.setOptimizeFields(aModelList.getOptimizeFields());
                    foundModel.setUpdatedDate(new Date());

                    coreLearnerDao.save(foundModel, session);
                }
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            if (null != session && null != session.getTransaction()) {
                session.getTransaction().rollback();
            }
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public CoreLearner getOneCoreLeaner(Long optimizationRuleId, String identifier, OptimizeField optimizeField, Map<String, Object> segmentValues) {
        Session session = null;
        CoreLearner coreLearner = new CoreLearner();
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            coreLearner = this.checkExist(session, optimizationRuleId, identifier, optimizeField, segmentValues);

            session.clear();
            session.getTransaction().commit();
        } catch (Exception e) {
            if (null != session && null != session.getTransaction()) {
                session.getTransaction().rollback();
            }
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return coreLearner;
    }


    private CoreLearner checkExist(Session session, Long optimizationRuleId, String identifier, OptimizeField optimizeField, Map<String, Object> segmentValues) {
        CoreLearner coreLearner = new CoreLearner();
        List<CoreLearner> coreLearners = coreLearnerDao.findList(session, optimizationRuleId, identifier);
        for (CoreLearner coreLearnerFromDB : coreLearners) {
            OptimizeField optimizeFieldFromDB = JsonUtil.jsonToObject(coreLearnerFromDB.getOptimizeFields(), OptimizeField.class);
            if (!(optimizeFieldFromDB.getField().equals(optimizeField.getField()))) {
                continue;
            }

            Map<String, Object> segmentValuesFromDB = JsonUtil.jsonToMap(coreLearnerFromDB.getSegmentValues());
            //run global
            if (segmentValuesFromDB == null && segmentValues == null) {
                return coreLearnerFromDB;
            }
            if (segmentValuesFromDB != null && !segmentValuesFromDB.isEmpty()) {
                if (segmentValuesFromDB.equals(segmentValues)) {
                    return coreLearnerFromDB;
                }
            }
        }
        return coreLearner;
    }

    @Override
    public List<CoreLearner> findListByRuleId(Long optimizeRuleId) {
        Session session = null;
        List<CoreLearner> coreLearnerList = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            coreLearnerList = coreLearnerDao.findListByRuleId(session, optimizeRuleId);
            session.clear();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return coreLearnerList;
    }

    @Override
    public List<Object> getDistinctSegmentsByRuleId(Long optimizationRuleId) {
        Session session = null;
        List<Object> coreLearnerList = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            coreLearnerList = coreLearnerDao.getDistinctSegmentsByRuleId(session, optimizationRuleId);
            session.clear();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return coreLearnerList;
    }

    @Override
    public List<String> getDistinctIdentifiersBySegment(Map<String, Object> segments, Long optimizationRuleId) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            List<String> identifierList = new ArrayList<>();
            List<SegmentAndIdentifier> segmentAndIdentifierList = coreLearnerDao.getDistinctIdentifiersByRuleId(session, optimizationRuleId);
            for (SegmentAndIdentifier segmentAndIdentifier : segmentAndIdentifierList) {
                String segmentFromDB = segmentAndIdentifier.getSegment_values();
                String identifier = segmentAndIdentifier.getIdentifier();
                Map<String, Object> segmentMapFromDB = JsonUtil.jsonToMap(segmentFromDB);
                if (segments == null && segmentMapFromDB == null) {
                    identifierList.add(identifier);
                }
                if (segments != null && segmentMapFromDB != null && segments.equals(segmentMapFromDB)) {
                    identifierList.add(identifier);
                }
            }
            session.clear();
            return identifierList;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    @Override
    public List<OptimizeField> getDistinctOptimizeBySegmentAndIdentifier(Map<String, Object> segments, String identifier, Long optimizationRuleId) {
        Session session = null;
        List<OptimizeField> optimizeFieldList = new ArrayList<>();
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            List<SegmentAndIOptimizeField> segmentAndIOptimizeFieldList = coreLearnerDao.getDistinctOptimizeByRuleIdAndIdentifier(session, identifier, optimizationRuleId);
            for (SegmentAndIOptimizeField segmentAndIOptimizeField : segmentAndIOptimizeFieldList) {
                String optimizeField = segmentAndIOptimizeField.getOptimize_field();
                OptimizeField optimizeFieldObject = JsonUtil.jsonToObject(optimizeField, OptimizeField.class);
                String segmentFromDB = segmentAndIOptimizeField.getSegment_values();
                Map<String, Object> segmentMapFromDB = JsonUtil.jsonToMap(segmentFromDB);
                if (segments == null && segmentMapFromDB == null) {
                    optimizeFieldList.add(optimizeFieldObject);
                }
                if (segments != null && segmentMapFromDB != null && segments.equals(segmentMapFromDB)) {
                    optimizeFieldList.add(optimizeFieldObject);
                }
            }
            session.clear();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return optimizeFieldList;
    }
}

package com.pubvantage.service;

import com.pubvantage.dao.*;
import com.pubvantage.entity.*;
import com.pubvantage.utils.ConvertUtil;
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
    private CoreLearnerDaoInterface coreLearnerDao = new CoreLearnerDao();
    private static SparkDataTrainingDaoInterface sparkDataTrainingDao = new SparkDataTrainingDao();


    @Override
    public void saveListLearnerModel(List<CoreLearner> modelList, CoreOptimizationRule optimizationRule) {
        if (modelList == null || modelList.isEmpty()) {
            return;
        }
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            boolean deleteAll = coreLearnerDao.deleteAllByRuleId(session, optimizationRule.getId());
            if (!deleteAll) {
                return;
            }
            // save
            for (CoreLearner aModelList : modelList) {
                if (aModelList == null) {
                    continue;
                }
                aModelList.setCreatedDate(new Date());
                aModelList.setUpdatedDate(new Date());
                coreLearnerDao.save(aModelList, session);
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
    public CoreLearner getOneCoreLeaner(Long optimizationRuleId, String identifier, String optimizeFieldJson, String segmentGroup) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            CoreLearner coreLearner = coreLearnerDao.getOne(session, optimizationRuleId, identifier, optimizeFieldJson, segmentGroup);
            session.clear();
            return coreLearner;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    /**
     * @param optimizationRule optimization rule
     * @return all data need to generate prediction
     */
    @Override
    public PredictListData getPredictData(CoreOptimizationRule optimizationRule) {
        Long ruleId = optimizationRule.getId();
        PredictListData predictListData = new PredictListData();
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            predictListData.setRuleId(ruleId);
            predictListData.setOptimizeFieldsJson(coreLearnerDao.getOptimizeFieldsJson(session, ruleId));
            //date
            List<String> listDate = sparkDataTrainingDao.getDistinctDates(ruleId, optimizationRule.getDateField());
            ConvertUtil.addFutureDate(listDate);
            predictListData.setListDate(listDate);

            List<String> segmentGroupsJson = coreLearnerDao.getDistinctSegmentValues(session, predictListData.getRuleId());
            predictListData.setSegmentGroupJson(segmentGroupsJson);
            predictListData.setSegmentGroups(extractSegmentsFromJson(segmentGroupsJson));
            session.clear();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return predictListData;
    }

    private List<Map<String, String>> extractSegmentsFromJson(List<String> segmentGroups) {
        List<Map<String, String>> list = new ArrayList<>();
        for (String segmentJson : segmentGroups) {
            Map<String, String> map = JsonUtil.jsonToMap(segmentJson);
            list.add(map);
        }
        return list;
    }


    @Override
    public List<String> getMetricsFromCoreLeaner(CoreLearner coreLearner) {
        List<String> list = new ArrayList<>();
        MathModel mathModel = JsonUtil.jsonToObject(coreLearner.getMathModel(), MathModel.class);
        if (mathModel != null && mathModel.getCoefficients() != null && !mathModel.getCoefficients().isEmpty()) {
            for (Map.Entry<String, Double> entry : mathModel.getCoefficients().entrySet()) {
                list.add(entry.getKey());
            }
        }
        return list;
    }

    @Override
    public List<String> getDistinctIdentifiers(Long optimizationRuleId, String segmentGroup) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            List<String> segmentJson = coreLearnerDao.getDistinctIdentifiers(session, optimizationRuleId, segmentGroup);
            session.clear();

            return segmentJson;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

}

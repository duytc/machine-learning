package com.pubvantage.service;

import com.pubvantage.dao.CoreLearnerDao;
import com.pubvantage.dao.CoreLearnerDaoInterface;
import com.pubvantage.dao.CoreLearningModelDao;
import com.pubvantage.dao.CoreLearningModelDaoInterface;
import com.pubvantage.entity.CoreAutoOptimizationConfig;
import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.CoreLearningModel;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.utils.HibernateUtil;
import com.pubvantage.utils.JsonUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class CoreLearningModelService implements CoreLearningModelServiceInterface {
    private static Logger logger = Logger.getLogger(CoreLearningModelService.class.getName());
    private CoreLearningModelDaoInterface coreLearningModelDAO = new CoreLearningModelDao();
    private CoreLearnerDaoInterface coreLearnerDao = new CoreLearnerDao();

    @Override
    public void saveListLearnerModel(List<CoreLearner> modelList) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            for (CoreLearner aModelList : modelList) {
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
            if (!(optimizeFieldFromDB.getField().equals(optimizeField.getField())
                    && optimizeFieldFromDB.getGoal().equals(optimizeField.getGoal())
                    && optimizeFieldFromDB.getWeight() != null
                    && optimizeFieldFromDB.getWeight().doubleValue() == optimizeField.getWeight().doubleValue())) {
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

    /**
     * save list of models to database
     *
     * @param modelList list of model need to be saved
     */
    @Override
    public void saveListModel(List<CoreLearningModel> modelList) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            for (CoreLearningModel aModelList : modelList) {

                CoreLearningModel foundModel = coreLearningModelDAO.findOne(session, aModelList.getAutoOptimizationConfigId(), aModelList.getIdentifier());
                if (null == foundModel) {
                    //add new
                    coreLearningModelDAO.save(aModelList, session);
                } else {
                    //update
                    foundModel.setModel(aModelList.getModel());
                    foundModel.setForecastFactorValues(aModelList.getForecastFactorValues());
                    foundModel.setCategoricalFieldWeights(aModelList.getCategoricalFieldWeights());
                    foundModel.setUpdatedDate(new Date());
                    foundModel.setType(aModelList.getType());

                    coreLearningModelDAO.save(foundModel, session);
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
    public String getModelPath(CoreAutoOptimizationConfig coreAutoOptimizationConfig, String identifier) {
        Session session = null;
        String modelPath = "";
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            modelPath = coreLearningModelDAO.getModelPath(session, coreAutoOptimizationConfig.getId(), identifier);
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
        return modelPath;
    }

    @Override
    public CoreLearningModel findOne(Long autOptimizationConfigId, String identifier) {
        Session session = null;
        CoreLearningModel coreLearningModel = new CoreLearningModel();
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            coreLearningModel = coreLearningModelDAO.findOne(session, autOptimizationConfigId, identifier);
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
        return coreLearningModel;
    }
}

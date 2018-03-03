package com.pubvantage.service;

import com.pubvantage.dao.CoreLearnerDao;
import com.pubvantage.dao.CoreLearnerDaoInterface;
import com.pubvantage.dao.CoreLearningModelDao;
import com.pubvantage.dao.CoreLearningModelDaoInterface;
import com.pubvantage.entity.CoreAutoOptimizationConfig;
import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.CoreLearningModel;
import com.pubvantage.utils.HibernateUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.util.Date;
import java.util.List;

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

                CoreLearner foundModel = coreLearnerDao.findOne(session,
                        aModelList.getOptimizationRuleId(),
                        aModelList.getIdentifier(),
                        aModelList.getSegmentValues()
                        , aModelList.getOptimizeFields());
                if (null == foundModel) {
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

package com.pubvantage.service;

import com.pubvantage.dao.DataTrainingDAO;
import com.pubvantage.dao.DataTrainingDAOImpl;
import com.pubvantage.entity.CoreLearningModel;
import com.pubvantage.utils.HibernateUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.util.Date;
import java.util.List;

public class DataTrainingServiceImpl implements DataTrainingService {
    DataTrainingDAO trainingDAO = new DataTrainingDAOImpl();
    private static Logger logger = Logger.getLogger(DataTrainingServiceImpl.class.getName());

    /**
     * save list of models to database
     * @param modelList list of model need to be saved
     * @return true: success, false: fail
     */
    @Override
    public boolean saveListModel(List<CoreLearningModel> modelList) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            for (int index = 0; index < modelList.size(); index++) {

                CoreLearningModel foundModel = trainingDAO.findModelByIdentifierAndAutoOptimizationId(session, modelList.get(index));
                if (foundModel.getId() == null || foundModel.getId() == 0) {
                    //add new
                    trainingDAO.saveModel(session, modelList.get(index));
                } else {
                    //update
                    foundModel.setModel(modelList.get(index).getModel());
                    foundModel.setForecastFactorValues(modelList.get(index).getForecastFactorValues());
                    foundModel.setCategoricalFieldWeights(modelList.get(index).getCategoricalFieldWeights());
                    foundModel.setUpdatedDate(new Date());
                    foundModel.setType(modelList.get(index).getType());

                    trainingDAO.saveModel(session, foundModel);
                }
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            if (null != session.getTransaction()) {
                session.getTransaction().rollback();
            }
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return false;
    }
}

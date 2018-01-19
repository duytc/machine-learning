package com.pubvantage.dao;

import com.pubvantage.entity.CoreLearningModel;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.List;

public class DataTrainingDAOImpl implements DataTrainingDAO {
    private static Logger logger = Logger.getLogger(DataTrainingDAOImpl.class.getName());

    /**
     * Save or update core learning model
     * @param session hibernate session
     * @param model data need to insert to database
     * @return true: success, false: fail
     */

    @Override
    public boolean saveModel(Session session, CoreLearningModel model) {

        try {
            session.save(model);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return false;
    }

    /**
     * get core learning model by identifier and auto optimization id
     * @param session hibernate session
     * @param model data contain identifier and auto optimization id
     * @return CoreLearningModel object correspond a row in core_learning_model table in database
     */
    @Override
    public CoreLearningModel findModelByIdentifierAndAutoOptimizationId(Session session, CoreLearningModel model) {
        StringBuilder stringBuilder = new StringBuilder();

        CoreLearningModel resultModel = new CoreLearningModel();

        try {
            if (model != null) {
                long autoOptimizationConfigId = model.getAutoOptimizationConfigId();
                String identifier = model.getIdentifier();

                stringBuilder.append("FROM CoreLearningModel WHERE autoOptimizationConfigId = :autoOptimizationConfigId AND identifier = :identifier");

                Query query = session.createQuery(stringBuilder.toString());
                query.setParameter("autoOptimizationConfigId", autoOptimizationConfigId);
                query.setParameter("identifier", identifier);

                List<CoreLearningModel> list = query.list();
                if (list != null && !list.isEmpty()) {
                    resultModel = list.get(0);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        return resultModel;
    }

}

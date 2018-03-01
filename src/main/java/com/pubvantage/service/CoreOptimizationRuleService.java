package com.pubvantage.service;


import com.google.gson.JsonObject;
import com.pubvantage.dao.CoreAutoOptimizationConfigDao;
import com.pubvantage.entity.CoreAutoOptimizationConfig;
import com.pubvantage.utils.HibernateUtil;
import org.hibernate.Session;
import org.apache.log4j.Logger;
import java.util.List;

public class CoreOptimizationRuleService implements CoreOptimizationRuleServiceInterface {
    private CoreAutoOptimizationConfigDao coreAutoOptimizationConfigDao = new CoreAutoOptimizationConfigDao();
    private static Logger logger = Logger.getLogger(CoreOptimizationRuleService.class.getName());

    @Override
    public String[] getSegmentFields(long optimizationRuleId) {
        return new String[0];
    }

    @Override
    public String[] getOptimizeFields(long optimizationRuleId) {
        return new String[0];
    }

    @Override
    public String[] getMetrics(long optimizationRuleId) {
        return new String[0];
    }

    @Override
    public String[] getIdentifiers(long optimizationRuleId) {
        return null;
    }

    @Override
    public boolean checkToken(long autoOptimizationConfigId, String token) {
        Session session = null;
        boolean isValid = false;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            isValid = coreAutoOptimizationConfigDao.checkToken(session, autoOptimizationConfigId, token);
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
        return isValid;
    }

    @Override
    public List<String> getFactors(Long id) {
        return null;
    }

    @Override
    public JsonObject getFieldType(Long id) {
        return null;
    }

    @Override
    public CoreAutoOptimizationConfig findById(Long autoOptimizationConfigId) {
        return null;
    }

    @Override
    public String[] getObjectiveAndFactors(long autoOptimizationId) {
        return new String[0];
    }

    @Override
    public List<String> getPositiveFactors(long autoOptimizationId) {
        return null;
    }

    @Override
    public List<String> getNegativeFactors(long autoOptimizationId) {
        return null;
    }

}

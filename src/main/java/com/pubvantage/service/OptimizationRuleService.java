package com.pubvantage.service;


import com.google.gson.JsonObject;
import com.pubvantage.dao.CoreAutoOptimizationConfigDao;
import com.pubvantage.dao.OptimizationRuleDao;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.utils.HibernateUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class OptimizationRuleService implements OptimizationRuleServiceInterface {
    private CoreAutoOptimizationConfigDao coreAutoOptimizationConfigDao = new CoreAutoOptimizationConfigDao();
    private static Logger logger = Logger.getLogger(OptimizationRuleService.class.getName());
    private OptimizationRuleDao optimizationRuleDao = new OptimizationRuleDao();

    @Override
    public List<String> getSegmentFields(CoreOptimizationRule optimizationRule) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getOptimizeFields(long optimizationRuleId) {
        return null;
    }

    @Override
    public String[] getMetrics(long optimizationRuleId) {
        return new String[0];
    }

    @Override
    public List<String> getIdentifiers(CoreOptimizationRule optimizationRule) {
        return new ArrayList<>();
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
    public CoreOptimizationRule findById(Long optimizationRuleId) {
        Session session = null;
        CoreOptimizationRule optimizationRule = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            optimizationRule = optimizationRuleDao.findById(optimizationRuleId, session);
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
        return optimizationRule;
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

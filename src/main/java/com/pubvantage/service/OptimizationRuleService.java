package com.pubvantage.service;


import com.google.gson.JsonObject;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.dao.CoreAutoOptimizationConfigDao;
import com.pubvantage.dao.OptimizationRuleDao;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.CoreReportView;
import com.pubvantage.service.Learner.ReportViewService;
import com.pubvantage.utils.HibernateUtil;
import com.pubvantage.utils.JsonUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OptimizationRuleService implements OptimizationRuleServiceInterface {
    private CoreAutoOptimizationConfigDao coreAutoOptimizationConfigDao = new CoreAutoOptimizationConfigDao();
    private static Logger logger = Logger.getLogger(OptimizationRuleService.class.getName());
    private OptimizationRuleDao optimizationRuleDao = new OptimizationRuleDao();
    private ReportViewServiceInterface viewService = new ReportViewService();

    @Override
    public List<String> getSegmentFields(Long optimizationRuleId) {
        CoreOptimizationRule optimizationRule = this.findById(optimizationRuleId);
        return JsonUtil.jsonArrayStringToJavaList(optimizationRule.getSegmentFields());
    }

    @Override
    public List<String> getOptimizeFields(Long optimizationRuleId) {
        CoreOptimizationRule optimizationRule = this.findById(optimizationRuleId);
        List<HashMap<String, String>> map = JsonUtil.jsonArrayObjectsToListMap(optimizationRule.getOptimizeFields());
        List<String> optimizeFieldList = new ArrayList<>();
        map.forEach(optimizeField -> optimizeFieldList.add(optimizeField.get(MyConstant.FIELD)));
        return optimizeFieldList;
    }

    /**
     * get only number metrics (not implement yet)
     * @param optimizationRuleId
     * @return
     */
    @Override
    public List<String> getMetrics(Long optimizationRuleId) {
        CoreOptimizationRule optimizationRule = this.findById(optimizationRuleId);
        CoreReportView reportView = viewService.findById(optimizationRule.getReportViewId());
        return JsonUtil.jsonArrayStringToJavaList(reportView.getMetrics());
    }

    @Override
    public List<String> getIdentifiers(CoreOptimizationRule optimizationRule) {
        return new ArrayList<>();
    }

    @Override
    public boolean checkToken(Long autoOptimizationConfigId, String token) {
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
    public String[] getObjectiveAndFactors(Long autoOptimizationId) {
        return new String[0];
    }

    @Override
    public List<String> getPositiveFactors(Long autoOptimizationId) {
        return null;
    }

    @Override
    public List<String> getNegativeFactors(Long autoOptimizationId) {
        return null;
    }

}

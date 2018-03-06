package com.pubvantage.service;


import com.google.gson.JsonObject;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.dao.CoreAutoOptimizationConfigDao;
import com.pubvantage.dao.OptimizationRuleDao;
import com.pubvantage.dao.SparkDataTrainingDao;
import com.pubvantage.dao.SparkDataTrainingDaoInterface;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.CoreReportView;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.service.Learner.ReportViewService;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.HibernateUtil;
import com.pubvantage.utils.JsonUtil;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Row;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptimizationRuleService implements OptimizationRuleServiceInterface {
    private CoreAutoOptimizationConfigDao coreAutoOptimizationConfigDao = new CoreAutoOptimizationConfigDao();
    private static Logger logger = Logger.getLogger(OptimizationRuleService.class.getName());
    private OptimizationRuleDao optimizationRuleDao = new OptimizationRuleDao();
    private ReportViewServiceInterface viewService = new ReportViewService();
    private SparkDataTrainingDaoInterface sparkDataTrainingDao = new SparkDataTrainingDao();

    @Override
    public List<String> getSegmentFields(Long optimizationRuleId) {
        CoreOptimizationRule optimizationRule = this.findById(optimizationRuleId);
        return JsonUtil.jsonArrayStringToJavaList(optimizationRule.getSegmentFields());
    }

    @Override
    public List<OptimizeField> getOptimizeFields(Long optimizationRuleId) {
        CoreOptimizationRule optimizationRule = this.findById(optimizationRuleId);
        return getOptimizeFields(optimizationRule);
    }

    @Override
    public List<OptimizeField> getOptimizeFields(CoreOptimizationRule optimizationRule) {
        List<HashMap<String, String>> map = JsonUtil.jsonArrayObjectsToListMap(optimizationRule.getOptimizeFields());
        List<OptimizeField> optimizeFieldList = new ArrayList<>();
        map.forEach(optimizeField -> {
            OptimizeField optimizeFieldObject = new OptimizeField();
            optimizeFieldObject.setField(optimizeField.get(MyConstant.FIELD));
            optimizeFieldObject.setGoal(optimizeField.get(MyConstant.GOAL));
            optimizeFieldObject.setWeight(ConvertUtil.convertObjectToDouble(optimizeField.get(MyConstant.WEIGHT)));
            optimizeFieldList.add(optimizeFieldObject);

        });
        return optimizeFieldList;
    }

    /**
     * get only number metrics (but not implement yet)
     *
     * @param optimizationRuleId
     * @return
     */
    @Override
    public List<String> getMetrics(Long optimizationRuleId) {
        CoreOptimizationRule optimizationRule = this.findById(optimizationRuleId);
        CoreReportView reportView = viewService.findById(optimizationRule.getReportViewId());
        String jsonFieldType = reportView.getFieldTypes();
        List<String> metrics = JsonUtil.jsonArrayStringToJavaList(reportView.getMetrics());
        Map<String, String> fieldType = JsonUtil.jsonToMap(jsonFieldType);
        return filterNumberTypeMetric(metrics, fieldType);
    }

    private List<String> filterNumberTypeMetric(List<String> metrics, Map<String, String> fieldType) {
        List<String> filteredMetrics = new ArrayList<>();
        if (metrics != null && fieldType != null) {
            for (int i = 0; i < metrics.size(); i++) {
                String type = fieldType.get(metrics.get(i));
                if (MyConstant.DECIMAL_TYPE.equals(type) || MyConstant.NUMBER_TYPE.equals(type)) {
                    filteredMetrics.add(metrics.get(i));
                }
            }
        }
        return filteredMetrics;
    }

    @Override
    public List<String> getVectorFields(Long optimizationRuleId) {
        CoreOptimizationRule optimizationRule = this.findById(optimizationRuleId);

        return null;
    }


    @Override
    public List<String> getIdentifiers(CoreOptimizationRule optimizationRule) {
        List<Row> resultList = sparkDataTrainingDao.getIdentifiers(optimizationRule.getId());
        List<String> identifiers = new ArrayList<>();

        if (resultList != null && !resultList.isEmpty()) {
            for (Row aResultList : resultList) {
                identifiers.add(aResultList.get(0).toString());
            }
        }
        return identifiers;
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

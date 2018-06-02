package com.pubvantage.service;


import com.jsoniter.JsonIterator;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.dao.*;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.CoreReportView;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.HibernateUtil;
import com.pubvantage.utils.JsonUtil;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Row;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.*;

public class OptimizationRuleService extends AbstractGenericService<CoreOptimizationRule> implements OptimizationRuleServiceInterface {
    private static Logger logger = Logger.getLogger(OptimizationRuleService.class.getName());
    private ReportViewServiceInterface reportViewService = new ReportViewService();
    private SparkDataTrainingDaoInterface sparkDataTrainingDao = new SparkDataTrainingDao();

    private OptimizationRuleDaoInterface optimizationRuleDao = new OptimizationRuleDao();
    private OptimizeField field;

    @Override
    public List<String> getColumnsForScoreTable(CoreOptimizationRule optimizationRule) {
        List<String> columns = new ArrayList<>();
        columns.add(MyConstant.SCORE_ID);
        columns.add(optimizationRule.getDateField());
        columns.add(MyConstant.SCORE_IDENTIFIER);
        columns.add(MyConstant.SCORE_SEGMENT_VALUES);

        List<HashMap<String, String>> optimizeFields = JsonIterator.deserialize(optimizationRule.getOptimizeFields(), ArrayList.class);
        for (HashMap<String, String> optimize : optimizeFields) {
            String string = JsonUtil.toJson(optimize);
            OptimizeField optimizeField3 = JsonUtil.jsonToObject(string, OptimizeField.class);
            columns.add(optimizeField3.getField());
        }
        columns.add(MyConstant.SCORE);
        columns.add(MyConstant.SCORE_IS_PREDICT);
        return columns;
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

    @Override
    public List<String> getIdentifiers(CoreOptimizationRule optimizationRule) {
        List<Row> resultList = sparkDataTrainingDao.getIdentifiers(optimizationRule.getId());
        List<String> identifiers = new ArrayList<>();

        if (resultList != null && !resultList.isEmpty()) {
            for (Row aResultList : resultList) {
                if (aResultList.get(0) != null)
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
            isValid = optimizationRuleDao.checkToken(session, autoOptimizationConfigId, token);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return isValid;
    }


    @Override
    public boolean checkOptimizeFieldIsDigit(CoreOptimizationRule optimizationRule, OptimizeField optimizeField) {
        if (optimizeField == null) return false;
        if (optimizationRule == null || optimizationRule.getReportViewId() == null) return false;

        CoreReportView reportView = reportViewService.findById(optimizationRule.getReportViewId(), new ReportViewDao());
        if (reportView == null || reportView.getId() == null) return false;

        String jsonFieldType = reportView.getFieldTypes();
        Map<String, String> fieldType = JsonUtil.jsonToMap(jsonFieldType);
        String optimizeType = fieldType.get(optimizeField.getField());
        return MyConstant.DECIMAL_TYPE.equals(optimizeType) || MyConstant.NUMBER_TYPE.equals(optimizeType);
    }

    @Override
    public void setLoadingForOptimizationRule(Long optimizationRuleId, boolean finishLoading) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            String builder = "UPDATE CoreOptimizationRule SET finishLoading = " +
                    finishLoading +
                    " WHERE id = " +
                    optimizationRuleId;
            Query query = session.createQuery(builder);
            query.executeUpdate();
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
    }

    @Override
    public List<String> getSegments(CoreOptimizationRule optimizationRule) {
        String jsonSegments = optimizationRule.getSegmentFields();
        return JsonUtil.jsonArrayStringToJavaList(jsonSegments);
    }

    @Override
    public List<String> getNoSpaceSegments(CoreOptimizationRule optimizationRule) {
        return ConvertUtil.removeSpace(getSegments(optimizationRule));
    }

    @Override
    public List<String> getDimensions(CoreOptimizationRule optimizationRule) {
        CoreReportView reportView = reportViewService.findById(optimizationRule.getReportViewId(), new ReportViewDao());
        return reportViewService.getDimensions(reportView);
    }

    @Override
    public List<String> getNoSpaceDimensions(CoreOptimizationRule optimizationRule) {
        CoreReportView reportView = reportViewService.findById(optimizationRule.getReportViewId(), new ReportViewDao());
        return reportViewService.getNoSpaceDimensions(reportView);
    }

    /**
     * compute hash md5 use optimize field
     *
     * @param optimizationRule rule
     * @return checksum
     */
    @Override
    public String getCurrentRuleChecksum(CoreOptimizationRule optimizationRule) {
        if (optimizationRule == null) {
            return null;
        }
        List<OptimizeField> optimizeFieldList = getOptimizeFields(optimizationRule);
        Collections.sort(optimizeFieldList);
        StringBuilder stringBuilder = new StringBuilder();
        for (OptimizeField field : optimizeFieldList) {
            stringBuilder.append(field.getField())
                    .append(field.getGoal())
                    .append(field.getWeight());
        }

        return ConvertUtil.hashMd5(stringBuilder.toString());
    }

    @Override
    public String getCurrentTrainingDataChecksum(Long optimizationRuleId) {
        if (optimizationRuleId == null) {
            return null;
        }
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Object currentChecksum = optimizationRuleDao.getCurrentChecksum("__data_training_" + optimizationRuleId, session);
            if (currentChecksum == null) {
                return null;
            }
            return currentChecksum.toString();
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
    public boolean isChecksumChanged(String current, String last) {
        if (current == null) {
            return last != null;
        }
        return !current.equals(last);
    }

    @Override
    public boolean updateRuleChecksum(CoreOptimizationRule optimizationRule) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            CoreOptimizationRule ruleFromDb = optimizationRuleDao.findById(optimizationRule.getId(), session);
            if (ruleFromDb != null) {
                ruleFromDb.setLastRuleChecksum(optimizationRule.getLastRuleChecksum());
                optimizationRuleDao.save(ruleFromDb, session);
                session.getTransaction().commit();
                return true;
            }
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
        return false;
    }

    @Override
    public boolean updateTraingDataChecksum(CoreOptimizationRule optimizationRule) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            CoreOptimizationRule ruleFromDb = optimizationRuleDao.findById(optimizationRule.getId(), session);
            if (ruleFromDb != null) {
                ruleFromDb.setLastTrainingDataChecksum(optimizationRule.getLastTrainingDataChecksum());
                optimizationRuleDao.save(ruleFromDb, session);
                session.getTransaction().commit();
                return true;
            }
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
        return false;
    }
}

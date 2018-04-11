package com.pubvantage.service;

import com.pubvantage.constant.MyConstant;
import com.pubvantage.dao.ScoreDao;
import com.pubvantage.dao.ScoreDaoInterface;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.HibernateUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreService implements ScoreServiceInterface {
    private static Logger logger = Logger.getLogger(ScoreService.class.getName());
    private OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();
    private ScoreDaoInterface scoreDao = new ScoreDao();

    @Override
    public void saveScore(Map<String, Map<String, Map<String, Map<String, Double>>>> scoreMap,
                          CoreOptimizationRule coreOptimizationRule,
                          String futureDate) {
        List<String> columnsSpace = optimizationRuleService.getColumnsForScoreTable(coreOptimizationRule);
        List<String> columns = ConvertUtil.removeSpace(columnsSpace);
        Map<String, Object> values = new HashMap<>();
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            boolean deleteAll = scoreDao.deleteAll(session, coreOptimizationRule.getId());
            if (!deleteAll) {
                throw new Exception("Delete learner model fail");
            }
            //loop by date
            for (Map.Entry<String, Map<String, Map<String, Map<String, Double>>>> dateEntry : scoreMap.entrySet()) {
                String date = dateEntry.getKey();
                if (futureDate.equals(date)) {
                    values.put(MyConstant.SCORE_IS_PREDICT, true);
                } else {
                    values.put(MyConstant.SCORE_IS_PREDICT, false);
                }
                String dateField = coreOptimizationRule.getDateField();
                values.put(ConvertUtil.removeSpace(dateField), date);
                Map<String, Map<String, Map<String, Double>>> dateMap = dateEntry.getValue();
                //loop by segment
                for (Map.Entry<String, Map<String, Map<String, Double>>> segmentEntry : dateMap.entrySet()) {
                    String segment = segmentEntry.getKey();
                    segment = MyConstant.GLOBAL_KEY.equals(segment) ? null : segment;
                    values.put(MyConstant.SCORE_SEGMENT_VALUES, segment);
                    Map<String, Map<String, Double>> segmentMap = segmentEntry.getValue();
                    //loop by identifier
                    for (Map.Entry<String, Map<String, Double>> identifierEntry : segmentMap.entrySet()) {
                        String identifier = identifierEntry.getKey();
                        values.put(MyConstant.SCORE_IDENTIFIER, identifier);
                        Map<String, Double> optimizeMap = identifierEntry.getValue();
                        for (Map.Entry<String, Double> optimizeEntry : optimizeMap.entrySet()) {
                            values.put(optimizeEntry.getKey(), optimizeEntry.getValue());
                        }
                        //save
                        scoreDao.insertScore(session, columns, values, coreOptimizationRule, optimizeMap);
                    }
                }
            }

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
}
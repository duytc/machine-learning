package com.pubvantage.dao;

import com.pubvantage.constant.MyConstant;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.service.score.ScoreService;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.JsonUtil;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScoreDao implements ScoreDaoInterface {
    private static Logger logger = Logger.getLogger(ScoreService.class.getName());

    @Override
    public boolean deleteAll(Session session, Long ruleId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM ")
                .append(MyConstant.SCORE_TABLE_NAME_PRE)
                .append(ruleId);
        try {
            Query query = session.createSQLQuery(stringBuilder.toString());
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error(e.getCause(), e);
        }
        return false;
    }

    @Override
    public int updateScore(Session session, List<String> columns, Map<String, Object> values,
                           CoreOptimizationRule optimizationRule, Map<String, Double> optimizeMap, Long scoreId) {
        StringBuilder stringBuilder = new StringBuilder();
        Long optimizeRuleId = optimizationRule.getId();
        String dateField = optimizationRule.getDateField();
        String noSpaceDateField = ConvertUtil.removeSpace(dateField);
        List<String> paramString = ConvertUtil.concatParamUpdateQuery(columns);
        try {
            stringBuilder.append("UPDATE ")
                    .append(MyConstant.SCORE_TABLE_NAME_PRE)
                    .append(optimizeRuleId)
                    .append(" SET ")
                    .append(ConvertUtil.joinListString(paramString, ", "))
                    .append(" WHERE ")
                    .append(MyConstant.SCORE_ID)
                    .append(" = ")
                    .append(scoreId);
            Query query = session.createSQLQuery(stringBuilder.toString());
            query.setParameter(noSpaceDateField, values.get(dateField));
            query.setParameter(MyConstant.SCORE_IDENTIFIER, values.get(MyConstant.SCORE_IDENTIFIER));
            query.setParameter(MyConstant.SCORE_SEGMENT_VALUES, values.get(MyConstant.SCORE_SEGMENT_VALUES));
            addOptimizeParamForQuery(query, optimizeMap);
            query.setParameter(MyConstant.SCORE, values.get(MyConstant.SCORE));
            query.setParameter(MyConstant.SCORE_IS_PREDICT, values.get(MyConstant.SCORE_IS_PREDICT));
            return query.executeUpdate();

        } catch (Exception e) {
            logger.error(e.getCause(), e);
            return -1;
        }
    }

    private void addOptimizeParamForQuery(Query query, Map<String, Double> optimizeMap) {
        for (Map.Entry<String, Double> optimizeEntry : optimizeMap.entrySet()) {
            String key = optimizeEntry.getKey();
            if (MyConstant.SCORE.equals(key)) continue;
            OptimizeField optimizeField = JsonUtil.jsonToObject(key, OptimizeField.class);
            String optimizeFieldNoSpace = ConvertUtil.removeSpace(optimizeField.getField());
            if (MyConstant.NULL_PREDICT_VALUE == optimizeEntry.getValue()) {
                query.setParameter(optimizeFieldNoSpace, null);
            } else {
                query.setParameter(optimizeFieldNoSpace, optimizeEntry.getValue());
            }
        }
    }

    @Override
    public int insertScore(Session session, List<String> columns, Map<String, Object> values,
                           CoreOptimizationRule optimizationRule, Map<String, Double> optimizeMap) {
        StringBuilder stringBuilder = new StringBuilder();
        Long optimizeRuleId = optimizationRule.getId();
        String dateField = optimizationRule.getDateField();
        String noSpaceDateField = ConvertUtil.removeSpace(dateField);
        try {

            stringBuilder.append("INSERT INTO ")
                    .append(MyConstant.SCORE_TABLE_NAME_PRE)
                    .append(optimizeRuleId).append("(")
                    .append(ConvertUtil.joinListString(columns, ", "))
                    .append(")").append(" VALUES (");

            stringBuilder.append(ConvertUtil.buildInsertValueQuery(columns));
            stringBuilder.append(")");
            Query query = session.createSQLQuery(stringBuilder.toString());
            query.setParameter(MyConstant.SCORE_ID, 0);
            query.setParameter(noSpaceDateField, values.get(noSpaceDateField));
            query.setParameter(MyConstant.SCORE_IDENTIFIER, values.get(MyConstant.SCORE_IDENTIFIER));
            query.setParameter(MyConstant.SCORE_SEGMENT_VALUES, values.get(MyConstant.SCORE_SEGMENT_VALUES));
            addOptimizeParamForQuery(query, optimizeMap);
            query.setParameter(MyConstant.SCORE, values.get(MyConstant.SCORE));
            query.setParameter(MyConstant.SCORE_IS_PREDICT, values.get(MyConstant.SCORE_IS_PREDICT));
            return query.executeUpdate();

        } catch (Exception e) {
            logger.error(e.getCause(), e);
            return -1;
        }

    }

    @Override
    public Map<String, Object> findOne(Session session, List<String> columns, Map<String, Object> values,
                                       CoreOptimizationRule optimizationRule, Map<String, Double> optimizeMap) {
        Long optimizeRuleId = optimizationRule.getId();
        String dateField = optimizationRule.getDateField();
        String noSpaceDateField = ConvertUtil.removeSpace(dateField);
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("SELECT ")
                    .append(ConvertUtil.joinListString(columns, ", "))
                    .append(" FROM ")
                    .append(MyConstant.SCORE_TABLE_NAME_PRE)
                    .append(optimizeRuleId).append(" WHERE ")
                    .append(MyConstant.SCORE_IDENTIFIER)
                    .append(" = :identifier AND ")
                    .append(" ").append(MyConstant.SCORE_SEGMENT_VALUES);
            if (values.get(MyConstant.SCORE_SEGMENT_VALUES) == null) {
                stringBuilder.append(" IS NULL AND ");
            } else {
                stringBuilder.append(" = :segment_values AND ");
            }
            stringBuilder.append("DATE_FORMAT(")
                    .append(noSpaceDateField)
                    .append(", '")
                    .append(MyConstant.DATE_FORMAT)
                    .append("')")
                    .append(" = '")
                    .append(values.get(dateField)).append("'");
            Query query = session.createSQLQuery(stringBuilder.toString());
            query.setParameter("identifier", values.get(MyConstant.SCORE_IDENTIFIER));
            if (values.get(MyConstant.SCORE_SEGMENT_VALUES) != null) {
                query.setParameter("segment_values", values.get(MyConstant.SCORE_SEGMENT_VALUES));
            }

            List<Object[]> list = query.list();
            if (list != null && !list.isEmpty()) {
                Object[] row = list.get(0);
                Map<String, Object> map = new LinkedHashMap<>();
                for (int i = 0; i < columns.size(); i++) {
                    Object value = row[i];
                    map.put(columns.get(i), value);
                }
                return map;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}

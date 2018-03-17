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
    public int updateScore(Session session, List<String> columns, Map<String, Object> values,
                           CoreOptimizationRule optimizationRule, Map<String, Double> optimizeMap, Long scoreId) {
        StringBuilder stringBuilder = new StringBuilder();
        Long optimizeRuleId = optimizationRule.getId();
        String dateField = optimizationRule.getDateField();
        List<String> paramString = ConvertUtil.concatParamUpdateQuery(columns);
        try {

            stringBuilder.append("UPDATE " + MyConstant.SCORE_TABLE_NAME_PRE + optimizeRuleId + " SET ");
            stringBuilder.append(ConvertUtil.joinListString(paramString, ", "));
            stringBuilder.append(" WHERE ").append(MyConstant.SCORE_ID).append(" = " + scoreId);
            Query query = session.createSQLQuery(stringBuilder.toString());
            query.setParameter(dateField, values.get(dateField));
            query.setParameter(MyConstant.SCORE_IDENTIFIER, values.get(MyConstant.SCORE_IDENTIFIER));
            query.setParameter(MyConstant.SCORE_SEGMENT_VALUES, values.get(MyConstant.SCORE_SEGMENT_VALUES));
            for (Map.Entry<String, Double> optimizeEntry : optimizeMap.entrySet()) {
                String key = optimizeEntry.getKey();
                if (MyConstant.SCORE.equals(key)) continue;
                OptimizeField optimizeField = JsonUtil.jsonToObject(key, OptimizeField.class);
                query.setParameter(optimizeField.getField(), optimizeEntry.getValue());
            }
            query.setParameter(MyConstant.SCORE, values.get(MyConstant.SCORE));
            query.setParameter(MyConstant.SCORE_IS_PREDICT, values.get(MyConstant.SCORE_IS_PREDICT));
            return query.executeUpdate();

        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public int insertScore(Session session, List<String> columns, Map<String, Object> values,
                           CoreOptimizationRule optimizationRule, Map<String, Double> optimizeMap) {
        StringBuilder stringBuilder = new StringBuilder();
        Long optimizeRuleId = optimizationRule.getId();
        String dateField = optimizationRule.getDateField();

        try {

            stringBuilder.append("INSERT INTO " + MyConstant.SCORE_TABLE_NAME_PRE + optimizeRuleId + "(")
                    .append(ConvertUtil.joinListString(columns, ", "))
                    .append(")").append(" VALUES (");

            stringBuilder.append(ConvertUtil.buildInsertValueQuery(columns));
            stringBuilder.append(")");
            Query query = session.createSQLQuery(stringBuilder.toString());
            query.setParameter(MyConstant.SCORE_ID, 0);
            query.setParameter(dateField, values.get(dateField));
            query.setParameter(MyConstant.SCORE_IDENTIFIER, values.get(MyConstant.SCORE_IDENTIFIER));
            query.setParameter(MyConstant.SCORE_SEGMENT_VALUES, values.get(MyConstant.SCORE_SEGMENT_VALUES));
            for (Map.Entry<String, Double> entry : optimizeMap.entrySet()) {
                String key = entry.getKey();
                if (MyConstant.SCORE.equals(key)) continue;
                OptimizeField optimizeField = JsonUtil.jsonToObject(key, OptimizeField.class);
                query.setParameter(optimizeField.getField(), entry.getValue());
            }
            query.setParameter(MyConstant.SCORE, values.get(MyConstant.SCORE));
            query.setParameter(MyConstant.SCORE_IS_PREDICT, values.get(MyConstant.SCORE_IS_PREDICT));
            return query.executeUpdate();

        } catch (Exception e) {
            return -1;
        }

    }

    @Override
    public Map<String, Object> findOne(Session session, List<String> columns, Map<String, Object> values,
                                       CoreOptimizationRule optimizationRule, Map<String, Double> optimizeMap) {
        Long optimizeRuleId = optimizationRule.getId();
        String dateField = optimizationRule.getDateField();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("SELECT " + ConvertUtil.joinListString(columns, ", ")
                    + " FROM " + MyConstant.SCORE_TABLE_NAME_PRE + optimizeRuleId
                    + " WHERE " + MyConstant.SCORE_IDENTIFIER + " = :identifier AND ");
            stringBuilder.append(" " + MyConstant.SCORE_SEGMENT_VALUES + " = :segment_values AND ");
            stringBuilder.append("DATE_FORMAT(" + dateField + ", '" + MyConstant.DATE_FORMAT + "')")
                    .append(" = '")
                    .append(values.get(dateField)).append("'");
            Query query = session.createSQLQuery(stringBuilder.toString());
            query.setParameter("identifier", values.get(MyConstant.SCORE_IDENTIFIER));
            query.setParameter("segment_values", values.get(MyConstant.SCORE_SEGMENT_VALUES));

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
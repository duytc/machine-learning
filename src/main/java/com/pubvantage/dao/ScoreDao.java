package com.pubvantage.dao;

import com.pubvantage.constant.MyConstant;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.service.ScoreService;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.JsonUtil;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

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

    private void addOptimizeParamForQuery(Query query, Map<String, Double> optimizeMap) {
        for (Map.Entry<String, Double> optimizeEntry : optimizeMap.entrySet()) {
            String key = optimizeEntry.getKey();
            if (MyConstant.SCORE.equals(key)) continue;
            OptimizeField optimizeField = JsonUtil.jsonToObject(key, OptimizeField.class);
            String optimizeFieldNoSpace = ConvertUtil.removeSpace(optimizeField.getField());
            if (MyConstant.NO_HISTORY_PREDICTION_VALUE == optimizeEntry.getValue()) {
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
}

package com.pubvantage.dao;

import com.pubvantage.entity.CoreOptimizationRule;
import org.hibernate.Session;

import java.util.List;
import java.util.Map;

public interface ScoreDaoInterface {
    boolean deleteAll(Session session, Long ruleId);

    int insertScore(Session session, List<String> columns, Map<String, Object> values,
                    CoreOptimizationRule optimizationRule, Map<String, Double> optimizeMap);

}

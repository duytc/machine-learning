package com.pubvantage.dao;

import com.pubvantage.entity.CoreOptimizationRule;
import org.hibernate.Session;

public interface OptimizationRuleDaoInterface extends GenericDaoInterface<CoreOptimizationRule> {
    boolean checkToken(Session session, Long autoOptimizationConfigId, String token);
}

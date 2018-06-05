package com.pubvantage.dao;

import com.pubvantage.entity.CoreOptimizationRule;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.List;

public class OptimizationRuleDao extends AbstractGenericDao<CoreOptimizationRule> implements OptimizationRuleDaoInterface {
    @Override
    public boolean checkToken(Session session, Long autoOptimizationId, String token) {
        Query query = session.createQuery("FROM CoreOptimizationRule WHERE id = :autoOptimizationId AND token = :token");
        query.setParameter("autoOptimizationId", autoOptimizationId);
        query.setParameter("token", token);
        List<CoreOptimizationRule> configList = query.list();

        return null != configList && !configList.isEmpty();
    }
}

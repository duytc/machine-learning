package com.pubvantage.dao;

import com.pubvantage.entity.CoreAutoOptimizationConfig;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.List;

public class CoreAutoOptimizationConfigDao extends AbstractGenericDao<CoreAutoOptimizationConfig> implements CoreAutoOptimizationConfigDaoInterface {


    @Override
    public boolean checkToken(Session session, Long autoOptimizationId, String token) {
        Query query = session.createQuery("FROM CoreAutoOptimizationConfig WHERE id = :autoOptimizationId AND token = :token");
        query.setParameter("autoOptimizationId", autoOptimizationId);
        query.setParameter("token", token);
        List<CoreAutoOptimizationConfig> configList = query.list();

        return null != configList && !configList.isEmpty();
    }
}

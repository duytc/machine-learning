package com.pubvantage.dao;

import com.pubvantage.entity.CoreAutoOptimizationConfig;
import org.hibernate.Session;

public interface CoreAutoOptimizationConfigDaoInterface extends GenericDaoInterface<CoreAutoOptimizationConfig> {

    boolean checkToken(Session session, Long autoOptimizationConfigId, String token);

}

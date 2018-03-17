package com.pubvantage.service;

import com.pubvantage.dao.SparkDataTrainingDao;
import com.pubvantage.dao.SparkDataTrainingDaoInterface;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;

public class DataTrainingServiceOld implements DataTrainingServiceInterface {
    private SparkDataTrainingDaoInterface sparkDataTrainingDaoInterface = new SparkDataTrainingDao();

    /**
     * @param autoOptimizationConfigId auto optimization config id
     * @return list of identifiers
     */
    @Override
    public String[] getIdentifiers(Long autoOptimizationConfigId) {
        List<Row> resultList = sparkDataTrainingDaoInterface.getIdentifiers(autoOptimizationConfigId);

        String[] identifiers = new String[0];

        int index = 0;
        if (resultList != null && !resultList.isEmpty()) {
            int size = resultList.size();
            identifiers = new String[size];
            for (Row aResultList : resultList) {
                identifiers[index++] = aResultList.get(0).toString();
            }
        }

        return identifiers;
    }

}

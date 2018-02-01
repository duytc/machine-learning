package com.pubvantage.dao;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;

public interface SparkDataTrainingDaoInterface {
    Dataset<Row> getDataSetByTable(Long autoOptimizationConfigId, String identifier, String[] objectiveAndFactor);

    List<Row> getIdentifiers(Long autoOptimizationConfigId);

    List<Object> getDistinctByFactor(String factorName, Long autoOptimizationConfigId);
}

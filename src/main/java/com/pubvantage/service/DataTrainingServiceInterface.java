package com.pubvantage.service;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public interface DataTrainingServiceInterface {
    String[] getIdentifiers(Long autoOptimizationConfigId);

    Dataset<Row> getDataSetByTable(Long autoOptimizationConfigId, String identifier, String[] objectiveAndFactor);

}

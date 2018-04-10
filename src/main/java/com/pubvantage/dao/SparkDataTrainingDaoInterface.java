package com.pubvantage.dao;

import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.entity.prediction.PredictDataWrapper;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;
import java.util.Map;

public interface SparkDataTrainingDaoInterface {

    Dataset<Row> getDataSet(CoreOptimizationRule optimizationRule, String identifier, List<String> objectiveAndFields);

    List<Row> getIdentifiers(Long autoOptimizationConfigId);

    List<String> getDistinctDates(Long optimizationRuleId, String dateField);

    Double getObjectiveFromDB(PredictDataWrapper predictDataWrapper,
                              List<String> metrics,
                              List<String> dimensions,
                              CoreOptimizationRule optimizationRule);
}

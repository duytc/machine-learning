package com.pubvantage.conditionprocessor;

import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.service.CoreLearningModelService;
import com.pubvantage.service.CoreLearningModelServiceInterface;
import com.pubvantage.utils.JsonUtil;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;

import java.util.LinkedHashMap;
import java.util.List;

public class ConditionConverter {
    private CoreLearningModelServiceInterface coreLearnerModelService = new CoreLearningModelService();
    private CoreLearner coreLearner;

    public ConditionConverter(CoreLearner coreLearner) {
        this.coreLearner = coreLearner;
    }

    public Vector buildVector() {
        List<String> metrics = coreLearnerModelService.getMetricsFromCoreLeaner(coreLearner);
        double[] doubleValue = new double[metrics.size()];
        LinkedHashMap<String, Double> metricsPredictiveValues = getMetricsPredictionValues(coreLearner);
        for (int index = 0; index < metrics.size(); index++) {
            String fieldName = metrics.get(index);
            Double metricPredictValue = metricsPredictiveValues.get(fieldName);
            doubleValue[index] = metricPredictValue == null ? 0D : metricPredictValue;
        }
        return Vectors.dense(doubleValue);
    }

    private LinkedHashMap<String, Double> getMetricsPredictionValues(CoreLearner coreLearner) {
        return JsonUtil.jsonToLinkedHashMap(coreLearner.getMetricsPredictiveValues());
    }
}

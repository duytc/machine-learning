package com.pubvantage.prediction;

import com.pubvantage.dao.ReportViewDao;
import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.CoreReportView;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.service.*;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.JsonUtil;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class VectorBuilder {
    private OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();
    private ReportViewServiceInterface reportViewService = new ReportViewService();

    private CoreLearner coreLearner;
    private CoreOptimizationRule optimizationRule;

    VectorBuilder(CoreLearner coreLearner, CoreOptimizationRule optimizationRule) {
        this.coreLearner = coreLearner;
        this.optimizationRule = optimizationRule;
    }

    /**
     * @return Vector data need for prediction
     */
    Vector buildVector() {
        List<String> segments = optimizationRuleService.getSegments(optimizationRule);
        Map<String, List<Double>> predictiveValues = getPredictionValues(coreLearner);


        List<Double> doubleList = new ArrayList<>();
        for (String segment : segments) {
            List<Double> values = predictiveValues.get(segment);
            doubleList.addAll(values);
        }

        OptimizeField optimizeField = JsonUtil.jsonToObject(coreLearner.getOptimizeFields(), OptimizeField.class);
        CoreReportView reportView = reportViewService.findById(optimizationRule.getReportViewId(), new ReportViewDao());
        List<String> metrics = reportViewService.getNoSpaceDigitMetrics(reportView, optimizeField.getField());

        if (predictiveValues == null)
            return null;
        for (String fieldName : metrics) {
            List<Double> values = predictiveValues.get(fieldName);
            doubleList.addAll(values);
        }
        double[] doubleValue = ConvertUtil.listDoubleToArray(doubleList);

        return Vectors.dense(doubleValue);
    }

    private Map<String, List<Double>> getPredictionValues(CoreLearner coreLearner) {
        return JsonUtil.jsonToMap(coreLearner.getMetricsPredictiveValues());
    }
}

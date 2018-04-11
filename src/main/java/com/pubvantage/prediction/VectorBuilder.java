package com.pubvantage.prediction;

import com.pubvantage.entity.CoreLearner;
import com.pubvantage.service.CoreLearningModelService;
import com.pubvantage.service.CoreLearningModelServiceInterface;
import com.pubvantage.utils.JsonUtil;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;

import java.util.List;
import java.util.Map;

public class VectorBuilder {
    private CoreLearningModelServiceInterface coreLearnerModelService = new CoreLearningModelService();
    private CoreLearner coreLearner;
    private Map<String, List<String>> convertedRule;
    private Map<String, String> segments;

    public VectorBuilder(CoreLearner coreLearner, Map<String, List<String>> convertedRule, Map<String, String> segments) {
        this.coreLearner = coreLearner;
        this.convertedRule = convertedRule;
        this.segments = segments;
    }

    /**
     * @return Vector data need for prediction
     */
    public Vector buildVector() {
        List<String> metrics = coreLearnerModelService.getMetricsFromCoreLeaner(coreLearner);
        double[] doubleValue = new double[metrics.size()];
        Map<String, Double> predictiveValues = getPredictionValues(coreLearner);
        if (predictiveValues == null)
            return null;
        for (int index = 0; index < metrics.size(); index++) {
            String fieldName = metrics.get(index);
            Double valueInPrediction;
            if (isSegment(this.convertedRule, fieldName)) {
                valueInPrediction = getSegmentDigitValue(this.convertedRule, fieldName, segments);
            } else {
                valueInPrediction = predictiveValues.get(fieldName);
            }
            doubleValue[index] = valueInPrediction == null ? 0D : valueInPrediction;
        }
        return Vectors.dense(doubleValue);
    }

    /**
     * value = index of field in convertedRule list due to 'converting text to digit by Frequency rule'
     * @param convertedRule Example: {country: [global, VN]}
     * @param fieldName     country
     * @param segments      {country: global}
     * @return index of global in [global, VN]
     */
    private Double getSegmentDigitValue(Map<String, List<String>> convertedRule,
                                        String fieldName, Map<String, String> segments) {
        List<String> convertedSegmentRule = convertedRule.get(fieldName);
        String segmentValue = segments.get(fieldName);
        int index = convertedSegmentRule.indexOf(segmentValue);

        return (double) index;
    }

    private boolean isSegment(Map<String, List<String>> convertedRule, String fieldName) {
        return convertedRule != null && convertedRule.get(fieldName) != null;
    }

    private Map<String, Double> getPredictionValues(CoreLearner coreLearner) {
        return JsonUtil.jsonToMap(coreLearner.getMetricsPredictiveValues());
    }
}

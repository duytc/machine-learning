package com.pubvantage.entity;


import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "core_learner")
public class CoreLearner {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "optimization_rule_id")
    private Long optimizationRuleId;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "segment_values")
    private String segmentValues;

    @Column(name = "optimize_fields")
    private String optimizeFields;

    @Column(name = "model_path")
    private String modelPath;

    @Column(name = "metrics_predictive_values")
    private String metricsPredictiveValues;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "updated_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    @Column(name = "math_model")
    private String mathModel;

    public CoreLearner() {
    }

    public Long getId() {
        return id;
    }

    public CoreLearner(Long optimizationRuleId, String identifier, String segmentValues, String modelPath, String metricsPredictiveValues, Date createdDate, Date updatedDate, String mathModel) {
        this.optimizationRuleId = optimizationRuleId;
        this.identifier = identifier;
        this.segmentValues = segmentValues;
        this.modelPath = modelPath;
        this.metricsPredictiveValues = metricsPredictiveValues;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.mathModel = mathModel;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOptimizationRuleId() {
        return optimizationRuleId;
    }

    public void setOptimizationRuleId(Long optimizationRuleId) {
        this.optimizationRuleId = optimizationRuleId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSegmentValues() {
        return segmentValues;
    }

    public void setSegmentValues(String segmentValues) {
        this.segmentValues = segmentValues;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getMetricsPredictiveValues() {
        return metricsPredictiveValues;
    }

    public void setMetricsPredictiveValues(String metricsPredictiveValues) {
        this.metricsPredictiveValues = metricsPredictiveValues;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getMathModel() {
        return mathModel;
    }

    public void setMathModel(String mathModel) {
        this.mathModel = mathModel;
    }

    public String getOptimizeFields() {
        return optimizeFields;
    }

    public void setOptimizeFields(String optimizeFields) {
        this.optimizeFields = optimizeFields;
    }

    @Override
    public String toString() {
        return "CoreLearner{" +
                "id=" + id +
                ", optimizationRuleId=" + optimizationRuleId +
                ", identifier='" + identifier + '\'' +
                ", segmentValues='" + segmentValues + '\'' +
                ", modelPath='" + modelPath + '\'' +
                ", metricsPredictiveValues='" + metricsPredictiveValues + '\'' +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                ", mathModel='" + mathModel + '\'' +
                '}';
    }
}

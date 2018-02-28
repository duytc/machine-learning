package com.pubvantage.entity;


import javax.persistence.*;

public class CoreLearner {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  private long id;

  @Column(name = "optimization_rule_id")
  private long optimizationRuleId;

  @Column(name = "identifier")
  private String identifier;

  @Column(name = "segment_values")
  private String segmentValues;

  @Column(name = "model_path")
  private String modelPath;

  @Column(name = "metric_predictive_values")
  private String metricsPredictiveValues;

  @Column(name = "created_date")
  @Temporal(TemporalType.TIMESTAMP)
  private java.sql.Timestamp createdDate;

  @Column(name = "updated_date")
  @Temporal(TemporalType.TIMESTAMP)
  private java.sql.Timestamp updatedDate;

  @Column(name = "math_model")
  private String mathModel;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public long getOptimizationRuleId() {
    return optimizationRuleId;
  }

  public void setOptimizationRuleId(long optimizationRuleId) {
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


  public java.sql.Timestamp getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(java.sql.Timestamp createdDate) {
    this.createdDate = createdDate;
  }


  public java.sql.Timestamp getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(java.sql.Timestamp updatedDate) {
    this.updatedDate = updatedDate;
  }


  public String getMathModel() {
    return mathModel;
  }

  public void setMathModel(String mathModel) {
    this.mathModel = mathModel;
  }

}

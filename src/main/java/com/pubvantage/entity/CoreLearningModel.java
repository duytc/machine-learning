package com.pubvantage.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "core_learner_model")
public class CoreLearningModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long id;

    @Column(name = "auto_optimization_config_id")
    private Long autoOptimizationConfigId;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "model")
    private String model;

    @Column(name = "type")
    private String type;

    @Column(name = "updated_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    @Column(name = "forecast_factor_values")
    private String forecastFactorValues;

    @Column(name = "categorical_field_weights")
    private String categoricalFieldWeights;

    @Column(name = "model_path")
    private String modelPath;


    public CoreLearningModel() {
    }

    public CoreLearningModel(Long autoOptimizationConfigId, String identifier, String model, String type, Date updatedDate, String forecastFactorValues, String categoricalFieldWeights) {
        this.autoOptimizationConfigId = autoOptimizationConfigId;
        this.identifier = identifier;
        this.model = model;
        this.type = type;
        this.updatedDate = updatedDate;
        this.forecastFactorValues = forecastFactorValues;
        this.categoricalFieldWeights = categoricalFieldWeights;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAutoOptimizationConfigId() {
        return autoOptimizationConfigId;
    }

    public void setAutoOptimizationConfigId(Long autoOptimizationConfigId) {
        this.autoOptimizationConfigId = autoOptimizationConfigId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getForecastFactorValues() {
        return forecastFactorValues;
    }

    public void setForecastFactorValues(String forecastFactorValues) {
        this.forecastFactorValues = forecastFactorValues;
    }

    public String getCategoricalFieldWeights() {
        return categoricalFieldWeights;
    }

    public void setCategoricalFieldWeights(String categoricalFieldWeights) {
        this.categoricalFieldWeights = categoricalFieldWeights;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModePath(String modelPath) {
        this.modelPath = modelPath;
    }
}

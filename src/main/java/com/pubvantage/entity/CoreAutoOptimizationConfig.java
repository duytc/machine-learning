package com.pubvantage.entity;


import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "core_auto_optimization_config")
public class CoreAutoOptimizationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(name = "publisher_id")
    private Long publisherId;
    @Column
    private String name;
    @Column
    private String transforms;
    @Column
    private String filters;
    @Column
    private String metrics;
    @Column
    private String dimensions;
    @Column(name = "field_types")
    private String fieldTypes;
    @Column(name = "join_by")
    private String joinBy;
    @Column
    private String factors;
    @Column
    private String objective;
    @Column(name = "expected_objective")
    private String expectedObjective;
    @Column(name = "date_range")
    private String dateRange;
    @Column
    private Boolean active;
    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    @Column
    private String identifiers;
    @Column(name = "positive_factors")
    private String positiveFactors;
    @Column(name = "negative_factors")
    private String negativeFactors;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTransforms() {
        return transforms;
    }

    public void setTransforms(String transforms) {
        this.transforms = transforms;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getFieldTypes() {
        return fieldTypes;
    }

    public void setFieldTypes(String fieldTypes) {
        this.fieldTypes = fieldTypes;
    }

    public String getJoinBy() {
        return joinBy;
    }

    public void setJoinBy(String joinBy) {
        this.joinBy = joinBy;
    }

    public String getFactors() {
        return factors;
    }

    public void setFactors(String factors) {
        this.factors = factors;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(String identifiers) {
        this.identifiers = identifiers;
    }

    public String getPositiveFactors() {
        return positiveFactors;
    }

    public void setPositiveFactors(String positiveFactors) {
        this.positiveFactors = positiveFactors;
    }

    public String getNegativeFactors() {
        return negativeFactors;
    }

    public void setNegativeFactors(String negativeFactors) {
        this.negativeFactors = negativeFactors;
    }

    public String getExpectedObjective() {
        return expectedObjective;
    }

    public void setExpectedObjective(String expectedObjective) {
        this.expectedObjective = expectedObjective;
    }
}

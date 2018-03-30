package com.pubvantage.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "core_optimization_rule")
public class CoreOptimizationRule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "report_view_id")
    private Long reportViewId;

    @Column(name = "publisher_id")
    private Long publisherId;

    @Column(name = "name")
    private String name;

    @Column(name = "date_field")
    private String dateField;

    @Column(name = "date_range")
    private String dateRange;

    @Column(name = "identifier_fields")
    private String identifierFields;

    @Column(name = "optimize_fields")
    private String optimizeFields;

    @Column(name = "segment_fields")
    private String segmentFields;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "token")
    private String token;

    @Column(name = "finish_loading")
    private Boolean finishLoading;

    public CoreOptimizationRule() {
    }

    public CoreOptimizationRule(Long reportViewId, Long publisherId, String name, String dateField, String dateRange, String identifierFields, String optimizeFields, String segmentFields, Date createdDate, String token, Boolean finishLoading) {
        this.reportViewId = reportViewId;
        this.publisherId = publisherId;
        this.name = name;
        this.dateField = dateField;
        this.dateRange = dateRange;
        this.identifierFields = identifierFields;
        this.optimizeFields = optimizeFields;
        this.segmentFields = segmentFields;
        this.createdDate = createdDate;
        this.token = token;
        this.finishLoading = finishLoading;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReportViewId() {
        return reportViewId;
    }

    public void setReportViewId(Long reportViewId) {
        this.reportViewId = reportViewId;
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

    public String getDateField() {
        return dateField;
    }

    public void setDateField(String dateField) {
        this.dateField = dateField;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public String getIdentifierFields() {
        return identifierFields;
    }

    public void setIdentifierFields(String identifierFields) {
        this.identifierFields = identifierFields;
    }

    public String getOptimizeFields() {
        return optimizeFields;
    }

    public void setOptimizeFields(String optimizeFields) {
        this.optimizeFields = optimizeFields;
    }

    public String getSegmentFields() {
        return segmentFields;
    }

    public void setSegmentFields(String segmentFields) {
        this.segmentFields = segmentFields;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getFinishLoading() { return finishLoading; }

    public void setFinishLoading(Boolean finishLoading) { this.finishLoading = finishLoading; }

    @Override
    public String toString() {
        return "CoreOptimizationRule{" +
                "id=" + id +
                ", reportViewId=" + reportViewId +
                ", publisherId=" + publisherId +
                ", name='" + name + '\'' +
                ", dateField='" + dateField + '\'' +
                ", dateRange='" + dateRange + '\'' +
                ", identifierFields='" + identifierFields + '\'' +
                ", optimizeFields='" + optimizeFields + '\'' +
                ", segmentFields='" + segmentFields + '\'' +
                ", createdDate=" + createdDate +
                ", token='" + token + '\'' +
                '}';
    }
}

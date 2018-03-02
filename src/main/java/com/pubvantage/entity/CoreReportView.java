package com.pubvantage.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "core_report_view")
public class CoreReportView {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "publisher_id")
    private Long publisherId;

    @Column(name = "master_report_view_id")
    private Long masterReportViewId;

    @Column(name = "transforms")
    private String transforms;

    @Column(name = "weighted_calculations")
    private String weightedCalculations;

    @Column(name = "metrics")
    private String metrics;

    @Column(name = "dimensions")
    private String dimensions;

    @Column(name = "name")
    private String name;

    @Column(name = "field_types")
    private String field_Types;

    @Column(name = "join_by")
    private String join_By;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created_Date;

    @Column(name = "shared_keys_config")
    private String sharedKeysConfig;

    @Column(name = "show_in_total")
    private String showInTotal;

    @Column(name = "formats")
    private String formats;

    @Column(name = "is_show_data_set_name")
    private Long isShowDataSetName;

    @Column(name = "last_activity")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastActivity;

    @Column(name = "last_run")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastRun;

    @Column(name = "enable_custom_dimension_metric")
    private Long enableCustomDimensionMetric;

    @Column(name = "sub_view")
    private Long subView;

    @Column(name = "filters")
    private String filters;

    @Column(name = "large_report")
    private Long largeReport;

    @Column(name = "available_to_run")
    private Long availableToRun;

    @Column(name = "available_to_change")
    private Long availableToChange;

    @Column(name = "pre_calculate_table")
    private String preCalculateTable;

    public CoreReportView() {
    }

    public CoreReportView(Long publisherId, Long masterReportViewId, String transforms, String weightedCalculations, String metrics, String dimensions, String name, String field_Types, String join_By, Date created_Date, String sharedKeysConfig, String showInTotal, String formats, Long isShowDataSetName, Date lastActivity, Date lastRun, Long enableCustomDimensionMetric, Long subView, String filters, Long largeReport, Long availableToRun, Long availableToChange, String preCalculateTable) {
        this.publisherId = publisherId;
        this.masterReportViewId = masterReportViewId;
        this.transforms = transforms;
        this.weightedCalculations = weightedCalculations;
        this.metrics = metrics;
        this.dimensions = dimensions;
        this.name = name;
        this.field_Types = field_Types;
        this.join_By = join_By;
        this.created_Date = created_Date;
        this.sharedKeysConfig = sharedKeysConfig;
        this.showInTotal = showInTotal;
        this.formats = formats;
        this.isShowDataSetName = isShowDataSetName;
        this.lastActivity = lastActivity;
        this.lastRun = lastRun;
        this.enableCustomDimensionMetric = enableCustomDimensionMetric;
        this.subView = subView;
        this.filters = filters;
        this.largeReport = largeReport;
        this.availableToRun = availableToRun;
        this.availableToChange = availableToChange;
        this.preCalculateTable = preCalculateTable;
    }

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

    public Long getMasterReportViewId() {
        return masterReportViewId;
    }

    public void setMasterReportViewId(Long masterReportViewId) {
        this.masterReportViewId = masterReportViewId;
    }

    public String getTransforms() {
        return transforms;
    }

    public void setTransforms(String transforms) {
        this.transforms = transforms;
    }

    public String getWeightedCalculations() {
        return weightedCalculations;
    }

    public void setWeightedCalculations(String weightedCalculations) {
        this.weightedCalculations = weightedCalculations;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getField_Types() {
        return field_Types;
    }

    public void setField_Types(String field_Types) {
        this.field_Types = field_Types;
    }

    public String getJoin_By() {
        return join_By;
    }

    public void setJoin_By(String join_By) {
        this.join_By = join_By;
    }

    public Date getCreated_Date() {
        return created_Date;
    }

    public void setCreated_Date(Date created_Date) {
        this.created_Date = created_Date;
    }

    public String getSharedKeysConfig() {
        return sharedKeysConfig;
    }

    public void setSharedKeysConfig(String sharedKeysConfig) {
        this.sharedKeysConfig = sharedKeysConfig;
    }

    public String getShowInTotal() {
        return showInTotal;
    }

    public void setShowInTotal(String showInTotal) {
        this.showInTotal = showInTotal;
    }

    public String getFormats() {
        return formats;
    }

    public void setFormats(String formats) {
        this.formats = formats;
    }

    public Long getIsShowDataSetName() {
        return isShowDataSetName;
    }

    public void setIsShowDataSetName(Long isShowDataSetName) {
        this.isShowDataSetName = isShowDataSetName;
    }

    public Date getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Date lastActivity) {
        this.lastActivity = lastActivity;
    }

    public Date getLastRun() {
        return lastRun;
    }

    public void setLastRun(Date lastRun) {
        this.lastRun = lastRun;
    }

    public Long getEnableCustomDimensionMetric() {
        return enableCustomDimensionMetric;
    }

    public void setEnableCustomDimensionMetric(Long enableCustomDimensionMetric) {
        this.enableCustomDimensionMetric = enableCustomDimensionMetric;
    }

    public Long getSubView() {
        return subView;
    }

    public void setSubView(Long subView) {
        this.subView = subView;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public Long getLargeReport() {
        return largeReport;
    }

    public void setLargeReport(Long largeReport) {
        this.largeReport = largeReport;
    }

    public Long getAvailableToRun() {
        return availableToRun;
    }

    public void setAvailableToRun(Long availableToRun) {
        this.availableToRun = availableToRun;
    }

    public Long getAvailableToChange() {
        return availableToChange;
    }

    public void setAvailableToChange(Long availableToChange) {
        this.availableToChange = availableToChange;
    }

    public String getPreCalculateTable() {
        return preCalculateTable;
    }

    public void setPreCalculateTable(String preCalculateTable) {
        this.preCalculateTable = preCalculateTable;
    }
}
package com.pubvantage.service;

import com.pubvantage.entity.CoreReportView;

import java.util.List;
import java.util.Map;

public interface ReportViewServiceInterface extends GenericServiceInterface<CoreReportView> {

    Map<String, String> getFieldsType(CoreReportView reportView);

    List<String> getDigitMetrics(CoreReportView reportView, String optimizeFieldName);

    List<String> getDimensions(CoreReportView reportView);

    List<String> getNoSpaceDigitMetrics(CoreReportView reportView, String optimizeFieldName);

    List<String> getNoSpaceDimensions(CoreReportView reportView);

}

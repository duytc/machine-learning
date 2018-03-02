package com.pubvantage.service;

import com.pubvantage.entity.CoreReportView;

public interface ReportViewServiceInterface {

    CoreReportView findById(Long reportViewId);
}

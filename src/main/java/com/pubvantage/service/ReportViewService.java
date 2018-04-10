package com.pubvantage.service;

import com.pubvantage.constant.MyConstant;
import com.pubvantage.dao.ReportViewDao;
import com.pubvantage.dao.ReportViewDaoInterface;
import com.pubvantage.entity.CoreReportView;
import com.pubvantage.utils.ConvertUtil;
import com.pubvantage.utils.HibernateUtil;
import com.pubvantage.utils.JsonUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by quyendq on 02/03/2018.
 */
public class ReportViewService extends AbstractGenericService<CoreReportView> implements ReportViewServiceInterface {

    @Override
    public Map<String, String> getFieldsType(CoreReportView reportView) {
        String jsonFieldType = reportView.getFieldTypes();
        return JsonUtil.jsonToMap(jsonFieldType);
    }

    @Override
    public List<String> getDigitMetrics(CoreReportView reportView, String optimizeFieldName) {
        List<String> metrics = JsonUtil.jsonArrayStringToJavaList(reportView.getMetrics());
        if (metrics == null || metrics.isEmpty()) return null;
        // Remove optimize field
        int indexOfOptimizeField = metrics.indexOf(optimizeFieldName);
        if (indexOfOptimizeField >= 0) {
            metrics.remove(indexOfOptimizeField);
        }
        return filterDigitFields(metrics, getFieldsType(reportView));
    }

    @Override
    public List<String> getDimensions(CoreReportView reportView) {
        return JsonUtil.jsonArrayStringToJavaList(reportView.getDimensions());
    }

    @Override
    public List<String> getNoSpaceDigitMetrics(CoreReportView reportView, String optimizeFieldName) {
        return ConvertUtil.removeSpace(getDigitMetrics(reportView, optimizeFieldName));
    }

    @Override
    public List<String> getNoSpaceDimensions(CoreReportView reportView) {
        return ConvertUtil.removeSpace(getDimensions(reportView));
    }

    private List<String> filterDigitFields(List<String> fieldsData, Map<String, String> fieldType) {
        List<String> filteredFields = new ArrayList<>();
        if (fieldsData != null && fieldType != null) {
            for (String metric : fieldsData) {
                String type = fieldType.get(metric);
                if (MyConstant.DECIMAL_TYPE.equals(type) || MyConstant.NUMBER_TYPE.equals(type)) {
                    filteredFields.add(metric);
                }
            }
        }
        return filteredFields;
    }

}

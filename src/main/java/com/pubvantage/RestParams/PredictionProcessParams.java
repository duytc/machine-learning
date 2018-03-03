package com.pubvantage.RestParams;

import com.google.gson.Gson;
import com.pubvantage.AppMain;
import com.pubvantage.entity.Condition;
import com.pubvantage.entity.FactorValues;
import com.pubvantage.entity.SegmentField;
import org.apache.log4j.Logger;

import java.util.List;

public class PredictionProcessParams {
    private static Logger logger = Logger.getLogger(AppMain.class.getName());
    private PredictionRequestParam requestParam;

    public PredictionProcessParams(String params) {
        try {
            requestParam = new Gson().fromJson(params, PredictionRequestParam.class);
        } catch (Exception e) {
            logger.error("Request is invalid");
        }
    }

    public Long getOptimizationRuleId() {
        if (requestParam == null)
            return null;

        return requestParam.getOptimizationRuleId();
    }

    public List<String> getIdentifiers() {
        if (requestParam == null)
            return null;

        return requestParam.getIdentifiers();
    }

    public Condition getConditions() {
        if (requestParam == null)
            return null;

        return requestParam.getConditions();
    }

    public String getToken() {
        if (requestParam == null)
            return null;

        return requestParam.getToken();
    }

    public boolean validates() {
        return null != requestParam
                && null != requestParam.getOptimizationRuleId()
                && null != requestParam.getIdentifiers()
                && null != requestParam.getConditions()
                && null != requestParam.getToken();
    }
}

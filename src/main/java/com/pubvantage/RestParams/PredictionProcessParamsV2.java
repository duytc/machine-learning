package com.pubvantage.RestParams;

import com.google.gson.Gson;
import com.pubvantage.AppMain;
import org.apache.log4j.Logger;

public class PredictionProcessParamsV2 {
    private static Logger logger = Logger.getLogger(AppMain.class.getName());
    private PredictionRequestParamV2 requestParam;

    public PredictionProcessParamsV2(String params) {
        try {
            requestParam = new Gson().fromJson(params, PredictionRequestParamV2.class);
        } catch (Exception e) {
            logger.error("Request is invalid");
        }
    }

    public Long getOptimizationRuleId() {
        if (requestParam == null)
            return null;

        return requestParam.getOptimizationRuleId();
    }

    public String getToken() {
        if (requestParam == null)
            return null;

        return requestParam.getToken();
    }

    public boolean validates() {
        return null != requestParam
                && null != requestParam.getOptimizationRuleId()
                && null != requestParam.getToken();
    }
}

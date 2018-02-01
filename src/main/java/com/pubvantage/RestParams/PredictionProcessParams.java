package com.pubvantage.RestParams;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.pubvantage.AppMain;
import org.apache.log4j.Logger;
import spark.Request;

import java.util.List;

public class PredictionProcessParams {
    private static Logger logger = Logger.getLogger(AppMain.class.getName());
    private PredictionRequestParam requestParam;

    public PredictionProcessParams(Request request) {
        try {
            requestParam = new Gson().fromJson(request.body(), PredictionRequestParam.class);
        } catch (Exception e) {
            logger.error("Request is invalid");
        }
    }

    public Long getAutoOptimizationConfigId() {
        if (requestParam == null)
            return null;

        return requestParam.getAutoOptimizationConfigId();
    }

    public List<String> getIdentifiers() {
        if (requestParam == null)
            return null;

        return requestParam.getIdentifiers();
    }

    public JsonArray getConditions() {
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
                && null != requestParam.getAutoOptimizationConfigId()
                && null != requestParam.getIdentifiers()
                && null != requestParam.getConditions()
                && null != requestParam.getToken();
    }
}

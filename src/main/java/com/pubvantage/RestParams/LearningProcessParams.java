package com.pubvantage.RestParams;

import com.google.gson.Gson;
import com.pubvantage.AppMain;
import org.apache.log4j.Logger;
import spark.Request;

public class LearningProcessParams {

    private static Logger logger = Logger.getLogger(AppMain.class.getName());

    private LearnerRequestParam requestParam;

    public LearningProcessParams(Request request) {
        try {
            requestParam = new Gson().fromJson(request.body(), LearnerRequestParam.class);
        } catch (Exception e) {
            logger.error("Request is invalid");
        }
    }

    public Long getAutoOptimizationConfigId() {
        if (requestParam == null)
            return null;

        return requestParam.getAutoOptimizationConfigId();
    }

    public String getToken() {
        if (requestParam == null)
            return null;

        return requestParam.getToken();
    }

    /**
     * @return true if parameters is valid. Otherwise, return false
     */
    public boolean validateParams() {
        return null != requestParam
                && null != requestParam.getAutoOptimizationConfigId()
                && 0 != requestParam.getAutoOptimizationConfigId()
                && null != requestParam.getToken()
                && !requestParam.getToken().isEmpty();

    }
}

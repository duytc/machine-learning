package com.pubvantage.RestParams;

import com.google.gson.Gson;
import com.pubvantage.AppMain;
import com.pubvantage.service.CoreOptimizationRuleService;
import com.pubvantage.service.CoreOptimizationRuleServiceInterface;
import org.apache.log4j.Logger;

public class LearningProcessParams {

    private static Logger logger = Logger.getLogger(AppMain.class.getName());

    private LearnerRequestParam requestParam;

    public LearningProcessParams(String stringJson) {
        // Check string json before call API

        try {
            requestParam = new Gson().fromJson(stringJson, LearnerRequestParam.class);
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

    /**
     * @return true if parameters is valid. Otherwise, return false
     */
    public boolean validateOptimizationRules()
    {
        return null != requestParam
                && null != requestParam.getOptimizationRuleId()
                && 0 != requestParam.getOptimizationRuleId()
                && null != requestParam.getToken()
                && !requestParam.getToken().isEmpty();
    }

    public boolean validateToken()
    {
        CoreOptimizationRuleServiceInterface sqlService = new CoreOptimizationRuleService();
        return sqlService.checkToken(requestParam.getOptimizationRuleId(), requestParam.getToken());
    }
}

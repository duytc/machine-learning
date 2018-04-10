package com.pubvantage.restparams;

import com.google.gson.Gson;
import com.pubvantage.service.OptimizationRuleService;
import com.pubvantage.service.OptimizationRuleServiceInterface;
import org.apache.log4j.Logger;

public class LearningProcessParams {

    private static Logger logger = Logger.getLogger(LearningProcessParams.class.getName());

    private LearnerRequestParam requestParam;

    public LearningProcessParams(String stringJson) {
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
        OptimizationRuleServiceInterface sqlService = new OptimizationRuleService();
        return sqlService.checkToken(requestParam.getOptimizationRuleId(), requestParam.getToken());
    }
}

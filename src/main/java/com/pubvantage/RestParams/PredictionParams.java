package com.pubvantage.RestParams;

import com.google.gson.JsonArray;
import spark.Request;

import java.util.Arrays;
import java.util.List;

public class PredictionParams {

    public PredictionParams(Request request) {

    }

    public Long getAutoOptimizationConfigId()
    {
        return 0l;
    }

    public List<String> getIdentifiers()
    {
        return Arrays.asList("identifier1", "identifier2", "identifier3");
    }

    public JsonArray getConditions()
    {

        return new JsonArray();
    }
}

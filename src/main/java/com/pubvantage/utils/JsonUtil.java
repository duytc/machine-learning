package com.pubvantage.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import spark.ResponseTransformer;

public class JsonUtil {
    public static String toJson(Object object) {
        return new Gson().toJson(object);
    }

    public static ResponseTransformer json() {
        return JsonUtil::toJson;
    }

    public static JsonArray toJsonArray(String[] array) {
        if (array == null || array.length == 0) {
            return new JsonArray();
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().setLenient().create();
        String daysJson = gson.toJson(array);
        JsonParser jsonParser = new JsonParser();
        return jsonParser.parse(daysJson).getAsJsonArray();
    }
}

package com.pubvantage.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.jsoniter.JsonIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {
    public static String toJson(Object object) {
        return new Gson().toJson(object);
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

    public static List<String> jsonArrayStringToJavaList(String jsonArrayString) {
        return JsonIterator.deserialize(jsonArrayString, ArrayList.class);
    }

    public static List<HashMap<String, String>> jsonArrayObjectsToListMap(String jsonArrayObjects) {
        return JsonIterator.deserialize(jsonArrayObjects, ArrayList.class);
    }

    public static String mapToJson(Map map) {
        if (map == null)
            return null;
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(map);
    }

    public static <K, V> Map<K, V> jsonToMap(String json) {
        if (json == null)
            return null;
        return JsonIterator.deserialize(json, Map.class);
    }

    public static <T> T jsonToObject(String json, Class<T> type) {
        return JsonIterator.deserialize(json, type);
    }

}

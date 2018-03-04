package com.pubvantage.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.jsoniter.JsonIterator;
import spark.ResponseTransformer;

import java.io.InputStreamReader;
import java.util.*;

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
//
//    /**
//     * convert string json to array list.NOTE: error occur when string has special characters
//     * @param jsonArrayString
//     * @return
//     */
//    public static ArrayList<String> jsonArrayStringToJavaList(String jsonArrayString) {
//        JsonParser jsonParser = new JsonParser();
//        JsonArray arrayFromString = jsonParser.parse(jsonArrayString).getAsJsonArray();
//        ArrayList<String> arrayList = new Gson().fromJson(arrayFromString, ArrayList.class);
//        return arrayList;
//    }

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
        return JsonIterator.deserialize(json, Map.class);
    }

    public static <K, V> LinkedHashMap<K, V> jsonToLinkedHashMap(String json) {
        return JsonIterator.deserialize(json, LinkedHashMap.class);
    }

    public static <T> T jsonToObject(String json, Class<T> type) {
        return JsonIterator.deserialize(json, type);
    }
}

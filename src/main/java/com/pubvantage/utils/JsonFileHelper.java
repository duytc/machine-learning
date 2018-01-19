package com.pubvantage.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonFileHelper {
    private static Logger logger = Logger.getLogger(JsonFileHelper.class.getName());

    /**
     * write data to json file
     *
     * @param path path of file
     * @param data input data need to be saved
     */
    public static void writeToFile(String path, String data) {
        try {
            FileWriter writer = new FileWriter(path);
            writer.write(data);
            writer.close();

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * get data from json
     *
     * @param path     path of file
     * @param fileName file's name
     * @return data in file
     */
    public static JsonObject getJsonFromFile(String path, String fileName) {
        StringBuilder stringBuilder = new StringBuilder(path);
        stringBuilder.append("/").append(fileName).append(".json");

        JsonObject jsonObject = new JsonObject();
        Gson gson = new Gson();
        try {
            BufferedReader br = new BufferedReader(
                    new FileReader(stringBuilder.toString()));

            jsonObject = gson.fromJson(br, JsonObject.class);


        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return jsonObject;
    }
}

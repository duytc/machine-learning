package com.pubvantage.restparams;

import com.google.gson.JsonElement;

public class LearnerResponse {

    private int status;
    private String message;
    private JsonElement data;

    public LearnerResponse() {

    }

    public LearnerResponse(int status, String message, JsonElement data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }
}

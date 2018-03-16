package com.pubvantage.thread;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by quyendq on 16/03/2018.
 */
public class TestRunable implements Runnable {
    private ConcurrentHashMap<String, Double> map;
    private Double input;
    private String key;

    public TestRunable() {
    }

    public TestRunable(ConcurrentHashMap<String, Double> map, Double input, String key) {
        this.map = map;
        this.input = input;
        this.key = key;
    }

    @Override
    public void run() {
        map.put(key, input);
    }
}

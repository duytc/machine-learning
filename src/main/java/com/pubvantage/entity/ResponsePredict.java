package com.pubvantage.entity;

import java.util.List;

/**
 * Created by quyendq on 04/03/2018.
 */
public class ResponsePredict {
    private Long id;
    private List<PredictScore> info;

    public ResponsePredict() {
    }

    public ResponsePredict(Long id, List<PredictScore> info) {
        this.id = id;
        this.info = info;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<PredictScore> getInfo() {
        return info;
    }

    public void setInfo(List<PredictScore> info) {
        this.info = info;
    }
}

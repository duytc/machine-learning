package com.pubvantage.entity;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.spark.util.AccumulatorV2;

import java.io.Serializable;
import java.util.Map;

public class DoubleAccumulator extends AccumulatorV2<Map<Integer, Double>, Double[]> implements Serializable {
    private Double[] listValue;
    private int size;

    public DoubleAccumulator(Double[] listValue, int size) {
        this.listValue = listValue;
        this.size = size;
    }

    public DoubleAccumulator(int size) {
        this.size = size;
        this.listValue = new Double[size];
    }

    public DoubleAccumulator(Double[] listValue, int[] listIndex) {
        this.listValue = listValue;
        this.size = listIndex.length;
    }

    public DoubleAccumulator() {
        this.size = 0;
        this.listValue = new Double[this.size];

    }

    @Override
    public boolean isZero() {
        return this.listValue == null || this.listValue.length == 0;
    }

    @Override
    public AccumulatorV2<Map<Integer, Double>, Double[]> copy() {
        return new DoubleAccumulator(this.listValue.clone(), this.size);
    }

    @Override
    public void reset() {
        this.size = 0;
        this.listValue = new Double[this.size];
    }

    @Override
    public void add(Map<Integer, Double> map) {
        for (Map.Entry<Integer, Double> entry : map.entrySet()) {
            this.listValue[entry.getKey()] = entry.getValue();
        }
    }

    @Override
    public void merge(AccumulatorV2<Map<Integer, Double>, Double[]> other) {
        Double[] newListValue = ArrayUtils.addAll(other.value());
        this.listValue = newListValue;
        this.size = this.listValue.length;
    }

    @Override
    public Double[] value() {
        return this.listValue;
    }
}

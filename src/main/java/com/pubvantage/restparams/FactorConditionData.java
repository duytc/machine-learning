package com.pubvantage.restparams;

import java.util.List;

public class FactorConditionData {
    private String factor;
    private List<Object> values;
    private boolean isAll;

    public FactorConditionData(String factor, List<Object> values, boolean isAll) {
        this.factor = factor;
        this.values = values;
        this.isAll = isAll;
    }

    public FactorConditionData() {
    }

    public String getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = factor;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }

    public boolean getIsAll() {
        return isAll;
    }

    public void setIsAll(boolean all) {
        isAll = all;
    }

}

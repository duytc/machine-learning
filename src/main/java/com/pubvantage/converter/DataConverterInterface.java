package com.pubvantage.converter;

import com.pubvantage.entity.ConvertedDataWrapper;

public interface DataConverterInterface {
    ConvertedDataWrapper doConvert(long autoOptimizationId, String identifier);
}

package com.pubvantage.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;

public class ConvertUtil {
    private static AppResource appResource;
    private static Properties properties;

    static {
        appResource = new AppResource();
        properties = appResource.getPropValues();
    }

    public static Double truncate(Double numble, int scale) {
        Double value = BigDecimal.valueOf(numble)
                .setScale(scale, RoundingMode.HALF_UP)
                .doubleValue();

        return value;
    }

    public static Double convertObjectToDouble(Object obj) {
        int scale = Integer.parseInt(properties.getProperty("number.scale"));

        try {
            return truncate(Double.parseDouble(obj.toString()), scale);
        } catch (Exception e) {
            return truncate(0d, scale);
        }


    }

    public static BigDecimal convertObjectToDecimal(Object object) {
        int scale = Integer.parseInt(properties.getProperty("number.scale"));
        Double input = 0d;
        if (input instanceof Number) {
            input = Double.parseDouble(object.toString());
        }
        return BigDecimal.valueOf(input)
                .setScale(scale, RoundingMode.HALF_UP);

    }
}

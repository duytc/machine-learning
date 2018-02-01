package com.pubvantage.utils;

import com.pubvantage.entity.FactorDataType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ConvertUtil {
    private static Properties properties;

    static {
        AppResource appResource = new AppResource();
        properties = appResource.getPropValues();
    }
    /**
     * @param number an double value. Example: 1.2345678
     * @param scale  a number. Example 3
     * @return 1.234
     */
    private static Double truncate(Double number, int scale) {
        return BigDecimal.valueOf(number)
                .setScale(scale, RoundingMode.HALF_UP)
                .doubleValue();
    }
    /**
     * @param obj an object
     * @return Double value of object
     */
    public static Double convertObjectToDouble(Object obj) {
        int scale = Integer.parseInt(properties.getProperty("number.scale"));
        try {
            return truncate(Double.parseDouble(obj.toString()), scale);
        } catch (Exception e) {
            return truncate(0d, scale);
        }
    }
    /**
     * @param object an object
     * @return decimal value of object
     */
    public static BigDecimal convertObjectToDecimal(Object object) {
        int scale = Integer.parseInt(properties.getProperty("number.scale"));
        Double input = Double.parseDouble(object.toString());
        return BigDecimal.valueOf(input)
                .setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * @param input a double value. Example : 1.23456789987
     * @return 1.234567899. default scale is 9
     */
    public static Double scaleDouble(Double input) {
        int scale = Integer.parseInt(properties.getProperty("number.scale"));
        return BigDecimal.valueOf(input)
                .setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }
    /**
     * @param inputType input string
     * @return check if inputType is Number
     */
    public static boolean isTextOrDate(String inputType) {
        return FactorDataType.TEXT.equals(inputType) || FactorDataType.DATE.equals(inputType);
    }
    /**
     * @param inputType input string
     * @return check if inputType is Number
     */
    public static boolean isNumberFactor(String inputType) {
        return FactorDataType.NUMBER.equals(inputType) || FactorDataType.DECIMAL.equals(inputType);
    }


    /**
     * @param unsortedMap unsorted map
     * @param <K>         type of key
     * @param <V>         type of value
     * @return ascending sorted by value map
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> ascendingSortMapByValue(Map<K, V> unsortedMap) {
        List<Map.Entry<K, V>> list = new LinkedList<>(unsortedMap.entrySet());
        Collections.sort(list, Comparator.comparing(o -> (o.getValue())));
        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }


    /**
     * @param unsortedMap unsorted map
     * @param <K>         type of key
     * @param <V>         type of value
     * @return descending sorted by value map
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> descendingSortMapByValue(Map<K, V> unsortedMap) {
        List<Map.Entry<K, V>> list = new LinkedList<>(unsortedMap.entrySet());
        Collections.sort(list, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));
        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    /**
     *
     * @param listFactors list of factors
     * @return SQL String check all factors is not null
     */
    public static String generateAllIsNoteNull(String[] listFactors) {
        String concatString = String.join(" IS NOT NULL AND ", listFactors);
        String string2 = concatString + " IS NOT NULL";

        return string2;
    }

}
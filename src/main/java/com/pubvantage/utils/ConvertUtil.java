package com.pubvantage.utils;

import com.pubvantage.constant.MyConstant;
import com.pubvantage.entity.FactorDataType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
     * @param listFactors list of factors
     * @return SQL String check all factors is not null
     */
    public static String generateAllIsNoteNull(String[] listFactors) {
        String concatString = String.join(" IS NOT NULL AND ", listFactors);
        String string2 = concatString + " IS NOT NULL";

        return string2;
    }

    public static String generateAllIsNoteNull(List<String> list) {
        String concatString = String.join(" IS NOT NULL AND ", list);
        String string2 = concatString + " IS NOT NULL";

        return string2;
    }

    public static List<List<String>> generateSubsets(List<String> set) {
        List<List<String>> result = new ArrayList<>();
        result.add(null);
        int size = set.size();
        for (int i = 1; i < (1 << size); i++) {
            List<String> subSet = new ArrayList<>();
            //skip empty set so start from 1
            System.out.print("{ ");
            // Print current subset
            for (int j = 0; j < size; j++) {
                // (1<<j) is a number with jth bit 1
                // so when we 'and' them with the
                // subset number we get which numbers
                // are present in the subset and which
                // are not
                if ((i & (1 << j)) > 0) {
                    System.out.print(set.get(j) + " ");
                    subSet.add(set.get(j));
                }
            }
            System.out.println("}");
            result.add(subSet);
        }
        //for run global

        return result;
    }

    public static String joinListString(List<String> list, String separate) {
        return list.stream()
                .map(i -> i.toString())
                .collect(Collectors.joining(separate));
    }

    public static String buildSUMQuery(String field) {
        return " SUM(" + field + ")" + " AS " + field + " ";
    }

    public static List<String> buildListSUMQuery(List<String> list) {
        List<String> result = new ArrayList<>();
        for (String item : list) {
            result.add(buildSUMQuery(item));
        }
        return result;
    }

    public static String mapValueToString(Map<String, Object> map) {
        List<String> list = new ArrayList<>();
        map.forEach((key, value) -> list.add(value.toString()));
        return joinListString(list, "-");
    }

    public static <V extends Comparable<V>> boolean valuesEquals(Map<?, V> map1, Map<?, V> map2) {
        List<V> values1 = new ArrayList<>(map1.values());
        List<V> values2 = new ArrayList<>(map2.values());
        Collections.sort(values1);
        Collections.sort(values2);
        return values1.equals(values2);
    }

    public static String buildInsertValueQuery(List<String> columns) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(":");
        stringBuilder.append(joinListString(columns, ",:"));
        return stringBuilder.toString();
    }

    public static List<String> concatParamUpdateQuery(List<String> field) {
        List<String> result = new ArrayList<>();
        for (String aField : field) {
            if (MyConstant.SCORE_ID.equals(aField)) continue;
            result.add(aField + " = :" + aField);
        }
        return result;
    }

    public static Date nextDay(Date dt) {
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.DATE, 1);
        dt = c.getTime();
        return dt;
    }

    public static String dateToString(Date date, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static String removeSpace(String field) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char ch : field.toCharArray()) {
            if (ch != ' ') {
                stringBuilder.append(ch);
            } else {
                stringBuilder.append('_');
            }
        }
        return stringBuilder.toString();
    }

    /**
     * @param list ["days 2", "sum 1"]
     * @return ["days_2", "sum_1"]
     */

    public static List<String> removeSpace(List<String> list) {
        List<String> noSpaceList = new ArrayList<>();
        for (String field : list) {
            noSpaceList.add(removeSpace(field));
        }
        return noSpaceList;
    }
    /**
     * @param avgString avg(xxx)
     * @return xxx
     */
    public static String removeAvg(String avgString) {
        if (avgString == null || avgString.length() < 4) return "";
        return avgString.substring(4, avgString.length() - 1);
    }

    public static List<String> removeAvg(List<String> listAvg){
        List<String> plain = new ArrayList<>();
        for (String item: listAvg) {
            plain.add(removeAvg(item));
        }
        return plain;
    }
}
package com.pubvantage.utils;

import com.pubvantage.constant.MyConstant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
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

        return concatString + " IS NOT NULL";
    }

    public static String generateAllIsNoteNull(List<String> list) {
        String concatString = String.join(" IS NOT NULL AND ", list);

        return concatString + " IS NOT NULL";
    }

    /**
     * @param list list of dimensions. Example: [a, b]
     * @param type AND or OR
     * @return Example: a = 'Global' AND b = 'Global'
     */
    public static String generateAllIsGlobal(List<String> list, String type) {
        if (list == null || list.isEmpty()) return "";
        return String.join(" = '" +
                MyConstant.GLOBAL_DIMENSION_VALUE + "' " + type + " ", list) +
                " = '" + MyConstant.GLOBAL_DIMENSION_VALUE + "' ";
    }

    /**
     * @param set Example: [a,b]
     * @return [null, [a], [b], [a,b]]
     */
    public static List<List<String>> generateSubsets(List<String> set) {
        List<List<String>> result = new ArrayList<>();
        result.add(null);
        int size = set.size();
        for (int i = 1; i < (1 << size); i++) {
            List<String> subSet = new ArrayList<>();
            // Print current subset
            for (int j = 0; j < size; j++) {
                // (1<<j) is a number with jth bit 1
                // so when we 'and' them with the
                // subset number we get which numbers
                // are present in the subset and which
                // are not
                if ((i & (1 << j)) > 0) {
                    subSet.add(set.get(j));
                }
            }
            result.add(subSet);
        }
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

    public static String buildInsertValueQuery(List<String> columns) {
        return ":" + joinListString(columns, ",:");
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

    /**
     * @param date   a date value
     * @param format date format
     * @return string value of date
     */
    public static String dateToString(Date date, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    /**
     * @param field Example: days 2
     * @return days_2
     */
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
     * This use for label of column need to  convert text to digit
     *
     * @param list  ["a", "b"]
     * @param index "Index"
     * @return ["a Index", "b Index"]
     */
    public static List<String> concatIndex(List<String> list, String index) {
        if (list == null) return null;
        List<String> newList = new ArrayList<>();
        for (String item : list) {
            newList.add(item + index);
        }
        return newList;
    }

    /**
     * This use for label of column need to  convert text to digit
     *
     * @param list        ["a", "b"]
     * @param indexString "Index"
     * @return ["a Index", "b Index"]
     */
    public static String[] concatIndexToArray(List<String> list, String indexString) {
        if (list == null) return null;
        String[] newArray = new String[list.size()];
        int i = -1;
        for (String item : list) {
            i++;
            newArray[i] = item + indexString;
        }
        return newArray;
    }

    /**
     * @param avgString avg(xxx)
     * @return xxx
     */
    public static String removeAvg(String avgString) {
        if (avgString == null || avgString.length() < 4) return "";
        return avgString.substring(4, avgString.length() - 1);
    }


    public static String concatMax(String string) {
        if (string == null) return null;
        return "max(" + string + ")";
    }

    public static List<String> removeAvg(List<String> listAvg) {
        List<String> plain = new ArrayList<>();
        for (String item : listAvg) {
            plain.add(removeAvg(item));
        }
        return plain;
    }

    /**
     * @param segmentData Example: { country: ["VN", "global"], domain: ["a.com", "global"]
     * @return [{country: "VN", domain: "a.com"}, {country: "global", domain: "a.com"}, ...]
     */
    public static List<Map<String, String>> generateAllSegmentPair(Map<String, List<String>> segmentData) {
        if (segmentData == null) return null;
        List<String> segmentFields = new ArrayList<>(segmentData.keySet());
        int size = segmentFields.size();
        List<Map<String, String>> listSegmentGroup = new ArrayList<>();
        Map<String, String> stepMap = new HashMap<>();
        backTrack(0, size, segmentData, segmentFields, stepMap, listSegmentGroup);

        return listSegmentGroup;
    }

    private static void backTrack(int step,
                                  int size,
                                  Map<String, List<String>> segmentData,
                                  List<String> segmentFields,
                                  Map<String, String> stepMap,
                                  List<Map<String, String>> listSegmentGroup) {
        if (step == size) {
            Map<String, String> copy = new HashMap<>(stepMap);
            listSegmentGroup.add(copy);
            return;
        }
        String segmentField = segmentFields.get(step);
        List<String> segmentValues = segmentData.get(segmentField);
        for (String value : segmentValues) {
            stepMap.put(segmentField, value);
            backTrack(step + 1, size, segmentData, segmentFields, stepMap, listSegmentGroup);
            stepMap.put(segmentField, null);
        }
    }

    /**
     * add future date to listDate. The day after the latest date in data training
     *
     * @param listDate list of date in data training
     */
    public static String addFutureDate(List<String> listDate) {
        try {
            if (listDate == null || listDate.isEmpty()) {
                Date today = new Date();
                Date nextDay = ConvertUtil.nextDay(today);
                String nextDayString = ConvertUtil.dateToString(nextDay, MyConstant.DATE_FORMAT_JAVA);
                listDate = new ArrayList<>();
                listDate.add(nextDayString);
                return nextDayString;
            }

            java.util.Collections.sort(listDate);
            String latestDate = listDate.get(listDate.size() - 1);
            Date date1 = new SimpleDateFormat(MyConstant.DATE_FORMAT_JAVA).parse(latestDate);
            Date nextDay = ConvertUtil.nextDay(date1);
            String nextDayString = ConvertUtil.dateToString(nextDay, MyConstant.DATE_FORMAT_JAVA);
            listDate.add(nextDayString);
            return nextDayString;

        } catch (ParseException e) {
            e.printStackTrace();
            Date today = new Date();
            Date nextDay = ConvertUtil.nextDay(today);
            String nextDayString = ConvertUtil.dateToString(nextDay, MyConstant.DATE_FORMAT_JAVA);
            listDate.add(nextDayString);
            return nextDayString;
        }
    }
}
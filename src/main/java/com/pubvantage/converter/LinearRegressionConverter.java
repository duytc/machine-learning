package com.pubvantage.converter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubvantage.entity.ConvertedDataWrapper;
import com.pubvantage.entity.FactorDataType;
import com.pubvantage.service.CoreAutoOptimizationConfigService;
import com.pubvantage.service.CoreAutoOptimizationConfigServiceInterface;
import com.pubvantage.service.DataTrainingService;
import com.pubvantage.service.DataTrainingServiceInterface;
import com.pubvantage.utils.ConvertUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

import java.util.*;

public class LinearRegressionConverter implements DataConverterInterface {
    private CoreAutoOptimizationConfigServiceInterface autoOptimizationConfigService;
    private DataTrainingServiceInterface dataTrainingService;
    private String[] orderFieldType;
    private int[] positiveOrder;
    private int[] negativeOrder;
    private String objectiveAndFactor[];
    private final int objectiveIndex = 0;
    private List<String> numberTypeFactors;
    private List<String> textAndDateFactors;

    private final int POSITIVE = 1;
    private final int NEGATIVE = -1;

    public LinearRegressionConverter() {
        autoOptimizationConfigService = new CoreAutoOptimizationConfigService();
        dataTrainingService = new DataTrainingService();
    }

    /**
     * convert data to learn
     *
     * @param autoOptimizationId input autoOptimizationId
     * @param identifier         input identifier
     */
    public ConvertedDataWrapper doConvert(long autoOptimizationId, String identifier) {
        ConvertedDataWrapper convertedDataWrapper = new ConvertedDataWrapper();
        //[objective, factor1, factor2,...]
        objectiveAndFactor = autoOptimizationConfigService.getObjectiveAndFactors(autoOptimizationId);

        //prepare field type data
        JsonObject fieldType = autoOptimizationConfigService.getFieldType(autoOptimizationId);
        orderFieldType = generateOrderFieldType(fieldType);
        numberTypeFactors = generateNumberTypeFactors(objectiveAndFactor);
        textAndDateFactors = generateTextFactors(objectiveAndFactor);
        // get data from DB
        Dataset<Row> trainingDataSet = dataTrainingService.getDataSetByTable(autoOptimizationId, identifier, objectiveAndFactor);
//        trainingDataSet.show();
        if (trainingDataSet == null || trainingDataSet.count() == 0) {
            return null;
        }
        //prepare positive, negative, objective
        List<String> positiveFactors = autoOptimizationConfigService.getPositiveFactors(autoOptimizationId);
        List<String> negativeFactors = autoOptimizationConfigService.getNegativeFactors(autoOptimizationId);
        positiveFactors = handleBothPositiveAndNegativeFactorIsEmpty(positiveFactors, negativeFactors, objectiveAndFactor);
        positiveOrder = new int[objectiveAndFactor.length];
        positiveOrder = generateArrayFollowByFactorOrder(positiveFactors, POSITIVE);
        negativeOrder = new int[objectiveAndFactor.length];
        negativeOrder = generateArrayFollowByFactorOrder(negativeFactors, NEGATIVE);

        //each factor: group by -> sum
        List<Dataset<Row>> sumNumberedDataByTextGroups = sumNumberedDataByTextGroup(trainingDataSet);
        Map<Integer, Map<String, Double>> textValueMap = computeTextValue(sumNumberedDataByTextGroups);
        //save data set
        convertedDataWrapper.setDataSet(convertedData(trainingDataSet, textValueMap));
        //category
        convertedDataWrapper.setCategoryWeight(generateCategoryFieldWeight(textValueMap));
        //forecast
        convertedDataWrapper.setForecast(generateForecastFactorValuesLocal(textValueMap, trainingDataSet));
        //save objectiveAndFactor
        convertedDataWrapper.setObjectiveAndFactors(Arrays.asList(objectiveAndFactor));
        return convertedDataWrapper;
    }

    /**
     * @param positiveFactors    positive factors
     * @param negativeFactors    negative factors
     * @param objectiveAndFactor objective
     * @return If both positiveFactors and negativeFactors are null -> all factors are positive
     */
    private List<String> handleBothPositiveAndNegativeFactorIsEmpty(List<String> positiveFactors, List<String> negativeFactors, String[] objectiveAndFactor) {
        if ((positiveFactors == null || positiveFactors.isEmpty()) && (negativeFactors == null || negativeFactors.isEmpty())) {
            positiveFactors = new ArrayList<>();
            for (int i = 1; i < objectiveAndFactor.length; i++) {
                if (FactorDataType.NUMBER.equals(orderFieldType[i]) || FactorDataType.DECIMAL.equals(orderFieldType[i]))
                    positiveFactors.add(objectiveAndFactor[i]);
            }
        }
        return positiveFactors;
    }

    /**
     * @param trainingDataSet training data
     * @param textValueMap    converted text data (text -> number)
     * @return data set contain all value is number
     */

    private Dataset<Row> convertedData(Dataset<Row> trainingDataSet, Map<Integer, Map<String, Double>> textValueMap) {
        StructType structType = new StructType();
        for (String anObjectiveAndFactor : objectiveAndFactor) {
            structType = structType.add(anObjectiveAndFactor, DataTypes.DoubleType, false);
        }
        ExpressionEncoder<Row> encoder = RowEncoder.apply(structType);

        final String[] objectiveAndFactor = this.objectiveAndFactor;
        final int objIndex = this.objectiveIndex;
        final String[] orderFieldType = this.orderFieldType;

        Dataset<Row> output = trainingDataSet.flatMap((FlatMapFunction<Row, Row>) rowData -> {
            Double[] rowDataToWrite = new Double[objectiveAndFactor.length];
            rowDataToWrite[0] = 0d;
            if (rowData.get(objIndex) instanceof Number) {
                rowDataToWrite[0] = ConvertUtil.scaleDouble(Double.parseDouble(rowData.get(objIndex).toString()));
            }
            int colIndex = 1;
            int textFactorIndex = -1;
            for (int col = 0; col < objectiveAndFactor.length; col++) {
                if (FactorDataType.TEXT.equals(orderFieldType[col]) || FactorDataType.DATE.equals(orderFieldType[col])) {
                    textFactorIndex++;
                    String data = rowData.get(col).toString();
                    Double effort = textValueMap.get(textFactorIndex).get(data);
                    rowDataToWrite[colIndex++] = effort;
                } else {
                    if (col != objIndex) {
                        Double value = 0d;
                        if (rowData.get(col) instanceof Number) {
                            value = ConvertUtil.scaleDouble(Double.parseDouble(rowData.get(col).toString()));
                        }
                        rowDataToWrite[colIndex++] = value;
                    }
                }
            }

            List<Double> data = new ArrayList<>(Arrays.asList(rowDataToWrite));
            ArrayList<Row> list = new ArrayList<>();
            list.add(RowFactory.create(data.toArray()));
            return list.iterator();
        }, encoder);

        List<Row> result = output.collectAsList();
        for (Row row : result) {
            for (int i = 0; i < row.length(); i++) {
                System.out.print(" " + row.get(i).toString());
            }
            System.out.println();
        }

        return output;
    }

    /**
     * @param sumData data after group by -> sum
     * @return data after converted text & date to number
     */
    private Map<Integer, Map<String, Double>> computeTextValue(List<Dataset<Row>> sumData) {
        Map<Integer, Map<String, Double>> mapList = new LinkedHashMap<>();

        for (int index = 0; index < sumData.size(); index++) {
            Map<String, Double> textFactorMap = new LinkedHashMap<>();
            List<Row> rowList = sumData.get(index).collectAsList();

            for (Row row : rowList) {
                double positiveSubtractNegativeSum = 0;
                double objectiveSum = ConvertUtil.convertObjectToDouble(row.get(objectiveIndex + 1));
                double result = 0;

                for (int colIndex = 1; colIndex < row.size(); colIndex++) {
                    String name = getNumberTypeColumnName(colIndex - 1);
                    int orderIndex = getOrderIndexByName(name);

                    if (positiveOrder[orderIndex] == POSITIVE) {
                        positiveSubtractNegativeSum += ConvertUtil.convertObjectToDouble(row.get(colIndex));
                    } else if (negativeOrder[orderIndex] == NEGATIVE) {
                        positiveSubtractNegativeSum -= ConvertUtil.convertObjectToDouble(row.get(colIndex));
                    }
                }

                if (positiveSubtractNegativeSum != 0) {
                    result = objectiveSum / positiveSubtractNegativeSum;
                }
                textFactorMap.put(row.get(0).toString(), result);
            }
            mapList.put(index, textFactorMap);

        }
        return mapList;
    }

    /**
     * @param name name of factor
     * @return index of factor
     */
    private int getOrderIndexByName(String name) {
        return ArrayUtils.indexOf(objectiveAndFactor, name);
    }

    /**
     * @param colIndex index
     * @return name of number type factor
     */
    private String getNumberTypeColumnName(int colIndex) {
        return numberTypeFactors.get(colIndex);
    }

    /**
     * @param trainingDataSet training data
     * @return sum data: each factor : group by -> sum
     */
    private List<Dataset<Row>> sumNumberedDataByTextGroup(Dataset<Row> trainingDataSet) {
        List<Dataset<Row>> dataSetList = new ArrayList<>();

        //check text and date type factors. 0 is objective (is number)
        for (int i = 1; i < objectiveAndFactor.length; i++) {
            if (FactorDataType.TEXT.equals(orderFieldType[i]) || FactorDataType.DATE.equals(orderFieldType[i])) {

                Map<String, String> map = new LinkedHashMap<>();
                for (int col = 0; col < objectiveAndFactor.length; col++) {
                    if (!FactorDataType.TEXT.equals(orderFieldType[col]) && !FactorDataType.DATE.equals(orderFieldType[col])) {
                        map.put(objectiveAndFactor[col], "sum");
                    }
                }
                Dataset<Row> textGroupData = trainingDataSet.groupBy(objectiveAndFactor[i]).agg(map);
                dataSetList.add(textGroupData);
                textGroupData.show();
            }

        }
        return dataSetList;
    }


    /**
     * @param textValueMap    data after convert text to number
     * @param trainingDataSet training data
     * @return forecast data
     */
    private JsonObject generateForecastFactorValuesLocal(Map<Integer, Map<String, Double>> textValueMap, Dataset<Row> trainingDataSet) {

        double[] forecastFactorValues = new double[objectiveAndFactor.length];
        //forecast  number value factor. (avg)
        Dataset<Row> avgDataSet = avgNumberedData(trainingDataSet);
        String[] objectiveAndFactor = this.objectiveAndFactor;
        final String[] orderFieldType = this.orderFieldType;

        List<Row> data = avgDataSet.collectAsList();
        for (Row row : data) {
            int numberTypeFactorIndex = -1;
            for (int col = 0; col < objectiveAndFactor.length; col++) {
                if (!FactorDataType.TEXT.equals(orderFieldType[col]) && !FactorDataType.DATE.equals(orderFieldType[col])) {
                    numberTypeFactorIndex++;
                    if (row.get(numberTypeFactorIndex) instanceof Number) {
                        forecastFactorValues[col] = ConvertUtil.convertObjectToDouble(row.get(numberTypeFactorIndex).toString());
                    }
                }
            }
        }

        // forecast text & date value factors.(max)
        for (Map.Entry<Integer, Map<String, Double>> entry : textValueMap.entrySet()) {
            int colIndex = entry.getKey();
            Map<String, Double> valueMap = entry.getValue();
            double max = 0f;
            for (Map.Entry<String, Double> valueEntry : valueMap.entrySet()) {
                double valueDouble = valueEntry.getValue();
                if (max < valueDouble) {
                    max = valueDouble;
                }
            }
            String textFactorName = textAndDateFactors.get(colIndex);
            int textFactorIndex = ArrayUtils.indexOf(objectiveAndFactor, textFactorName);
            forecastFactorValues[textFactorIndex] = max;

        }
        JsonObject forecast = new JsonObject();
        //skip objective index 0
        for (int col = 1; col < objectiveAndFactor.length; col++) {
            forecast.addProperty(objectiveAndFactor[col], ConvertUtil.convertObjectToDecimal(forecastFactorValues[col]));
        }
        return forecast;
    }


    /**
     * @param trainingDataSet training data
     * @return average value of each factor and objective
     */
    private Dataset<Row> avgNumberedData(Dataset<Row> trainingDataSet) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int col = 0; col < objectiveAndFactor.length; col++) {
            if (!FactorDataType.TEXT.equals(orderFieldType[col]) && !FactorDataType.DATE.equals(orderFieldType[col])) {
                map.put(objectiveAndFactor[col], "avg");
            }
        }

        Dataset<Row> avgData = trainingDataSet.agg(map);
        avgData.show();
        return avgData;
    }


    /**
     * @param factors positive, negative factor data
     * @param value   positive: 1. Negative: -1
     * @return array contain 0, (1 or -1)
     */

    private int[] generateArrayFollowByFactorOrder(List<String> factors, int value) {
        int[] newOrder = new int[objectiveAndFactor.length];
        if (factors != null) {
            for (String factor : factors) {
                int index = ArrayUtils.indexOf(objectiveAndFactor, factor);
                newOrder[index] = value;
            }
        }

        return newOrder;
    }

    /**
     * @param fieldTypeObj factors type data
     * @return factors type array has order same as  objectiveAndFactor[]
     */
    private String[] generateOrderFieldType(JsonObject fieldTypeObj) {
        int size = objectiveAndFactor.length;

        String[] fieldByOrder = new String[size];
        for (Map.Entry<String, JsonElement> entry : fieldTypeObj.entrySet()) {
            String key = entry.getValue().getAsString();
            String value = entry.getKey();
            int newIndex = ArrayUtils.indexOf(objectiveAndFactor, value);
            if (newIndex >= 0) {
                fieldByOrder[newIndex] = key;
            }
        }
        return fieldByOrder;
    }


    /**
     * @param map data after convert text to number
     * @return category weight data
     */
    private JsonObject generateCategoryFieldWeight(Map<Integer, Map<String, Double>> map) {
        JsonObject category = new JsonObject();

        for (Map.Entry<Integer, Map<String, Double>> entry : map.entrySet()) {
            JsonObject colJsonObject = new JsonObject();

            int key = entry.getKey();
            Map<String, Double> map2 = entry.getValue();

            for (Map.Entry<String, Double> attributeEntry : map2.entrySet()) {
                String attKey = attributeEntry.getKey();
                double attValue = attributeEntry.getValue();
                colJsonObject.addProperty(attKey, ConvertUtil.convertObjectToDecimal(attValue));
            }
            category.add(textAndDateFactors.get(key), colJsonObject);
        }
        return category;
    }

    /**
     * @param allFactor all factors
     * @return list of number type factors
     */
    private List<String> generateNumberTypeFactors(String[] allFactor) {
        List<String> list = new ArrayList<>();
        for (int col = 0; col < allFactor.length; col++) {
            if (!FactorDataType.TEXT.equals(orderFieldType[col]) && !FactorDataType.DATE.equals(orderFieldType[col])) {
                list.add(allFactor[col]);
            }
        }
        return list;
    }

    /**
     * @param allFactor all factors
     * @return list of text and date type factors
     */
    private List<String> generateTextFactors(String[] allFactor) {
        List<String> list = new ArrayList<>();
        for (int col = 0; col < allFactor.length; col++) {
            if (FactorDataType.TEXT.equals(orderFieldType[col]) || FactorDataType.DATE.equals(orderFieldType[col])) {
                list.add(allFactor[col]);
            }
        }
        return list;
    }
}
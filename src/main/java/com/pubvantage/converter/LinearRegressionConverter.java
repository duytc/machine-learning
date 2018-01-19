package com.pubvantage.converter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubvantage.AppMain;
import com.pubvantage.entity.DataType;
import com.pubvantage.entity.DoubleAccumulator;
import com.pubvantage.utils.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.*;

public class LinearRegressionConverter implements DataConverterInterface {
    private final int objectiveIndex = 0;
    private SparkDataUtil sparkDataUtil = new SparkDataTrainingUtilImpl();
    private Dataset<Row> trainingDataSet;
    private JsonObject fieldType;
    private String[] orderFieldType;
    private List<String> positiveFactors;
    private List<String> negativeFactors;
    private int[] positiveOrder;
    private int[] negativeOrder;
    private AppResource appResource;
    private Properties properties;

    private String objectiveAndFactor[];

    private List<String> numberTypeFactors;
    private Map<Integer, Map<String, Double>> textValueMap;
    private List<String> textAndDateFactors;
    static Logger logger = Logger.getLogger(AppMain.class.getName());

    public LinearRegressionConverter() {
        appResource = new AppResource();
        properties = appResource.getPropValues();
    }

    /**
     * convert data to learn
     *
     * @param autoOptimizationId input autoOptimizationId
     * @param identifier         input identifier
     */
    public boolean doConvert(long autoOptimizationId, String identifier) {
        //[objective, factor1, factor2,...]
        objectiveAndFactor = sparkDataUtil.getListFactor(autoOptimizationId);
        FilePathHelper.createDataConvertedFolder(properties.getProperty("path.header"));
        CSVHelper.writeNoHeader(properties.getProperty("path.header"), objectiveAndFactor, false);

        //prepare field type data
        fieldType = sparkDataUtil.getFieldType(autoOptimizationId);
        orderFieldType = generateOrderFieldType(fieldType);

        numberTypeFactors = generateNumberTypeFactors(objectiveAndFactor);
        textAndDateFactors = generateTextFactors(objectiveAndFactor);

        // get data from DB
        trainingDataSet = sparkDataUtil.getDataSetByTable(new StringBuilder("__data_training_")
                .append(autoOptimizationId).toString(), identifier, objectiveAndFactor);

        if (trainingDataSet == null || trainingDataSet.count() == 0) {
            return false;
        }

        //prepare positive, negative, objective
        positiveFactors = sparkDataUtil.getPositiveNegativeField(autoOptimizationId, "positive_factors");
        negativeFactors = sparkDataUtil.getPositiveNegativeField(autoOptimizationId, "negative_factors");

        positiveFactors = handleBothPositiveAndNegativeFactorIsEmpty(positiveFactors, negativeFactors, objectiveAndFactor);

        positiveOrder = new int[objectiveAndFactor.length];
        positiveOrder = updateOrder(positiveFactors, 1);
        negativeOrder = new int[objectiveAndFactor.length];
        negativeOrder = updateOrder(negativeFactors, -1);

        //each factor: group by -> sum
        List<Dataset<Row>> sumNumberedDataByTextGroups = sumNumberedDataByTextGroup(trainingDataSet);
        textValueMap = computeTextValue(sumNumberedDataByTextGroups);

        //create folder to save
        String fileName = properties.getProperty("data.convert");
        if (fileName == null || fileName.isEmpty()) {
            fileName = "converted_data";
        }
        String filePath = createDataConvertedFilePath(getPathToSaveFile(autoOptimizationId, identifier, fileName, "txt"));

        saveConvertedDataToFile(filePath, trainingDataSet, textValueMap);
        //category
        handleCategory(textValueMap, autoOptimizationId, identifier);
        //forecast
        handleForecast(textValueMap, autoOptimizationId, identifier);

        return true;
    }

    private List<String> handleBothPositiveAndNegativeFactorIsEmpty(List<String> positiveFactors, List<String> negativeFactors, String[] objectiveAndFactor) {
        if ((positiveFactors == null || positiveFactors.isEmpty()) && (negativeFactors == null || negativeFactors.isEmpty())) {
            positiveFactors = new ArrayList<>();
            for (int i = 1; i < objectiveAndFactor.length; i++) {
                if (DataType.NUMBER.equals(orderFieldType[i]) || DataType.DECIMAL.equals(orderFieldType[i]))
                    positiveFactors.add(objectiveAndFactor[i]);
            }
        }
        return positiveFactors;
    }

    /**
     * @param filePath        path need to save file
     * @param trainingDataSet training data
     * @param textValueMap    data after converted text & data to number
     */

    private void saveConvertedDataToFile(String filePath, Dataset<Row> trainingDataSet, Map<Integer, Map<String, Double>> textValueMap) {
        //need to create local variable to access in trainingDataSet.foreachPartition()
        String[] objectiveAndFactor = this.objectiveAndFactor;
        final int objIndex = this.objectiveIndex;
        String[] orderFieldType = this.orderFieldType;

        trainingDataSet.foreachPartition(iterator -> {
            while (iterator.hasNext()) {
                Row rowData = iterator.next();
                String[] rowDataToWrite = new String[objectiveAndFactor.length];
                rowDataToWrite[0] = String.valueOf(0);
                if (rowData.get(objIndex) instanceof Number) {
                    rowDataToWrite[0] = String.valueOf(ConvertUtil.convertObjectToDecimal(Double.parseDouble(rowData.get(objIndex).toString())));
                }

                int colIndex = 1;
                int textFactorIndex = -1;
                for (int col = 0; col < objectiveAndFactor.length; col++) {

                    if (DataType.TEXT.equals(orderFieldType[col]) || DataType.DATE.equals(orderFieldType[col])) {
                        textFactorIndex++;
                        String data = rowData.get(col).toString();
                        double effort = textValueMap.get(textFactorIndex).get(data);
                        rowDataToWrite[colIndex++] = String.valueOf(effort);
                    } else {
                        if (col != objIndex) {
                            String value = "";
                            if (rowData.get(col) instanceof Number) {
                                value = String.valueOf(ConvertUtil.convertObjectToDecimal(Double.parseDouble(rowData.get(col).toString())));
                            }
                            rowDataToWrite[colIndex++] = value;
                        }
                    }
                }

                TextFileHelper.write(filePath, true, rowDataToWrite);
            }

        });
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

            for (int rowIndex = 0; rowIndex < rowList.size(); rowIndex++) {
                Row row = rowList.get(rowIndex);
                double positiveSubtractNegativeSum = 0;
                double objectiveSum = ConvertUtil.convertObjectToDouble(row.get(objectiveIndex + 1));
                double result = 0;

                for (int colIndex = 1; colIndex < row.size(); colIndex++) {
                    String name = getNumberTypeColumnName(colIndex - 1);
                    int orderIndex = getOrderIndexByName(name);

                    if (positiveOrder[orderIndex] == 1) {
                        positiveSubtractNegativeSum += ConvertUtil.convertObjectToDouble(row.get(colIndex));
                    } else if (negativeOrder[orderIndex] == -1) {
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
            if (DataType.TEXT.equals(orderFieldType[i]) || DataType.DATE.equals(orderFieldType[i])) {

                Map<String, String> map = new LinkedHashMap<>();
                for (int col = 0; col < objectiveAndFactor.length; col++) {
                    if (!DataType.TEXT.equals(orderFieldType[col]) && !DataType.DATE.equals(orderFieldType[col])) {
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
     * save forecast data to file
     *
     * @param textValueMap       data after convert text to number
     * @param autoOptimizationId auto optimization id
     * @param identifier         identifier
     */
    private void handleForecast(Map<Integer, Map<String, Double>> textValueMap, long autoOptimizationId, String identifier) {
        JsonObject jsonForecast = generateForecastFactorValuesLocal(textValueMap, trainingDataSet);

        String fileName = properties.getProperty("factor.forecast.value");
        if (fileName == null || fileName.isEmpty()) {
            fileName = "forecast_factor_values";
        }
        JsonFileHelper.writeToFile(getPathToSaveFile(autoOptimizationId, identifier, fileName, "json"), jsonForecast.toString());
    }

    /**
     *
     * @param textValueMap data after convert text to number
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
        for (int col1 = 0; col1 < data.size(); col1++) {
            Row row = data.get(col1);
            int numberTypeFactorIndex = -1;
            for (int col = 0; col < objectiveAndFactor.length; col++) {
                if (!DataType.TEXT.equals(orderFieldType[col]) && !DataType.DATE.equals(orderFieldType[col])) {
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
            if (!DataType.TEXT.equals(orderFieldType[col]) && !DataType.DATE.equals(orderFieldType[col])) {
                map.put(objectiveAndFactor[col], "avg");
            }
        }

        Dataset<Row> avgData = trainingDataSet.agg(map);
        avgData.show();
        return avgData;
    }


    /**
     * save category weight to file
     *
     * @param textValueMap       data after convert text to number
     * @param autoOptimizationId auto optimization id
     * @param identifier         identifier
     */
    private void handleCategory(Map<Integer, Map<String, Double>> textValueMap, long autoOptimizationId, String identifier) {
        JsonObject jsonCategory = generateCategoryFieldWeight(textValueMap);
        String fileName = properties.getProperty("field.category.weight");
        if (fileName == null || fileName.isEmpty()) {
            fileName = "categorical_field_weight";
        }
        JsonFileHelper.writeToFile(getPathToSaveFile(autoOptimizationId, identifier, fileName, "json"), jsonCategory.toString());
    }

    /**
     * @param filePath directory need to create
     * @return created path
     */
    private String createDataConvertedFilePath(String filePath) {
        return FilePathHelper.createDataConvertedFolder(filePath);
    }

    /**
     * @param autoOptimizationId auto optimization id
     * @param identifier         identifier
     * @param fileName           name of file
     * @param fileType           extension of file
     * @return path to save file
     */
    private String getPathToSaveFile(long autoOptimizationId, String identifier, String fileName, String fileType) {
        return FilePathHelper.getPathToSaveConvertData(autoOptimizationId, identifier, fileName, fileType);
    }

    /**
     * @param factors positive, negative factor data
     * @param value   positive: 1. Negative: -1
     * @return array contain 0, 1, -1.
     */

    private int[] updateOrder(List<String> factors, int value) {
        int[] newOrder = new int[objectiveAndFactor.length];
        if (factors != null) {
            for (int i = 0; i < factors.size(); i++) {
                int index = ArrayUtils.indexOf(objectiveAndFactor, factors.get(i));
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
            String value = entry.getKey().toString();
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
            if (!DataType.TEXT.equals(orderFieldType[col]) && !DataType.DATE.equals(orderFieldType[col])) {
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
            if (DataType.TEXT.equals(orderFieldType[col]) || DataType.DATE.equals(orderFieldType[col])) {
                list.add(allFactor[col]);
            }
        }
        return list;
    }


}

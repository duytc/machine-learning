package com.pubvantage;

import com.google.gson.JsonObject;
import com.pubvantage.converter.DataConverterInterface;
import com.pubvantage.converter.LinearRegressionConverter;
import com.pubvantage.entity.CoreLearningModel;
import com.pubvantage.learner.LearnerInterface;
import com.pubvantage.learner.LinearRegressionLearner;
import com.pubvantage.service.DataTrainingService;
import com.pubvantage.service.DataTrainingServiceImpl;
import com.pubvantage.utils.*;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.sql.SparkSession;

import java.util.*;
import java.util.stream.IntStream;

public class AppMain {
    //    private static final String MASTER = "spark://dtag-litpu:7077";
    public static final String LINEAR_REGRESSION_TYPE = "LinearRegression";
    public static SparkSession spark;
    public static JavaSparkContext sparkContext;
    static Logger logger = Logger.getLogger(AppMain.class.getName());
    private static DataTrainingService trainingService = new DataTrainingServiceImpl();
    private static AppResource appResource;
    private static Properties properties;
    private static SparkDataUtil dataUtil = new SparkDataTrainingUtilImpl();

    static {
        appResource = new AppResource();
        properties = appResource.getPropValues();
    }

    public static void main(String[] args) {
//        args = createTestData();
        printArgumentValues(args);

        createSparkContext();

        Map<Long, String[]> items = getAutoOptimizationConfigAndIdentifiers(args);
        logger.info("Generate model and save to data base");
        items.forEach(AppMain::generateAndSaveModel);

        stopSparkContext();
    }

    private static void stopSparkContext() {
        HibernateUtil.shutdown();
        spark.stop();
        sparkContext.stop();
    }

    private static void createSparkContext() {

        String appName = properties.getProperty("spark.app.name");
        String master = properties.getProperty("spark.master");

        SparkConf sparkConf = new SparkConf()
                .setAppName(appName)
                .setMaster(master);
        sparkContext = new JavaSparkContext(sparkConf);
        spark = SparkSession
                .builder()
                .appName(appName)
                .getOrCreate();

        HibernateUtil.startSession();
    }

    /**
     * @param args input parameters form command line
     * @return auto optimization config id and identifiers data
     */
    private static Map<Long, String[]> getAutoOptimizationConfigAndIdentifiers(String[] args) {
        Map<Long, String[]> items = new LinkedHashMap<>();

        String autoOptimizationIdKeyParameter = properties.getProperty("command.parameter.auto.optimization.id");
        String identifierKeyParameter = properties.getProperty("command.parameter.identifier");
        String allKey = properties.getProperty("command.parameter.value.all");

        int autoOptimizationIdParameterIndex = getIndexOfStringInArray(autoOptimizationIdKeyParameter, args);

        if (autoOptimizationIdParameterIndex < 0) {
            int identifierParameterIndex = getIndexOfStringInArray(identifierKeyParameter, args);


            if (identifierParameterIndex >= 0) {
                logger.error("WRONG PARAMETER: Auto optimization id is required");
                return items;
            }
            //get all config id and identifiers
            String[] autoOptimizationIdParameterArray = filterListAutoOptimizationConfigId(new String[0], true);

            for (String autoOptimizationConfigId : autoOptimizationIdParameterArray) {
                String[] identifiers = filterIdentifier(autoOptimizationConfigId, new String[0], true);
                items.put(Long.parseLong(autoOptimizationConfigId), identifiers);
            }
        } else if (args.length > autoOptimizationIdParameterIndex + 2) {
            String autoOptimizationIdParameterString = args[autoOptimizationIdParameterIndex + 2];
            String[] autoOptimizationIdParameterArray = autoOptimizationIdParameterString.split(",");
            boolean runAllAutoOptimizationConfigId = getIndexOfStringInArray(allKey, autoOptimizationIdParameterArray) >= 0;

            autoOptimizationIdParameterArray = filterListAutoOptimizationConfigId(autoOptimizationIdParameterArray, runAllAutoOptimizationConfigId);

            //filter identifiers for each config id
            for (String autoOptimizationConfigId : autoOptimizationIdParameterArray) {
                String[] identifierParameterArray = new String[0];
                boolean runAllIdentifier = false;
                int identifierParameterIndex = getIndexOfStringInArray(identifierKeyParameter, args);

                if (identifierParameterIndex < 0) {
                    runAllIdentifier = true;
                } else if (args.length > identifierParameterIndex + 2) {
                    String identifierParameterString = args[identifierParameterIndex + 2];
                    identifierParameterArray = identifierParameterString.split(",");
                    runAllIdentifier = getIndexOfStringInArray(allKey, identifierParameterArray) >= 0;
                }

                String[] identifiers = filterIdentifier(autoOptimizationConfigId, identifierParameterArray, runAllIdentifier);

                items.put(Long.parseLong(autoOptimizationConfigId), identifiers);
            }

        }
        return items;
    }

    private static String[] filterIdentifier(String autoOptimizationConfigId, String[] identifierParameterArray, boolean runAllIdentifier) {
        return dataUtil.filterIdentifier(autoOptimizationConfigId, identifierParameterArray, runAllIdentifier);

    }

    private static int getIndexOfStringInArray(String autoOptimizationIdKeyParameter, String[] parameterData) {
        if (autoOptimizationIdKeyParameter == null || autoOptimizationIdKeyParameter.isEmpty()) {
            return -1;
        }
        int size = parameterData.length;
        for (int index = 0; index < size; index++) {
            if (autoOptimizationIdKeyParameter.toLowerCase().equals(parameterData[index].toLowerCase())) {
                return index;
            }
        }
        return -1;
    }


    /**
     * get All auto optimization id
     *
     * @return
     */
    private static String[] filterListAutoOptimizationConfigId(String[] autoOptimizationIdParameterArray, boolean runAllAutoOptimizationConfigId) {
        return dataUtil.filterListAutoOptimizationConfigId(autoOptimizationIdParameterArray, runAllAutoOptimizationConfigId);
    }

    /**
     * @param autoOptimizationId auto optimization id
     * @param identifiers        identifiers
     */
    private static void generateAndSaveModel(long autoOptimizationId, String[] identifiers) {
        List<CoreLearningModel> modelList = new ArrayList<>();
        //converter
        DataConverterInterface converter = new LinearRegressionConverter();
        //leaner
        IntStream.range(0, identifiers.length).forEach((int i) -> {
            boolean didConvert = converter.doConvert(autoOptimizationId, identifiers[i]);
            if (didConvert) {
                String identifier = identifiers[i];
                LearnerInterface leaner = new LinearRegressionLearner(autoOptimizationId, identifier, spark);
                modelList.add(generateModel(leaner));
            }
        });
        saveModelToDatabase(modelList);

    }

    /**
     * Generate model from learned data
     *
     * @param learner learned data
     */
    private static CoreLearningModel generateModel(LearnerInterface learner) {
        CoreLearningModel model = new CoreLearningModel();
        model.setId(0l);
        model.setAutoOptimizationConfigId(learner.getAutoOptimizationConfigId());
        model.setType(LINEAR_REGRESSION_TYPE);
        model.setUpdatedDate(new Date());
        model.setCategoricalFieldWeights(getCategory(learner.getAutoOptimizationConfigId(), learner.getIdentifier()));
        model.setForecastFactorValues(getForeCast(learner.getAutoOptimizationConfigId(), learner.getIdentifier()));
        model.setModel(getModelStringData(learner));
        model.setIdentifier(learner.getIdentifier());

        return model;
    }

    /**
     * save list of model to database
     *
     * @param modelList list of model
     */
    private static void saveModelToDatabase(List<CoreLearningModel> modelList) {
        trainingService.saveListModel(modelList);
    }

    /**
     * @return forecast data from file
     */
    private static String getForeCast(long autoOptimizationId, String identifier) {
        JsonObject jsonObject = JsonFileHelper.getJsonFromFile(FilePathHelper.getConvertedDataPath(autoOptimizationId, identifier),
                properties.getProperty("factor.forecast.value"));
        return jsonObject.toString();
    }

    /**
     * @return category weight from file
     */
    private static String getCategory(long autoOptimizationId, String identifier) {
        JsonObject jsonObject = JsonFileHelper.getJsonFromFile(FilePathHelper.getConvertedDataPath(autoOptimizationId, identifier),
                properties.getProperty("field.category.weight"));
        return jsonObject.toString();
    }

    /**
     * @param learner learned data
     * @return
     */
    private static String getModelStringData(LearnerInterface learner) {
        String[] objectiveAndFactor = CSVHelper.read(properties.getProperty("path.header"));

        JsonObject jsonObject = new JsonObject();
        LinearRegressionModel model = learner.getLrModel();

        //coefficient
        Vector vec = model.coefficients();
        double[] coefficientsArray = vec.toArray();

        JsonObject coefficient = new JsonObject();

        for (int i = 0; i < coefficientsArray.length; i++) {
            int factorIndex = i + 1;// index 0 is objective
            if (Double.isNaN(coefficientsArray[i])) {
                coefficient.addProperty(objectiveAndFactor[factorIndex], "null");
            } else {
                coefficient.addProperty(objectiveAndFactor[factorIndex], coefficientsArray[i]);
            }
        }

        jsonObject.add("coefficient", coefficient);
        jsonObject.addProperty("intercept", model.intercept());
        return jsonObject.toString();
    }

    /**
     * Create test data sample
     *
     * @return
     */
    private static String[] createTestData() {
        String[] args;
        args = new String[6];
        int index = 0;
        args[index++] = "--autoOptimizationId";
        args[index++] = "=";
        args[index++] = "1";
        args[index++] = "--identifier";
        args[index++] = "=";
//        args[index++] = "all";
        args[index++] = "allenwestrepublic.com";
        return args;
    }

    /**
     * Print values of parameter
     *
     * @param args
     */
    private static void printArgumentValues(String[] args) {
        logger.info("Argument values:");
        for (int i = 0; i < args.length; i++) {
            logger.info(" " + args[i]);
        }
    }
}
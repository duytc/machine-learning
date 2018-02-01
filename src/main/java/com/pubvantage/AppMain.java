package com.pubvantage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pubvantage.Authentication.Authentication;
import com.pubvantage.RestParams.LearnerResponse;
import com.pubvantage.RestParams.LearningProcessParams;
import com.pubvantage.RestParams.PredictionProcessParams;
import com.pubvantage.converter.DataConverterInterface;
import com.pubvantage.converter.LinearRegressionConverter;
import com.pubvantage.entity.ConvertedDataWrapper;
import com.pubvantage.entity.CoreAutoOptimizationConfig;
import com.pubvantage.entity.CoreLearningModel;
import com.pubvantage.learner.LearnerInterface;
import com.pubvantage.learner.LinearRegressionLearner;
import com.pubvantage.service.*;
import com.pubvantage.service.Learner.LinearRegressionScoring;
import com.pubvantage.utils.*;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.sql.SparkSession;
import spark.Request;
import spark.Response;

import java.util.*;
import java.util.stream.IntStream;

import static spark.Spark.*;

public class AppMain {

    private static final String LINEAR_REGRESSION_TYPE = "LinearRegression";
    private static final int MAX_THREADS = 8;
    private static final int MIN_THREADS = 2;
    private static final int TIME_OUT_MILLIS = 30000;
    public static SparkSession sparkSession;
    private static JavaSparkContext sparkContext;
    private static String sparkMaster;
    private static String SPARK_MASTER_DEFAULT = "local[*]";
    private static Logger logger = Logger.getLogger(AppMain.class.getName());
    private static DataTrainingServiceInterface dataTrainingService;
    private static CoreLearningModelServiceInterface coreLearnerService;
    private static Properties defaultConfig;
    private static Properties userConfig;
    private static int DEFAULT_PORT = 8086;
    private static int PORT;

    static {
        AppResource appResource = new AppResource();
        defaultConfig = appResource.getPropValues();
        userConfig = appResource.getUserConfiguration();
    }

    public static void main(String[] args) {
        getUserConfiguration();

        sparkMaster = extractCommandLineParameter(args);
        if (!createSparkContext(sparkMaster)) {
            logger.info("------------------------");
            logger.info("sparkMaster: " + sparkMaster);
            logger.error("Please check your configuration");
            logger.info("------------------------");
            return;
        }

        logger.info("------------------------");
        logger.info("sparkMaster: " + sparkMaster);
        logger.info("Waiting for requests ...");
        logger.info("------------------------");

        //REST config
        port(PORT);
        threadPool(MAX_THREADS, MIN_THREADS, TIME_OUT_MILLIS);
        learningProcessAction();
        predictScoreAction();

    }

    /**
     * listen and process learning request
     */
    private static void learningProcessAction() {
        post("api/learner", AppMain::activeLearningProcess);
    }

    /**
     * listen and process score request
     */
    private static void predictScoreAction() {
        post("api/scores", AppMain::predictScores);
    }

    /**
     * @param args parameters from command line
     * @return spark master
     */
    private static String extractCommandLineParameter(String[] args) {
        logger.info("--------------------------------------------");
        if (args == null || args.length == 0) {
            logger.info("Use default spark master: " + SPARK_MASTER_DEFAULT);
            return SPARK_MASTER_DEFAULT;
        }
        logger.info("Use spark master: " + args[0]);
        return args[0];
    }

    /**
     * get user configuration
     */
    private static void getUserConfiguration() {
        PORT = getPortConfig();
        sparkMaster = getSparkMasterConfig();
    }

    /**
     * @return spark master depend on configuration
     */
    private static String getSparkMasterConfig() {
        String master = defaultConfig.getProperty("spark.master");
        if (master == null || master.isEmpty()) {
            master = SPARK_MASTER_DEFAULT;
        }
        return master;
    }

    /**
     * @return user desire port to run rest api
     */
    private static int getPortConfig() {
        int port;

        String portNumberString = userConfig.getProperty("api.port");
        try {
            port = Integer.parseInt(portNumberString);
        } catch (Exception e) {
            logger.warn("port " + portNumberString + "from user config is not valid");
            portNumberString = defaultConfig.getProperty("api.port");
            try {
                port = Integer.parseInt(portNumberString);
            } catch (Exception ex) {
                port = DEFAULT_PORT;
                logger.info("Use default port " + port);
            }
        }
        return port;
    }

    /**
     * process learning request
     *
     * @param request  request
     * @param response response
     * @return json data contain result of process
     */
    private static String activeLearningProcess(Request request, Response response) {
        response.type("application/json");

        //extract data from request
        LearningProcessParams learningProcessParams = new LearningProcessParams(request);
        boolean isValidParams = learningProcessParams.validateParams();
        if (!isValidParams) {
            LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_BAD_REQUEST, "Parameter is invalid", null);
            response.status(HttpStatus.SC_BAD_REQUEST);
            return new Gson().toJson(learnerResponse);
        }

        //verify token
        Authentication authentication = new Authentication(learningProcessParams.getAutoOptimizationConfigId(), learningProcessParams.getToken());
        boolean isPassAuthentication = authentication.authenticate();
        if (!isPassAuthentication) {
            response.status(HttpStatus.SC_UNAUTHORIZED);
            LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_UNAUTHORIZED, "Fail authentication", null);
            return new Gson().toJson(learnerResponse);
        }

        //pass verify token
        //get data then convert and learn
        Long autoOptimizationConfigId = learningProcessParams.getAutoOptimizationConfigId();
        String[] identifiers = getIdentifiers(autoOptimizationConfigId);
        JsonArray dataResponseArray = new JsonArray();
       List<String> successIdentifiers =  generateAndSaveModel(autoOptimizationConfigId, identifiers);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("autoOptimizationConfigId", autoOptimizationConfigId);
        jsonObject.add("identifiers", JsonUtil.toJsonArray(successIdentifiers.toArray(new String[0])));
        dataResponseArray.add(jsonObject);
        //return response
        LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_OK, "Learn successfully", dataResponseArray);
        response.status(HttpStatus.SC_OK);

        return new Gson().toJson(learnerResponse);
    }

    /**
     * Predict the scores for multiple conditions
     *
     * @param request  rest request
     * @param response response
     * @return json array that are score for multiple condition
     */
    private static String predictScores(Request request, Response response) {
        response.type("application/json");
        PredictionProcessParams predictionProcessParams = new PredictionProcessParams(request);
        boolean isValidParams = predictionProcessParams.validates();
        if (!isValidParams) {
            LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_BAD_REQUEST, "Parameter is invalid", null);
            response.status(HttpStatus.SC_BAD_REQUEST);
            return new Gson().toJson(learnerResponse);
        }

        Long autoOptimizationConfigId = predictionProcessParams.getAutoOptimizationConfigId();
        List<String> identifiers = predictionProcessParams.getIdentifiers();
        JsonArray conditions = predictionProcessParams.getConditions();
        String token = predictionProcessParams.getToken();

        Authentication authentication = new Authentication(autoOptimizationConfigId, token);
        boolean isValid = authentication.authenticate();
        if (!isValid) {
            LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_UNAUTHORIZED, "The request is unauthenticated", null);
            response.status(HttpStatus.SC_UNAUTHORIZED);
            return new Gson().toJson(learnerResponse);
        }

        CoreAutoOptimizationConfigService coreAutoOptimizationConfigService = new CoreAutoOptimizationConfigService();
        CoreAutoOptimizationConfig coreAutoOptimizationConfig = coreAutoOptimizationConfigService.findById(autoOptimizationConfigId);

        LinearRegressionScoring linearRegressionScoring = new LinearRegressionScoring(coreAutoOptimizationConfig, identifiers, conditions);
        Map<String, Map<String, Double>> predictions = linearRegressionScoring.predict();

        return new Gson().toJson(predictions);
    }

    /**
     * stop spark, hibernate
     */
    private static void stopSparkContext() {
        HibernateUtil.shutdown();
        sparkSession.stop();
        sparkContext.stop();
    }

    /**
     * create spark context
     *
     * @param master master url
     * @return status of creating spark context
     */
    private static boolean createSparkContext(String master) {
        String appName = defaultConfig.getProperty("spark.app.name");
        SparkConf sparkConf = new SparkConf()
                .setAppName(appName)
                .setMaster(master);
        try {
            sparkContext = new JavaSparkContext(sparkConf);
            sparkSession = SparkSession
                    .builder()
                    .appName(appName)
                    .getOrCreate();

            HibernateUtil.startSession();

            dataTrainingService = new DataTrainingService();
            coreLearnerService = new CoreLearningModelService();
            return true;
        } catch (Exception e) {
            logger.error("Error occurs when create spark context: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * get list identifiers
     *
     * @param autoOptimizationConfigId auto optomization config id
     * @return list of identifiers
     */
    private static String[] getIdentifiers(Long autoOptimizationConfigId) {
        return dataTrainingService.getIdentifiers(autoOptimizationConfigId);

    }

    /**
     * @param autoOptimizationId auto optimization id
     * @param identifiers        identifiers
     */
    private static List<String> generateAndSaveModel(long autoOptimizationId, String[] identifiers) {

        List<String> successIdentifiers = new ArrayList<>();
        List<CoreLearningModel> modelList = new ArrayList<>();
        //converter
        DataConverterInterface converter = new LinearRegressionConverter();
        //leaner
        IntStream.range(0, identifiers.length).forEach((int i) -> {
            ConvertedDataWrapper convertedDataWrapper = converter.doConvert(autoOptimizationId, identifiers[i]);
            if (convertedDataWrapper != null && convertedDataWrapper.getDataSet() != null) {
                String identifier = identifiers[i];
                LearnerInterface leaner = new LinearRegressionLearner(autoOptimizationId, identifier, sparkSession, convertedDataWrapper);
                modelList.add(generateModel(leaner));
                successIdentifiers.add(identifier);
            }
        });
        saveModelToDatabase(modelList);
        return successIdentifiers;
    }

    /**
     * Generate model from learned data
     *
     * @param learner learned data
     */
    private static CoreLearningModel generateModel(LearnerInterface learner) {
        CoreLearningModel model = new CoreLearningModel();
        model.setId(0L);
        model.setAutoOptimizationConfigId(learner.getAutoOptimizationConfigId());
        model.setType(LINEAR_REGRESSION_TYPE);
        model.setUpdatedDate(new Date());
        model.setCategoricalFieldWeights(learner.getConvertedDataWrapper().getCategoryWeight().toString());
        model.setForecastFactorValues(learner.getConvertedDataWrapper().getForecast().toString());
        model.setModel(getModelStringData(learner));
        model.setIdentifier(learner.getIdentifier());
        model.setModePath(FilePathUtil.getLearnerModelPath(learner.getAutoOptimizationConfigId(), learner.getIdentifier()));

        return model;
    }

    /**
     * save list of model to database
     *
     * @param modelList list of model
     */
    private static void saveModelToDatabase(List<CoreLearningModel> modelList) {
        coreLearnerService.saveListModel(modelList);
    }

    /**
     * @param learner learned data
     * @return json data of model
     */
    private static String getModelStringData(LearnerInterface learner) {
        List<String> objectiveAndFactor = learner.getConvertedDataWrapper().getObjectiveAndFactors();

        JsonObject jsonObject = new JsonObject();
        LinearRegressionModel model = learner.getLrModel();

        //coefficient
        Vector vec = model.coefficients();
        double[] coefficientsArray = vec.toArray();

        JsonObject coefficient = new JsonObject();

        for (int i = 0; i < coefficientsArray.length; i++) {
            int factorIndex = i + 1;// index 0 is objective
            if (Double.isNaN(coefficientsArray[i])) {
                coefficient.addProperty(objectiveAndFactor.get(factorIndex), "null");
            } else {
                double value = ConvertUtil.convertObjectToDecimal(coefficientsArray[i]).doubleValue();
                coefficient.addProperty(objectiveAndFactor.get(factorIndex), value);
            }
        }

        jsonObject.add("coefficient", coefficient);

        if (Double.isNaN(model.intercept())) {
            jsonObject.addProperty("intercept", "null");
        } else {
            double value = ConvertUtil.convertObjectToDecimal(model.intercept()).doubleValue();
            jsonObject.addProperty("intercept", value);
        }

        return jsonObject.toString();
    }

    /**
     * Create test data sample
     *
     * @return data test
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

}
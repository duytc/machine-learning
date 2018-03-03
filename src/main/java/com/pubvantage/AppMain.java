package com.pubvantage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pubvantage.Authentication.Authentication;
import com.pubvantage.RestParams.LearnerResponse;
import com.pubvantage.RestParams.LearningProcessParams;
import com.pubvantage.RestParams.PredictionProcessParams;
import com.pubvantage.constant.MessageConstant;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.CoreLearningModel;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.learner.LearnerInterface;
import com.pubvantage.learner.LinearRegressionLearner;
import com.pubvantage.learner.Params.LinearRegressionDataProcess;
import com.pubvantage.learner.Params.SegmentFieldGroup;
import com.pubvantage.service.*;
import com.pubvantage.service.DataTraning.DataTrainingService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
        String jsonParams = request.body();

        LearningProcessParams learningProcessParams = new LearningProcessParams(jsonParams);
        boolean isValidParams = learningProcessParams.validateOptimizationRules();
        if (!isValidParams) {
            LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_BAD_REQUEST, MessageConstant.INVALID_PARAM, null);
            response.status(HttpStatus.SC_BAD_REQUEST);
            return new Gson().toJson(learnerResponse);
        }

        //verify token
        boolean isPassAuthentication = learningProcessParams.validateToken();
        if (!isPassAuthentication) {
            response.status(HttpStatus.SC_UNAUTHORIZED);
            LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_UNAUTHORIZED, MessageConstant.INVALID_PERMISSION, null);
            return new Gson().toJson(learnerResponse);
        }

        //get data then convert and learn
        Long optimizationRuleId = learningProcessParams.getOptimizationRuleId();
        OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();
        CoreOptimizationRule optimizationRule = optimizationRuleService.findById(optimizationRuleId);

        JsonArray dataResponseArray = new JsonArray();
        List<String> successIdentifiers = generateAndSaveModel(optimizationRule);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(MyConstant.OPTIMIZE_RULE_ID, optimizationRuleId);
        jsonObject.add(MyConstant.IDENTIFIER, JsonUtil.toJsonArray(
                successIdentifiers != null ? successIdentifiers.toArray(new String[0]) : new String[0]));
        dataResponseArray.add(jsonObject);
        //return response
        LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_OK, MessageConstant.LEARN_SUCCESS, dataResponseArray);
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
            LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_BAD_REQUEST, MessageConstant.INVALID_PARAM, null);
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
            LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_UNAUTHORIZED, MessageConstant.INVALID_PERMISSION, null);
            response.status(HttpStatus.SC_UNAUTHORIZED);
            return new Gson().toJson(learnerResponse);
        }

        OptimizationRuleService coreOptimizationRuleService = new OptimizationRuleService();
        CoreOptimizationRule optimizationRule = coreOptimizationRuleService.findById(autoOptimizationConfigId);

//        LinearRegressionScoring linearRegressionScoring = new LinearRegressionScoring(optimizationRule, identifiers, conditions);
//        Map<String, Map<String, Double>> predictions = linearRegressionScoring.predict();
        String predictions = "";
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

            dataTrainingService = new DataTrainingServiceOld();
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

    private static List<String> generateAndSaveModel(CoreOptimizationRule optimizationRule) {
        List<String> successIdentifiers = new ArrayList<>();
        List<CoreLearner> modelList = new ArrayList<>();
        OptimizationRuleServiceInterface ruleService = new OptimizationRuleService();
        List<String> identifiers = ruleService.getIdentifiers(optimizationRule);
        List<String> segmentFields = JsonUtil.jsonArrayStringToJavaList(optimizationRule.getSegmentFields());

        if (identifiers.size() == 0) {
            return null;
        }

        List<PredictionParam> predictionParams = new ArrayList<>();

        for (String identifier : identifiers) {
            PredictionParam predictionParam = new PredictionParam(optimizationRule.getId(), identifier, segmentFields);
            predictionParams.add(predictionParam);
        }

        for (PredictionParam predictionParam : predictionParams) {
            modelList.addAll(generateModelForOneIdentifier(predictionParam));
        }

        saveModelToDatabase(modelList);

        return successIdentifiers;
    }

    private static List<CoreLearner> generateModelForOneIdentifier(PredictionParam predictionParam) {
        List<CoreLearner> coreLearnersList = new ArrayList<>();

        Long optimizationRuleId = predictionParam.getAutoOptimizationId();
        String identifier = predictionParam.getIdentifier();
        List<List<String>> segmentFieldGroups = predictionParam.generateMultipleSegmentFieldGroups();

        for (List<String> segmentFieldGroup : segmentFieldGroups) {
            SegmentFieldGroup segmentFieldGroupObject = new SegmentFieldGroup(optimizationRuleId, identifier, segmentFieldGroup);
            List<CoreLearner> coreLearners = generateModelForSegmentFieldGroup(segmentFieldGroupObject);
            coreLearnersList.addAll(coreLearners);
        }

        return coreLearnersList;
    }

    private static List<CoreLearner> generateModelForSegmentFieldGroup(SegmentFieldGroup segmentFieldGroup) {
        List<CoreLearner> coreLearners = new ArrayList<>();

        OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();
        List<OptimizeField> optimizeFields = optimizationRuleService.getOptimizeFields(segmentFieldGroup.getOptimizationRuleId());

        for (OptimizeField optimizeField : optimizeFields) {
            coreLearners.addAll(generateModelForOneOptimizeField(segmentFieldGroup, optimizeField));
        }

        return coreLearners;
    }

    private static List<CoreLearner> generateModelForOneOptimizeField(SegmentFieldGroup segmentFieldGroup, OptimizeField optimizeField) {
        List<CoreLearner> coreLearners = new ArrayList<>();

        Long optimizationRuleId = segmentFieldGroup.getOptimizationRuleId();
        String identifier = segmentFieldGroup.getIdentifier();
        List<String> oneSegmentGroup = segmentFieldGroup.getOneSegmentFieldGroup();

        DataTrainingService dataTrainingService = new DataTrainingService(optimizationRuleId, identifier, oneSegmentGroup);
        List<Map<String, Object>> uniqueValuesOfOneSegmentFieldGroup = dataTrainingService.getAllUniqueValuesForOneSegmentFieldGroup();

        for (Map<String, Object> uniqueValue : uniqueValuesOfOneSegmentFieldGroup) {
            LinearRegressionDataProcess linearRegressionDataProcess = new LinearRegressionDataProcess(optimizationRuleId, identifier, oneSegmentGroup, uniqueValue, optimizeField);
            CoreLearner learners = generateModelForOneValueOfSegmentFieldGroups(linearRegressionDataProcess);
            coreLearners.add(learners);
        }

        return coreLearners;
    }

    private static CoreLearner generateModelForOneValueOfSegmentFieldGroups(LinearRegressionDataProcess linearRegressionDataProcess) {
        LinearRegressionLearner linearRegressionLearner = new LinearRegressionLearner(sparkSession, linearRegressionDataProcess);
        LinearRegressionModel linearRegressionModel = linearRegressionLearner.generateModel(sparkSession);

        CoreLearner coreLearner = new CoreLearner();
        coreLearner.setId(0L);
        coreLearner.setIdentifier(linearRegressionDataProcess.getIdentifier());
        coreLearner.setOptimizationRuleId(linearRegressionDataProcess.getOptimizationRuleId());
        coreLearner.setSegmentValues(JsonUtil.mapToJson(linearRegressionDataProcess.getUniqueValue()));
        coreLearner.setOptimizeFields(JsonUtil.toJson(linearRegressionDataProcess.getOptimizeField()));
        if (linearRegressionModel == null) {
            coreLearner.setModelPath(null);
            coreLearner.setMathModel(null);
            coreLearner.setMetricsPredictiveValues(null);
        } else {
            coreLearner.setModelPath(FilePathUtil.getLearnerModelPath(
                    linearRegressionDataProcess.getOptimizationRuleId(),
                    linearRegressionDataProcess.getIdentifier(),
                    linearRegressionDataProcess.getOneSegmentGroup(),
                    linearRegressionDataProcess.getUniqueValue()));
            coreLearner.setMathModel(getModelStringData(linearRegressionDataProcess, linearRegressionModel));
            coreLearner.setMetricsPredictiveValues(linearRegressionDataProcess.getMetricsPredictiveValues().toString());
        }

        return coreLearner;
    }

    /**
     * @param segmentFields
     * @return
     */
    private static List<List<String>> createSegmentFieldGroups(List<String> segmentFields) {
        return ConvertUtil.generateSubsets(segmentFields);
    }

    /**
     * Generate model from learned data
     *
     * @param learner learned data
     */
    private static CoreLearningModel generateModel(LearnerInterface learner) {
        CoreLearningModel model = new CoreLearningModel();


        return model;
    }

    /**
     * save list of model to database
     *
     * @param modelList list of model
     */
    private static void saveModelToDatabase(List<CoreLearner> modelList) {
        coreLearnerService.saveListLearnerModel(modelList);
    }


    private static String getModelStringData(LinearRegressionDataProcess linearRegressionDataProcess, LinearRegressionModel linearRegressionModel) {
        List<String> objectiveAndFields = linearRegressionDataProcess.getObjectiveAndFields();

        JsonObject jsonObject = new JsonObject();
        LinearRegressionModel model = linearRegressionModel;

        //coefficient
        Vector vec = model.coefficients();
        double[] coefficientsArray = vec.toArray();

        JsonObject coefficient = new JsonObject();

        for (int i = 0; i < coefficientsArray.length; i++) {
            logger.info(coefficientsArray[i]);

            int factorIndex = i + 1;// index 0 is objective
            if (Double.isNaN(coefficientsArray[i])) {
                coefficient.addProperty(objectiveAndFields.get(factorIndex), "null");
            } else {
                double value = ConvertUtil.convertObjectToDecimal(coefficientsArray[i]).doubleValue();
                coefficient.addProperty(objectiveAndFields.get(factorIndex), value);
            }
        }

        jsonObject.add(MyConstant.COEFFICIENT, coefficient);

        if (Double.isNaN(model.intercept())) {
            jsonObject.addProperty(MyConstant.INTERCEPT, "null");
        } else {
            double value = ConvertUtil.convertObjectToDecimal(model.intercept()).doubleValue();
            jsonObject.addProperty(MyConstant.INTERCEPT, value);
        }

        return jsonObject.toString();
    }

}
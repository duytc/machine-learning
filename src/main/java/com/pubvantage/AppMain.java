package com.pubvantage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pubvantage.authentication.Authentication;
import com.pubvantage.constant.MessageConstant;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.dao.OptimizationRuleDao;
import com.pubvantage.entity.CoreLearner;
import com.pubvantage.entity.CoreOptimizationRule;
import com.pubvantage.entity.OptimizeField;
import com.pubvantage.learner.LearnerInterface;
import com.pubvantage.learner.LinearRegressionTrainingDataProcess;
import com.pubvantage.learner.LinearRegressionLearner;
import com.pubvantage.prediction.LinearRegressionScoring;
import com.pubvantage.restparams.LearnerResponse;
import com.pubvantage.restparams.LearningProcessParams;
import com.pubvantage.restparams.PredictionProcessParams;
import com.pubvantage.service.CoreLearningModelService;
import com.pubvantage.service.CoreLearningModelServiceInterface;
import com.pubvantage.service.OptimizationRuleService;
import com.pubvantage.service.OptimizationRuleServiceInterface;
import com.pubvantage.utils.AppResource;
import com.pubvantage.utils.FilePathUtil;
import com.pubvantage.utils.HibernateUtil;
import com.pubvantage.utils.JsonUtil;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
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

    private static final int MAX_THREADS = 15;
    private static final int MIN_THREADS = 2;
    private static final int TIME_OUT_MILLIS = 30000;
    public static SparkSession sparkSession;
    private static JavaSparkContext sparkContext;
    private static String sparkMaster;
    private static String SPARK_MASTER_DEFAULT = "local[*]";
    private static Logger logger = Logger.getLogger(AppMain.class.getName());
    private static CoreLearningModelServiceInterface coreLearnerService;
    private static Properties defaultConfig;
    private static Properties userConfig;
    private static int PORT;
    private static OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();

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
    private static void predictScoreAction() {
        post("api/v2/scores", AppMain::activeScoreProcess);
    }

    /**
     * listen and process learning request
     */
    private static void learningProcessAction() {
        post("api/learner", AppMain::activeLearningProcess);
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
                port = MyConstant.DEFAULT_PORT;
                logger.info("Use default port " + port);
            }
        }
        return port;
    }


    private static String activeScoreProcess(Request request, Response response) {
        response.type("application/json");
        Long optimizationRuleId = null;
        try {
            // handle request parameters
            String predictPrams = request.body();
            PredictionProcessParams predictionProcessParams = new PredictionProcessParams(predictPrams);
            boolean isValidParams = predictionProcessParams.validates();
            if (!isValidParams) {
                LearnerResponse predictResponse = new LearnerResponse(HttpStatus.SC_BAD_REQUEST, MessageConstant.INVALID_PARAM, null);
                response.status(HttpStatus.SC_BAD_REQUEST);
                return new Gson().toJson(predictResponse);
            }

            optimizationRuleId = predictionProcessParams.getOptimizationRuleId();
            optimizationRuleService.setLoadingForOptimizationRule(optimizationRuleId, false);

            // Check token
            String token = predictionProcessParams.getToken();
            Authentication authentication = new Authentication(optimizationRuleId, token);
            boolean isValid = authentication.authenticate();
            if (!isValid) {
                LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_UNAUTHORIZED, MessageConstant.INVALID_PERMISSION, null);
                response.status(HttpStatus.SC_UNAUTHORIZED);
                return new Gson().toJson(learnerResponse);
            }

            // do prediction
            CoreOptimizationRule optimizationRule = optimizationRuleService.findById(optimizationRuleId, new OptimizationRuleDao());
            LinearRegressionScoring regressionScoringV2 = new LinearRegressionScoring(optimizationRule);
            regressionScoringV2.predict();
            optimizationRuleService.setLoadingForOptimizationRule(optimizationRuleId, true);

            logger.info(MessageConstant.SCORING_COMPLETE);
            return new Gson().toJson("{'message': 'Done'}");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            optimizationRuleService.setLoadingForOptimizationRule(optimizationRuleId, true);
        }

        LearnerResponse predictResponse = new LearnerResponse(HttpStatus.SC_NOT_FOUND, MessageConstant.INTERNAL_ERROR, null);
        response.status(HttpStatus.SC_NOT_FOUND);
        return new Gson().toJson(predictResponse);
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
        Long optimizationRuleId = null;
        try {
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
            optimizationRuleId = learningProcessParams.getOptimizationRuleId();
            optimizationRuleService.setLoadingForOptimizationRule(optimizationRuleId, false);
            CoreOptimizationRule optimizationRule = optimizationRuleService.findById(optimizationRuleId, new OptimizationRuleDao());

            String currentTrainingDataChecksum = optimizationRuleService.getCurrentTrainingDataChecksum(optimizationRuleId);

            if (!optimizationRuleService.isChecksumChanged(currentTrainingDataChecksum, optimizationRule.getLastTrainingDataChecksum())) {
                logger.info(MessageConstant.SKIP_LEARNING);
                LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_OK, MessageConstant.SKIP_LEARNING, null);
                return new Gson().toJson(learnerResponse);
            }

            boolean updateChecksumOk = saveNewTrainingDataChecksum(optimizationRule, currentTrainingDataChecksum);

            if(!updateChecksumOk){
                logger.error(MessageConstant.UPDATE_CHECKSUM_ERROR);
                LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, MessageConstant.UPDATE_CHECKSUM_ERROR, null);
                return new Gson().toJson(learnerResponse);
            }
            List<String> successIdentifiers = generateAndSaveModel(optimizationRule);

            //return response
            JsonArray dataResponseArray = buildLearnerResponse(optimizationRuleId, successIdentifiers);
            LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_OK, MessageConstant.LEARN_SUCCESS, dataResponseArray);
            response.status(HttpStatus.SC_OK);
            optimizationRuleService.setLoadingForOptimizationRule(optimizationRuleId, true);

            logger.info(MessageConstant.GENERATE_LEARNER_MODEL_SUCCESS);

            return new Gson().toJson(learnerResponse);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            optimizationRuleService.setLoadingForOptimizationRule(optimizationRuleId, true);
        }

        LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_NOT_FOUND, MessageConstant.INTERNAL_ERROR, null);
        response.status(HttpStatus.SC_NOT_FOUND);
        return new Gson().toJson(learnerResponse);
    }

    private static boolean saveNewTrainingDataChecksum(CoreOptimizationRule optimizationRule, String currentTrainingDataChecksum) {
        optimizationRule.setLastTrainingDataChecksum(currentTrainingDataChecksum);
        return optimizationRuleService.updateChecksum(optimizationRule);
    }

    private static JsonArray buildLearnerResponse(Long optimizationRuleId, List<String> successIdentifiers) {
        JsonArray dataResponseArray = new JsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(MyConstant.OPTIMIZE_RULE_ID, optimizationRuleId);
        jsonObject.add(MyConstant.IDENTIFIER, JsonUtil.toJsonArray(
                successIdentifiers != null ? successIdentifiers.toArray(new String[0]) : new String[0]));
        dataResponseArray.add(jsonObject);

        return dataResponseArray;
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

            coreLearnerService = new CoreLearningModelService();
            return true;
        } catch (Exception e) {
            logger.error(MessageConstant.CREATING_SPARK_CONTEXT_ERROR, e);
            return false;
        }
    }

    private static List<String> generateAndSaveModel(CoreOptimizationRule optimizationRule) {
        logger.info(MessageConstant.START_LEARNING);
        List<String> successIdentifiers = new ArrayList<>();
        List<CoreLearner> modelList = new ArrayList<>();
        //distinct identifier values from data training
        List<String> identifiers = optimizationRuleService.getIdentifiers(optimizationRule);
        if (identifiers.size() == 0) {
            return null;
        }
        List<OptimizeField> optimizeFields = optimizationRuleService.getOptimizeFields(optimizationRule);

        for (String identifier : identifiers) {
            List<CoreLearner> coreLearnerList = generateModelForOneIdentifier(optimizeFields, identifier, optimizationRule);
            modelList.addAll(coreLearnerList);
            successIdentifiers.add(identifier);
        }
        saveModelToDatabase(modelList, optimizationRule);
        return successIdentifiers;
    }

    private static List<CoreLearner> generateModelForOneIdentifier(List<OptimizeField> optimizeFields,
                                                                   String identifier,
                                                                   CoreOptimizationRule optimizationRule) {
        List<CoreLearner> coreLearnersList = new ArrayList<>();
        for (OptimizeField optimizeField : optimizeFields) {
            LinearRegressionTrainingDataProcess linearRegressionDataProcess = new LinearRegressionTrainingDataProcess(optimizationRule, identifier, optimizeField);
            List<CoreLearner> learners = generateOptimizeModel(linearRegressionDataProcess);
            if (learners != null) {
                coreLearnersList.addAll(learners);
            }
        }
        return coreLearnersList;
    }

    private static List<CoreLearner> generateOptimizeModel(LinearRegressionTrainingDataProcess linearRegressionDataProcess) {

        LearnerInterface linearRegressionLearner = new LinearRegressionLearner(linearRegressionDataProcess);
        LinearRegressionModel linearRegressionModel = linearRegressionLearner.generateModel(sparkSession);
        if (linearRegressionModel == null) return null;

        Map<String, Map<String, double[]>> predictiveValues = linearRegressionDataProcess.getPredictiveValues();
        List<CoreLearner> coreLearnerList = new ArrayList<>();
        for (Map.Entry<String, Map<String, double[]>> entry : predictiveValues.entrySet()) {
            String jsonSegmentGroup = entry.getKey();
            jsonSegmentGroup = refactorSegmentGroup(jsonSegmentGroup);
            Map<String, double[]> prediction = entry.getValue();

            CoreLearner coreLearner = new CoreLearner();
            coreLearner.setId(0L);
            coreLearner.setIdentifier(linearRegressionDataProcess.getIdentifier());
            coreLearner.setOptimizationRuleId(linearRegressionDataProcess.getOptimizationRule().getId());
            coreLearner.setOptimizeFields(JsonUtil.toJson(linearRegressionDataProcess.getOptimizeField()));
            coreLearner.setModelPath(FilePathUtil.getLearnerModelPath(
                    linearRegressionDataProcess.getOptimizationRule().getId(),
                    linearRegressionDataProcess.getIdentifier(),
                    linearRegressionDataProcess.getOptimizeField().getField()));
            coreLearner.setMathModel(linearRegressionLearner.getModelStringData(linearRegressionModel));
            coreLearner.setMetricsPredictiveValues(JsonUtil.mapToJson(prediction));
            coreLearner.setSegmentValues(jsonSegmentGroup);

            coreLearnerList.add(coreLearner);
        }

        return coreLearnerList;
    }

    /**
     * @param jsonSegmentGroup segment group in json type. If it is global, the value is {"NO_SEGMENT":"NO_SEGMENT"}
     * @return return {} if jsonSegmentGroup is {"NO_SEGMENT":"NO_SEGMENT"}. Otherwise return jsonSegmentGroup
     */
    private static String refactorSegmentGroup(String jsonSegmentGroup) {
        Map<String, String> map = JsonUtil.jsonToMap(jsonSegmentGroup);
        if (MyConstant.NO_SEGMENT.equals(map.get(MyConstant.NO_SEGMENT))) {
            return "{}";
        }
        return jsonSegmentGroup;
    }

    /**
     * save list of model to database
     *
     * @param modelList        list of model
     * @param optimizationRule optimization rule
     */
    private static void saveModelToDatabase(List<CoreLearner> modelList, CoreOptimizationRule optimizationRule) {
        coreLearnerService.saveListLearnerModel(modelList, optimizationRule);
    }


}
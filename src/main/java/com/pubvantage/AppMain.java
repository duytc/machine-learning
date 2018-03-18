package com.pubvantage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pubvantage.authentication.Authentication;
import com.pubvantage.restparams.*;
import com.pubvantage.constant.MessageConstant;
import com.pubvantage.constant.MyConstant;
import com.pubvantage.dao.SparkDataTrainingDao;
import com.pubvantage.dao.SparkDataTrainingDaoInterface;
import com.pubvantage.entity.*;
import com.pubvantage.learner.LinearRegressionLearner;
import com.pubvantage.learner.Params.LinearRegressionDataProcess;
import com.pubvantage.learner.Params.SegmentFieldGroup;
import com.pubvantage.service.CoreLearningModelService;
import com.pubvantage.service.CoreLearningModelServiceInterface;
import com.pubvantage.service.DataTraning.DataTrainingService;
import com.pubvantage.service.Learner.LinearRegressionScoring;
import com.pubvantage.service.Learner.LinearRegressionScoringV2;
import com.pubvantage.service.OptimizationRuleService;
import com.pubvantage.service.OptimizationRuleServiceInterface;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private static int DEFAULT_PORT = 8086;
    private static int PORT;
    private static SparkDataTrainingDaoInterface sparkDataTrainingDao = new SparkDataTrainingDao();
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
        predictScoreActionV2();
    }

    /**
     * listen and process learning request
     */
    private static void predictScoreActionV2() {
        post("api/v2/scores", AppMain::activeScoreProcessV2);
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
                port = DEFAULT_PORT;
                logger.info("Use default port " + port);
            }
        }
        return port;
    }


    private static String activeScoreProcessV2(Request request, Response response) {
        response.type("application/json");
        try {
            String predictPrams = request.body();
            PredictionProcessParamsV2 predictionProcessParams = new PredictionProcessParamsV2(predictPrams);
            boolean isValidParams = predictionProcessParams.validates();
            if (!isValidParams) {
                LearnerResponse predictResponse = new LearnerResponse(HttpStatus.SC_BAD_REQUEST, MessageConstant.INVALID_PARAM, null);
                response.status(HttpStatus.SC_BAD_REQUEST);
                return new Gson().toJson(predictResponse);
            }
            Long optimizationRuleId = predictionProcessParams.getOptimizationRuleId();
            String token = predictionProcessParams.getToken();
            Authentication authentication = new Authentication(optimizationRuleId, token);
            boolean isValid = authentication.authenticate();
            if (!isValid) {
                LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_UNAUTHORIZED, MessageConstant.INVALID_PERMISSION, null);
                response.status(HttpStatus.SC_UNAUTHORIZED);
                return new Gson().toJson(learnerResponse);
            }
            CoreOptimizationRule optimizationRule = optimizationRuleService.findById(optimizationRuleId);
            List<String> listDate = sparkDataTrainingDao.getDistinctDates(optimizationRuleId, optimizationRule.getDateField());
            LinearRegressionScoringV2 regressionScoringV2 = new LinearRegressionScoringV2(optimizationRule, listDate);
            regressionScoringV2.predict();
            return new Gson().toJson("{'message': 'Done'}");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
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

        try {

            //extract data from request
            String jsonParams = request.body();

            LearningProcessParams learningProcessParams = new LearningProcessParams(jsonParams);
            boolean isValidParams = learningProcessParams.validateOptimizationRules();
            if (!isValidParams) {
                LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_BAD_REQUEST,
                        MessageConstant.INVALID_PARAM, null);
                response.status(HttpStatus.SC_BAD_REQUEST);
                return new Gson().toJson(learnerResponse);
            }

            //verify token
            boolean isPassAuthentication = learningProcessParams.validateToken();
            if (!isPassAuthentication) {
                response.status(HttpStatus.SC_UNAUTHORIZED);
                LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_UNAUTHORIZED,
                        MessageConstant.INVALID_PERMISSION, null);
                return new Gson().toJson(learnerResponse);
            }

            //get data then convert and learn
            Long optimizationRuleId = learningProcessParams.getOptimizationRuleId();
            CoreOptimizationRule optimizationRule = optimizationRuleService.findById(optimizationRuleId);

            JsonArray dataResponseArray = new JsonArray();
            List<String> successIdentifiers = generateAndSaveModel(optimizationRule);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(MyConstant.OPTIMIZE_RULE_ID, optimizationRuleId);
            jsonObject.add(MyConstant.IDENTIFIER, JsonUtil.toJsonArray(
                    successIdentifiers != null ? successIdentifiers.toArray(new String[0]) : new String[0]));
            dataResponseArray.add(jsonObject);
            //return response
            LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_OK,
                    MessageConstant.LEARN_SUCCESS, dataResponseArray);
            response.status(HttpStatus.SC_OK);

            return new Gson().toJson(learnerResponse);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        LearnerResponse learnerResponse = new LearnerResponse(HttpStatus.SC_NOT_FOUND,
                MessageConstant.INTERNAL_ERROR, null);
        response.status(HttpStatus.SC_NOT_FOUND);
        return new Gson().toJson(learnerResponse);
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
            logger.error("Error occurs when create spark context: " + e.getMessage(), e);
            return false;
        }
    }

    private static List<String> generateAndSaveModel(CoreOptimizationRule optimizationRule) {
        List<String> successIdentifiers = new ArrayList<>();
        List<CoreLearner> modelList = new ArrayList<>();
        List<CoreLearner> modelSafeList = Collections.synchronizedList(modelList);

        List<String> identifiers = optimizationRuleService.getIdentifiers(optimizationRule);
        List<String> segmentFields = JsonUtil.jsonArrayStringToJavaList(optimizationRule.getSegmentFields());

        if (identifiers.size() == 0) {
            return null;
        }

        List<PredictionParam> predictionParams = new ArrayList<>();

        for (String identifier : identifiers) {
            PredictionParam predictionParam = new PredictionParam(optimizationRule.getId(), identifier, segmentFields);
            predictionParams.add(predictionParam);
        }
        ExecutorService executorService = Executors.newFixedThreadPool(ConfigLoaderUtil.getExecuteServiceThreadLeaner());

        for (PredictionParam predictionParam : predictionParams) {
            executorService.execute(() -> {
                List<CoreLearner> coreLearnerList = generateModelForOneIdentifier(predictionParam);
                if (coreLearnerList != null && !coreLearnerList.isEmpty()) {
                    for (CoreLearner coreLearner : coreLearnerList) {
                        if (coreLearner != null) {
                            modelSafeList.add(coreLearner);
                        }
                    }
                }
            });
        }
        logger.error("executorService awaitTerminationAfterShutdown");
        ThreadUtil.awaitTerminationAfterShutdown(executorService);
        if (executorService.isShutdown()) {
            saveModelToDatabase(modelSafeList);
        }
        return successIdentifiers;
    }

    private static List<CoreLearner> generateModelForOneIdentifier(PredictionParam predictionParam) {
        List<CoreLearner> coreLearnersList = new ArrayList<>();

        Long optimizationRuleId = predictionParam.getAutoOptimizationId();
        String identifier = predictionParam.getIdentifier();
        List<List<String>> segmentFieldGroups = predictionParam.generateMultipleSegmentFieldGroups();

        for (List<String> segmentFieldGroup : segmentFieldGroups) {
            SegmentFieldGroup segmentFieldGroupObject = new SegmentFieldGroup(optimizationRuleId,
                    identifier, segmentFieldGroup);
            List<CoreLearner> coreLearners = generateModelForSegmentFieldGroup(segmentFieldGroupObject);
            if (coreLearners != null && !coreLearners.isEmpty()) {
                coreLearnersList.addAll(coreLearners);
            }
        }

        return coreLearnersList;
    }

    private static List<CoreLearner> generateModelForSegmentFieldGroup(SegmentFieldGroup segmentFieldGroup) {
        List<CoreLearner> coreLearners = new ArrayList<>();

        OptimizationRuleServiceInterface optimizationRuleService = new OptimizationRuleService();
        List<OptimizeField> optimizeFields = optimizationRuleService.getOptimizeFields(
                segmentFieldGroup.getOptimizationRuleId());

        for (OptimizeField optimizeField : optimizeFields) {
            List<CoreLearner> coreLearnerList = generateModelForOneOptimizeField(segmentFieldGroup, optimizeField);
            if (coreLearnerList != null && !coreLearnerList.isEmpty())
                for (CoreLearner coreLearner : coreLearnerList) {
                    if (coreLearner != null) {
                        coreLearners.add(coreLearner);
                    }
                }
        }

        return coreLearners;
    }

    private static List<CoreLearner> generateModelForOneOptimizeField(SegmentFieldGroup segmentFieldGroup,
                                                                      OptimizeField optimizeField) {
        List<CoreLearner> coreLearners = new ArrayList<>();

        Long optimizationRuleId = segmentFieldGroup.getOptimizationRuleId();
        String identifier = segmentFieldGroup.getIdentifier();
        List<String> oneSegmentGroup = segmentFieldGroup.getOneSegmentFieldGroup();

        //run global
        if (null == oneSegmentGroup) {
            LinearRegressionDataProcess linearRegressionDataProcess = new LinearRegressionDataProcess(
                    optimizationRuleId, identifier, null, optimizeField);
            CoreLearner learners = generateModelForOneValueOfSegmentFieldGroups(linearRegressionDataProcess);
            if (learners != null) {
                coreLearners.add(learners);
            }
            return coreLearners;
        }

        DataTrainingService dataTrainingService = new DataTrainingService(optimizationRuleId, identifier, oneSegmentGroup);
        List<Map<String, Object>> uniqueValuesOfOneSegmentFieldGroup = dataTrainingService.getAllUniqueValuesForOneSegmentFieldGroup();

        for (Map<String, Object> uniqueValue : uniqueValuesOfOneSegmentFieldGroup) {
            LinearRegressionDataProcess linearRegressionDataProcess = new LinearRegressionDataProcess(
                    optimizationRuleId, identifier, oneSegmentGroup, uniqueValue, optimizeField);
            CoreLearner learners = generateModelForOneValueOfSegmentFieldGroups(linearRegressionDataProcess);
            if (learners != null) {
                coreLearners.add(learners);
            }
        }
        return coreLearners;
    }

    private static CoreLearner generateModelForOneValueOfSegmentFieldGroups(
            LinearRegressionDataProcess linearRegressionDataProcess) {
        LinearRegressionLearner linearRegressionLearner = new LinearRegressionLearner(linearRegressionDataProcess);
        LinearRegressionModel linearRegressionModel = linearRegressionLearner.generateModel(sparkSession);

        CoreLearner coreLearner = new CoreLearner();
        coreLearner.setId(0L);
        coreLearner.setIdentifier(linearRegressionDataProcess.getIdentifier());
        coreLearner.setOptimizationRuleId(linearRegressionDataProcess.getOptimizationRuleId());
        coreLearner.setSegmentValues(JsonUtil.mapToJson(linearRegressionDataProcess.getUniqueValue()));
        coreLearner.setOptimizeFields(JsonUtil.toJson(linearRegressionDataProcess.getOptimizeField()));
        if (linearRegressionModel == null) {
            return null;
        } else {
            coreLearner.setModelPath(FilePathUtil.getLearnerModelPath(
                    linearRegressionDataProcess.getOptimizationRuleId(),
                    linearRegressionDataProcess.getIdentifier(),
                    linearRegressionDataProcess.getOneSegmentGroup(),
                    linearRegressionDataProcess.getUniqueValue(),
                    linearRegressionDataProcess.getOptimizeField().getField()));
            coreLearner.setMathModel(getModelStringData(linearRegressionDataProcess, linearRegressionModel));
            coreLearner.setMetricsPredictiveValues(linearRegressionDataProcess.getMetricsPredictiveValues().toString());
        }

        return coreLearner;
    }

    /**
     * save list of model to database
     *
     * @param modelList list of model
     */
    private static void saveModelToDatabase(List<CoreLearner> modelList) {
        coreLearnerService.saveListLearnerModel(modelList);
    }


    private static String getModelStringData(
            LinearRegressionDataProcess linearRegressionDataProcess, LinearRegressionModel linearRegressionModel) {
        List<String> objectiveAndFields = linearRegressionDataProcess.getObjectiveAndFields();
        JsonObject jsonObject = new JsonObject();
        //coefficient
        Vector vec = linearRegressionModel.coefficients();
        double[] coefficientsArray = vec.toArray();
        JsonObject coefficient = new JsonObject();
        for (int i = 0; i < coefficientsArray.length; i++) {
            int factorIndex = i + 1;// index 0 is objective
            if (Double.isNaN(coefficientsArray[i])) {
                coefficient.addProperty(objectiveAndFields.get(factorIndex), MyConstant.NULL_COEFFICIENT);
            } else {
                double value = ConvertUtil.convertObjectToDecimal(coefficientsArray[i]).doubleValue();
                coefficient.addProperty(objectiveAndFields.get(factorIndex), value);
            }
        }
        jsonObject.add(MyConstant.COEFFICIENT, coefficient);
        if (Double.isNaN(linearRegressionModel.intercept())) {
            jsonObject.addProperty(MyConstant.INTERCEPT, MyConstant.NULL_COEFFICIENT);
        } else {
            double value = ConvertUtil.convertObjectToDecimal(linearRegressionModel.intercept()).doubleValue();
            jsonObject.addProperty(MyConstant.INTERCEPT, value);
        }
        return jsonObject.toString();
    }

}
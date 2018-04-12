package com.pubvantage.constant;

/**
 * Created by quyendq on 02/03/2018.
 */
public class MyConstant {
    public static int DEFAULT_PORT = 8086;

    public static String FIELD = "field";
    public static String WEIGHT = "weight";
    public static String GOAL = "goal";
    public static String MAX = "Max";
    public static String MIN = "Min";
    public static String OVERALL_SCORE = "overallScore";
    public static String IDENTIFIER_COLUMN = "identifier";
    public static String COEFFICIENT = "Coefficients";
    public static String INTERCEPT = "Intercept";
    public static String OPTIMIZE_RULE_ID = "optimizationRuleId";
    public static String IDENTIFIER = "identifiers";
    public static String DATE_FORMAT = "%Y-%m-%d";
    public static String DATE_FORMAT_JAVA = "yyyy-MM-dd";

    public static String DATE_TYPE = "date";
    public static String DATETIME_TYPE = "datetime";
    public static String NUMBER_TYPE = "number";
    public static String DECIMAL_TYPE = "decimal";
    public static String TEXT_TYPE = "text";
    //score columns
    public static String SCORE_ID = "__id";
    public static String SCORE_IDENTIFIER = "identifier";
    public static String SCORE_SEGMENT_VALUES = "segment_field_values";
    public static String SCORE = "score";
    public static String SCORE_IS_PREDICT = "is_predict";

    public static String SCORE_TABLE_NAME_PRE = "__optimization_rule_score_";
    public static double NO_HISTORY_PREDICTION_VALUE = -Double.MAX_VALUE;
    public static double MODEL_NULL_PREDICTION_VALUE = 0D;
    public static double DEFAULT_SCORE_VALUE = 0.5d;
    public static String GLOBAL_KEY = "NULL";
    public static String GLOBAL_DIMENSION_VALUE = "global";
    public static String NO_SEGMENT = "NO_SEGMENT";

    public static String NULL_COEFFICIENT = "null";

    // convert data set
    public static String INDEX = " Index";
    public static String VECTOR = " Vector";
    public static String VECTOR_INTEGRATED = "VectorIntegrated";
    public static String SEGMENT_KEY = "segment";

    public static String SQL_OR = "OR";
    public static String SQL_AND = "AND";


}

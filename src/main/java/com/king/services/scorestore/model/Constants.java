package com.king.services.scorestore.model;

/**
 * Constants for application
 * Handles :
 * Http Error Messages, Context Names,
 * Config values like Port, Session Expiration, Session ID Length, Score List fetch size
 */
public class Constants {

    public static final int SCORE_LIST_FETCH_SIZE = 15;
    public static final int SESSION_ID_LENGTH =7;
    public static final int SERVER_PORT = 8081;
    public static final int TEST_SERVER_PORT = 8090;
    public static final int SESSION_EXPIRATION_TIME_MINS = 10;
    public static final int QUEUE_CONSUMER_THREAD_POOL_SIZE = 1;
    public static final String SLASH = "/";
    public static final String LOGIN_PATH = "login";
    public static final String HIGH_SCORE_LIST_PATH = "highscorelist";
    public static final String SCORE_PATH = "score";
    public static final String SESSION_KEY_PARAM_NAME = "sessionkey";
    public static final String LEVEL_ID_MISSING_ERROR_MESSAGE = "Level Info is missing.";
    public static final String LOGIN_ID_NAN_ERROR_MESSAGE = "LoginID must be a number/ unsigned integer.";
    public static final String LOGIN_ID_PERSIST_ERROR_MESSAGE = "Error in persisting loginID.";
    public static final String GENERAL_ERROR_MESSAGE = "Error encountered. Please try after some time.";
    public static final String LEVEL_PAYLOAD_NAN_ERROR_MESSAGE = "Level or Payload must be a number/unsigned integer.";
    public static final String LEVEL_NAN_ERROR_MESSAGE = "Level must be a number/unsigned integer.";
    public static final String PAYLOAD_REQUIRED_ERROR_MESSAGE = "Payload required.";
    public static final String SESSIONKEY_REQUIRED_ERROR_MESSAGE = "SessionKey Required.";
    public static final String SESSIONKEY_EXPIRED_ERROR_MESSAGE = "Session Expired.";
    public static final String LEVELS_SKIP_ERROR_MESSAGE = "User cannot skip levels. SessionID : ";
    public static final String LOGINID_OUT_OF_RANGE_ERROR_MESSAGE = "Login ID out of range. Permitted values 0 to 2147483647";
    public static final String LEVEL_OUT_OF_RANGE_ERROR_MESSAGE = "Level out of range. Permitted values 0 to 2147483647";
    public static final String SCORE_OUT_OF_RANGE_ERROR_MESSAGE = "Score out of range. Permitted values 0 to 2147483647";
}

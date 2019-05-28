package com.king.services.scorestore.handler;

import com.king.services.scorestore.model.Constants;
import com.king.services.scorestore.model.UserScoreDetails;
import com.king.services.scorestore.processor.QueueProcessor;
import com.king.services.scorestore.service.ScoreStoreService;
import com.king.services.scorestore.util.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Http Handler used for Registering Score in Async Mode
 * The Async processing is delegated to QueueProcessor Implementation.
 * <p>
 * Returns 200 for Successful Delegation of Score Registration
 * The cases of Expired SessionID are Handled in the AsyncProcessor
 * Returns 400 for SessionID missing , Level or Score Info missing or not a 31 bit unsigned Int
 * Returns 500 for other Exceptions
 */
public class RegisterScoreHttpAsyncHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(RegisterScoreHttpAsyncHandler.class.getName());
    private final BaseHttpHandler baseHttpHandler;
    private final QueueProcessor queueProcessor;
    private ScoreStoreService scoreStoreService;

    public RegisterScoreHttpAsyncHandler(BaseHttpHandler baseHttpHandler, QueueProcessor queueProcessor, ScoreStoreService scoreStoreService) {
        this.baseHttpHandler = baseHttpHandler;
        this.queueProcessor = queueProcessor;
        this.scoreStoreService = scoreStoreService;
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        try {
            baseHttpHandler.handle(httpExchange);
            Optional<String> sessionKeyOptional = Utils.extractQueryParamValue(httpExchange, Constants.SESSION_KEY_PARAM_NAME);
            Optional<String> levelOptional = Utils.extractPathParamValue(httpExchange, Constants.SCORE_PATH);
            Optional<String> scoreOptional = Utils.extractPayload(httpExchange);
            String sessionKey;
            int level;
            int score;
            if (!(levelOptional.isPresent()) || levelOptional.get().length() == 0)
                Utils.generateResponse(httpExchange, Constants.LEVEL_ID_MISSING_ERROR_MESSAGE, 400);
            else if ((level = Integer.parseInt(levelOptional.get())) < 0 || !Utils.rangeCheck(level))
                Utils.generateResponse(httpExchange, Constants.LEVEL_OUT_OF_RANGE_ERROR_MESSAGE, 400);
            else if (!(scoreOptional.isPresent()) || scoreOptional.get().length() == 0)
                Utils.generateResponse(httpExchange, Constants.PAYLOAD_REQUIRED_ERROR_MESSAGE, 400);
            else if ((score = Integer.parseInt(scoreOptional.get())) < 0 || !Utils.rangeCheck(score))
                Utils.generateResponse(httpExchange, Constants.SCORE_OUT_OF_RANGE_ERROR_MESSAGE, 400);
            else if (sessionKeyOptional.isPresent()) {
                sessionKey = sessionKeyOptional.get();
                queueProcessor.put(new UserScoreDetails(sessionKey, level, score, new Date().getTime()));
                Utils.generateResponse(httpExchange, "", 200);
            } else {
                Utils.generateResponse(httpExchange, Constants.SESSIONKEY_REQUIRED_ERROR_MESSAGE, 400);
            }
            return;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Level or Payload must be a number", e);
            Utils.generateResponse(httpExchange, Constants.LEVEL_PAYLOAD_NAN_ERROR_MESSAGE, 400);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception Encountered in RegisterScoreHttpAsyncHandler : ", e);
            Utils.generateResponse(httpExchange, Constants.GENERAL_ERROR_MESSAGE, 500);
        }
    }
}

package com.king.services.scorestore.handler;

import com.king.services.scorestore.exception.ServiceException;
import com.king.services.scorestore.model.Constants;
import com.king.services.scorestore.model.UserSession;
import com.king.services.scorestore.service.ScoreStoreService;
import com.king.services.scorestore.util.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Http Handler used for Registering Score in Sync Mode
 * The Sync processing is delegated to Service Layer Implementation.
 * <p>
 * Returns 200 for Successful Delegation of Score Registration
 * Returns 400 for Level or Score Info missing or not a 31 bit unsigned Int
 * Returns 403 for SessionID expired
 * Returns 500 for Exception from Service Layer or other Exceptions
 */
public class RegisterScoreHttpSyncHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(RegisterScoreHttpSyncHandler.class.getName());
    private BaseHttpHandler baseHttpHandler;
    private ScoreStoreService scoreStoreService;

    public RegisterScoreHttpSyncHandler(BaseHttpHandler baseHttpHandler, ScoreStoreService scoreStoreService) {
        this.baseHttpHandler = baseHttpHandler;
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
            int level = -1;
            int score = -1;
            if (!(levelOptional.isPresent()))
                Utils.generateResponse(httpExchange, Constants.LEVEL_ID_MISSING_ERROR_MESSAGE, 400);
            else if ((level = Integer.parseInt(levelOptional.get())) < 0)
                Utils.generateResponse(httpExchange, Constants.LEVEL_OUT_OF_RANGE_ERROR_MESSAGE, 400);
            else if (!(scoreOptional.isPresent()))
                Utils.generateResponse(httpExchange, Constants.SCORE_OUT_OF_RANGE_ERROR_MESSAGE, 400);
            else if ((score = Integer.parseInt(scoreOptional.get())) < 0)
                Utils.generateResponse(httpExchange, Constants.PAYLOAD_REQUIRED_ERROR_MESSAGE, 400);
            else if (sessionKeyOptional.isPresent()) {
                sessionKey = sessionKeyOptional.get();
                final Optional<UserSession> userSession = scoreStoreService.getUserSession(sessionKey);
                if (userSession.isPresent()) {
                    scoreStoreService.registerScore(userSession.get().getSessionID(), level, score);
                    Utils.generateResponse(httpExchange, "", 200);
                } else {
                    Utils.generateResponse(httpExchange, Constants.SESSIONKEY_INVALID + sessionKey, 403);
                }
            } else {
                Utils.generateResponse(httpExchange, Constants.SESSIONKEY_REQUIRED_ERROR_MESSAGE, 400);
            }
            return;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Level or Payload must be a number", e);
            Utils.generateResponse(httpExchange, Constants.LEVEL_PAYLOAD_NAN_ERROR_MESSAGE, 400);
        } catch (ServiceException e) {
            LOGGER.log(Level.SEVERE, "ServiceException Encountered in RegisterScoreHttpSyncHandler : ", e);
            Utils.generateResponse(httpExchange, e.getMessage(), 500);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception Encountered in RegisterScoreHttpSyncHandler : ", e);
            Utils.generateResponse(httpExchange, Constants.GENERAL_ERROR_MESSAGE, 500);
        }
    }
}

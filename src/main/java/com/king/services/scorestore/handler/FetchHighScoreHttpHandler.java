package com.king.services.scorestore.handler;

import com.king.services.scorestore.exception.ServiceException;
import com.king.services.scorestore.model.Constants;
import com.king.services.scorestore.service.ScoreStoreService;
import com.king.services.scorestore.util.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Http Handler used for Fetching Highest Scores for a given level
 * Returns 200 for Successful fetch irrespective of Scores available for the given Level
 * Payload in comma separated format of LoginID=Score in Desc Order
 * The number of records to be returned is controlled with Constants.SCORE_LIST_FETCH_SIZE = 15 (Default);
 * Returns 400 for Level Info  missing or not a 31 bit unsigned Int
 * Returns 500 for Exceptions from Service Layer or other Exceptions
 */
public class FetchHighScoreHttpHandler implements HttpHandler {
    private static final Logger LOGGER = Logger.getLogger(FetchHighScoreHttpHandler.class.getName());
    private final BaseHttpHandler baseHttpHandler;
    private final ScoreStoreService scoreStoreService;

    public FetchHighScoreHttpHandler(BaseHttpHandler baseHttpHandler, ScoreStoreService scoreStoreService) {
        this.baseHttpHandler = baseHttpHandler;
        this.scoreStoreService = scoreStoreService;
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        try {
            int level;
            baseHttpHandler.handle(httpExchange);
            Optional<String> levelOptional = Utils.extractPathParamValue(httpExchange, Constants.HIGH_SCORE_LIST_PATH);
            if (!levelOptional.isPresent() || levelOptional.get().length() == 0) {
                Utils.generateResponse(httpExchange, Constants.LEVEL_ID_MISSING_ERROR_MESSAGE, 400);
            } else if ((level = Integer.parseInt(levelOptional.get())) < 0 || !Utils.rangeCheck(level))
                Utils.generateResponse(httpExchange, Constants.LEVEL_OUT_OF_RANGE_ERROR_MESSAGE, 400);
            else {
                level = Integer.parseInt(levelOptional.get());
                String userList = scoreStoreService.getTopNScoresForLevelDesc(level);
                Utils.generateResponse(httpExchange, (userList), 200);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Level must be a number", e);
            Utils.generateResponse(httpExchange, Constants.LEVEL_NAN_ERROR_MESSAGE, 400);
        } catch (ServiceException e) {
            LOGGER.log(Level.SEVERE, "Service Exception Encountered in FetchHighScoreHttpHandler : ", e);
            Utils.generateResponse(httpExchange, e.getMessage(), 500);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception Exception Encountered in FetchHighScoreHttpHandler : ", e);
            Utils.generateResponse(httpExchange, Constants.GENERAL_ERROR_MESSAGE, 500);
        }
    }
}

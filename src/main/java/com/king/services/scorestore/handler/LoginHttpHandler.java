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
 * Http Handler used for Login Action
 * Returns 200 for Successful fetch along with Payload of SessionID
 * The sessionID is a random String whose length is controlled with Constants.SESSION_ID_LENGTH =7 (default)
 * Returns 400 for Login ID  missing or not a 31 bit unsigned Int
 * Returns 500 for Excpetions from Service Layer or other Exceptions
 */
public class LoginHttpHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(LoginHttpHandler.class.getName());
    private final BaseHttpHandler baseHttpHandler;
    private final ScoreStoreService scoreStoreService;

    public LoginHttpHandler(BaseHttpHandler baseHttpHandler, ScoreStoreService scoreStoreService) {
        this.baseHttpHandler = baseHttpHandler;
        this.scoreStoreService = scoreStoreService;
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        try {
            baseHttpHandler.handle(httpExchange);
            Optional<String> stringOptional = Utils.extractPathParamValue(httpExchange, Constants.LOGIN_PATH);
            final int loginId = Integer.parseInt(stringOptional.get());
            if (!Utils.rangeCheck(loginId))
                Utils.generateResponse(httpExchange, Constants.LOGINID_OUT_OF_RANGE_ERROR_MESSAGE, 400);
            Optional<String> sessionIDOptional = scoreStoreService.generateSessionID(loginId);
            if (sessionIDOptional.isPresent()) {
                Utils.generateResponse(httpExchange, (sessionIDOptional.get()), 200);
            } else {
                Utils.generateResponse(httpExchange, Constants.GENERAL_ERROR_MESSAGE, 500);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "NumberFormatException Exception Encountered in LoginHttpHandler : ", e);
            Utils.generateResponse(httpExchange, Constants.LOGIN_ID_NAN_ERROR_MESSAGE, 400);
        } catch (ServiceException e) {
            LOGGER.log(Level.SEVERE, "Service Exception Encountered in LoginHttpHandler : ", e);
            Utils.generateResponse(httpExchange, Constants.LOGIN_ID_PERSIST_ERROR_MESSAGE, 500);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception Encountered in LoginHttpHandler : ", e);
            Utils.generateResponse(httpExchange, Constants.GENERAL_ERROR_MESSAGE, 500);
        }
    }
}

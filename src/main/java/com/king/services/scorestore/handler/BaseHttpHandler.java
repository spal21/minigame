package com.king.services.scorestore.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.net.URI;
import java.util.logging.Logger;

/**
 * Base Http Handler to print the Http Request
 */
public class BaseHttpHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(BaseHttpHandler.class.getName());

    @Override
    public void handle(HttpExchange httpExchange) {
        printRequestInfo(httpExchange);
    }

    private void printRequestInfo(HttpExchange exchange) {
        LOGGER.info("\n------------");
        Headers requestHeaders = exchange.getRequestHeaders();
        LOGGER.info("-- headers --");
        requestHeaders.entrySet().forEach(e -> LOGGER.info(e.getKey() + " : " + e.getValue()));
        String requestMethod = exchange.getRequestMethod();
        LOGGER.info("-- HTTP method : " + requestMethod);
        URI requestURI = exchange.getRequestURI();
        String query = requestURI.getQuery();
        LOGGER.info("-- query : " + query);
        LOGGER.info("-- Path : " + requestURI.getPath());
        LOGGER.info("-----------\n");
    }
}

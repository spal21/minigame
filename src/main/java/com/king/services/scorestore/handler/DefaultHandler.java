package com.king.services.scorestore.handler;

import com.king.services.scorestore.util.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Default Http Handler for health check of Http Server
 */
public class DefaultHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) {
        Utils.generateResponse(httpExchange, "OK!! Working Fine", 200);
    }
}

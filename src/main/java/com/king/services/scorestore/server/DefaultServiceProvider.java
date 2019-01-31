package com.king.services.scorestore.server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class DefaultServiceProvider extends BasicHttpServerProvider {
    @Override
    public HttpServer createHttpServer(InetSocketAddress inetSocketAddress) throws IOException {
        return new BasicHttpServer(inetSocketAddress);
    }
}

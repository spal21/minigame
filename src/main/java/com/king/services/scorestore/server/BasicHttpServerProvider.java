package com.king.services.scorestore.server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public abstract class BasicHttpServerProvider {

    public static BasicHttpServerProvider newInstance() {
        return Holder.provider;
    }

    public abstract HttpServer createHttpServer(InetSocketAddress inetSocketAddress) throws IOException;

    private static class Holder {
        private static final BasicHttpServerProvider provider = new DefaultServiceProvider();
    }
}

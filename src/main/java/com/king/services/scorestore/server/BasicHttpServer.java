package com.king.services.scorestore.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

class BasicHttpServer extends HttpServer {
    private BasicServerImpl server;

    BasicHttpServer() throws IOException {
        this(new InetSocketAddress(80));
    }

    BasicHttpServer(InetSocketAddress inetSocketAddress) throws IOException {
        this.server = new BasicServerImpl(this, inetSocketAddress, 0);
    }

    public void bind(InetSocketAddress inetSocketAddress, int backlog) throws IOException {
        this.server.bind(inetSocketAddress, 0);
    }

    public void start() {
        this.server.start();
    }

    public Executor getExecutor() {
        return this.server.getExecutor();
    }

    public void setExecutor(Executor var1) {
        this.server.setExecutor(var1);
    }

    public void stop(int var1) {
        this.server.stop(var1);
    }


    public BasicHttpContext createContext(String path, HttpHandler httpHandler) {
        return this.server.createContext(path, httpHandler);
    }

    public BasicHttpContext createContext(String path) {
        return this.server.createContext(path);
    }

    public void removeContext(String var1) throws IllegalArgumentException {
        this.server.removeContext(var1);
    }

    public void removeContext(HttpContext var1) throws IllegalArgumentException {
        this.server.removeContext(var1);
    }

    public InetSocketAddress getAddress() {
        return this.server.getAddress();
    }
}

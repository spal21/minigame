package com.king.services.scorestore.server;

import com.sun.net.httpserver.*;

import java.util.*;
import java.util.logging.Logger;

public class BasicHttpContext extends HttpContext {

    private String path;
    private boolean hasPathParams;
    private HttpHandler handler;
    private Map<String, Object> attributes = new HashMap<>();
    private BasicServerImpl server;
    private LinkedList<Filter> filters = new LinkedList<>();
    private LinkedList<Filter> sfilters = new LinkedList();
    private Authenticator authenticator;

    BasicHttpContext(String path, HttpHandler handler, BasicServerImpl server) {
        if (path != null && path.length() >= 1 && path.charAt(0) == '/') {
            if (path.contains(Code.PATH_PARAM)) {
                hasPathParams = true;
                path = path.replace("/" + Code.PATH_PARAM, "");
            }
            this.path = path;
            this.handler = handler;
            this.server = server;
        } else {
            throw new IllegalArgumentException("Illegal value for path");
        }
    }

    @Override
    public HttpHandler getHandler() {
        return handler;
    }

    @Override
    public void setHandler(HttpHandler httpHandler) {
        if (httpHandler == null) {
            throw new NullPointerException("Null handler parameter");
        } else if (this.handler != null) {
            throw new IllegalArgumentException("handler already set");
        } else {
            this.handler = httpHandler;
        }
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public HttpServer getServer() {
        return server.getWrapper();
    }

    BasicServerImpl getServerImpl() {
        return this.server;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public List<Filter> getFilters() {
        return filters;
    }

    List<Filter> getSystemFilters() {
        return this.sfilters;
    }


    @Override
    public Authenticator setAuthenticator(Authenticator authenticator) {
        Authenticator oldValue = this.authenticator;
        this.authenticator = authenticator;
        return oldValue;
    }

    @Override
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    Logger getLogger() {
        return this.server.getLogger();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicHttpContext that = (BasicHttpContext) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {

        return Objects.hash(path);
    }
}

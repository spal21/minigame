package com.king.services.scorestore.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class BasicHttpExchange extends HttpExchange {
    BasicExchangeImpl impl;

    public BasicHttpExchange(BasicExchangeImpl var1) {
        this.impl = var1;
    }

    public Headers getRequestHeaders() {
        return this.impl.getRequestHeaders();
    }

    public Headers getResponseHeaders() {
        return this.impl.getResponseHeaders();
    }

    public URI getRequestURI() {
        return this.impl.getRequestURI();
    }

    public String getRequestMethod() {
        return this.impl.getRequestMethod();
    }

    public HttpContext getHttpContext() {
        return this.impl.getHttpContext();
    }

    public void close() {
        this.impl.close();
    }

    public InputStream getRequestBody() {
        return this.impl.getRequestBody();
    }

    public int getResponseCode() {
        return this.impl.getResponseCode();
    }

    public OutputStream getResponseBody() {
        return this.impl.getResponseBody();
    }

    public void sendResponseHeaders(int var1, long var2) throws IOException {
        this.impl.sendResponseHeaders(var1, var2);
    }

    public InetSocketAddress getRemoteAddress() {
        return this.impl.getRemoteAddress();
    }

    public InetSocketAddress getLocalAddress() {
        return this.impl.getLocalAddress();
    }

    public String getProtocol() {
        return this.impl.getProtocol();
    }

    public Object getAttribute(String var1) {
        return this.impl.getAttribute(var1);
    }

    public void setAttribute(String var1, Object var2) {
        this.impl.setAttribute(var1, var2);
    }

    public void setStreams(InputStream var1, OutputStream var2) {
        this.impl.setStreams(var1, var2);
    }

    public HttpPrincipal getPrincipal() {
        return this.impl.getPrincipal();
    }

    BasicExchangeImpl getExchangeImpl() {
        return this.impl;
    }
}

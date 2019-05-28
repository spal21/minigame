package com.king.services.scorestore.server;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class BasicContextList {
    private static final Logger logger = Logger.getLogger(BasicContextList.class.getName());
    private Map<String, BasicHttpContext> contextMap = new HashMap<>();

    BasicContextList() {
    }

    public synchronized void add(BasicHttpContext context) {
        contextMap.put(context.getPath(), context);
    }

    public synchronized int size() {
        return this.contextMap.size();
    }

    public synchronized BasicHttpContext findContext(String path) {
        return this.contextMap.entrySet().stream().filter(e -> path.endsWith(e.getKey()) || e.getKey().equals(path)).map(e -> e.getValue()).findAny().get();
    }

    public synchronized void remove(String path) throws IllegalArgumentException {
        BasicHttpContext context = this.findContext(path);
        if (context == null) {
            throw new IllegalArgumentException("cannot remove element from list");
        } else {
            this.contextMap.remove(path);
            logger.info("Context removed : " + context.getPath());
        }
    }

    public synchronized void remove(BasicHttpContext context) throws IllegalArgumentException {
        remove(context.getPath());
    }

}


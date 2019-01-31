package com.king.services.scorestore.server;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class BasicServerConfig {
    private static final int DEFAULT_CLOCK_TICK = 10000;
    private static final long DEFAULT_IDLE_INTERVAL = 30L;
    private static final int DEFAULT_MAX_IDLE_CONNECTIONS = 200;
    private static final long DEFAULT_MAX_REQ_TIME = -1L;
    private static final long DEFAULT_MAX_RSP_TIME = -1L;
    private static final long DEFAULT_TIMER_MILLIS = 1000L;
    private static final int DEFAULT_MAX_REQ_HEADERS = 200;
    private static final long DEFAULT_DRAIN_AMOUNT = 65536L;
    private static int clockTick;
    private static long idleInterval;
    private static long drainAmount;
    private static int maxIdleConnections;
    private static int maxReqHeaders;
    private static long maxReqTime;
    private static long maxRspTime;
    private static long timerMillis;
    private static boolean debug;
    private static boolean noDelay;

    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                BasicServerConfig.idleInterval = Long.getLong("sun.net.httpserver.idleInterval", 30L) * 1000L;
                BasicServerConfig.clockTick = Integer.getInteger("sun.net.httpserver.clockTick", 10000);
                BasicServerConfig.maxIdleConnections = Integer.getInteger("sun.net.httpserver.maxIdleConnections", 200);
                BasicServerConfig.drainAmount = Long.getLong("sun.net.httpserver.drainAmount", 65536L);
                BasicServerConfig.maxReqHeaders = Integer.getInteger("sun.net.httpserver.maxReqHeaders", 200);
                BasicServerConfig.maxReqTime = Long.getLong("sun.net.httpserver.maxReqTime", -1L);
                BasicServerConfig.maxRspTime = Long.getLong("sun.net.httpserver.maxRspTime", -1L);
                BasicServerConfig.timerMillis = Long.getLong("sun.net.httpserver.timerMillis", 1000L);
                BasicServerConfig.debug = Boolean.getBoolean("sun.net.httpserver.debug");
                BasicServerConfig.noDelay = Boolean.getBoolean("sun.net.httpserver.nodelay");
                return null;
            }
        });
    }


    BasicServerConfig() {
    }

    static boolean debugEnabled() {
        return debug;
    }

    static long getIdleInterval() {
        return idleInterval;
    }

    static int getClockTick() {
        return clockTick;
    }

    static int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    static long getDrainAmount() {
        return drainAmount;
    }

    static int getMaxReqHeaders() {
        return maxReqHeaders;
    }

    static long getMaxReqTime() {
        return maxReqTime;
    }

    static long getMaxRspTime() {
        return maxRspTime;
    }

    static long getTimerMillis() {
        return timerMillis;
    }

    static boolean noDelay() {
        return noDelay;
    }
}

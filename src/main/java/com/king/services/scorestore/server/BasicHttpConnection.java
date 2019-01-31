package com.king.services.scorestore.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class BasicHttpConnection {
    BasicHttpContext context;
    InputStream i;
    InputStream raw;
    OutputStream rawout;
    SocketChannel chan;
    SelectionKey selectionKey;
    // String protocol;
    long time;
    volatile long creationTime;
    volatile long rspStartedTime;
    int remaining;
    boolean closed = false;
    Logger logger;
    volatile BasicHttpConnection.State state;

    BasicHttpConnection() {
    }

    public String toString() {
        String var1 = null;
        if (this.chan != null) {
            var1 = this.chan.toString();
        }

        return var1;
    }

    void setContext(BasicHttpContext var1) {
        this.context = var1;
    }

    BasicHttpConnection.State getState() {
        return this.state;
    }

    void setState(BasicHttpConnection.State var1) {
        this.state = var1;
    }

    void setParameters(InputStream var1, OutputStream var2, SocketChannel var3, BasicHttpContext var8, InputStream var9) {
        this.context = var8;
        this.i = var1;
        this.rawout = var2;
        this.raw = var9;
        this.chan = var3;
        this.logger = var8.getLogger();
    }

    SocketChannel getChannel() {
        return this.chan;
    }

    void setChannel(SocketChannel var1) {
        this.chan = var1;
    }

    synchronized void close() {
        if (!this.closed) {
            this.closed = true;
            if (this.logger != null && this.chan != null) {
                this.logger.finest("Closing connection: " + this.chan.toString());
            }

            if (!this.chan.isOpen()) {
                BasicServerImpl.getLogger().info("Channel already closed");
            } else {
                try {
                    if (this.raw != null) {
                        this.raw.close();
                    }
                } catch (IOException var5) {
                    BasicServerImpl.getLogger().info(var5.getMessage());
                    BasicServerImpl.dprint(var5);
                }

                try {
                    if (this.rawout != null) {
                        this.rawout.close();
                    }
                } catch (IOException var4) {
                    BasicServerImpl.dprint(var4);
                }

                try {
                    this.chan.close();
                } catch (IOException var2) {
                    BasicServerImpl.dprint(var2);
                }

            }
        }
    }

    int getRemaining() {
        return this.remaining;
    }

    void setRemaining(int var1) {
        this.remaining = var1;
    }

    SelectionKey getSelectionKey() {
        return this.selectionKey;
    }

    InputStream getInputStream() {
        return this.i;
    }

    OutputStream getRawOutputStream() {
        return this.rawout;
    }

    BasicHttpContext getHttpContext() {
        return this.context;
    }

    public static enum State {
        IDLE,
        REQUEST,
        RESPONSE;

        private State() {
        }
    }
}

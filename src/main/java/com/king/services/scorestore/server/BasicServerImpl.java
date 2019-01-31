//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.king.services.scorestore.server;

import com.king.services.scorestore.handler.DefaultHandler;
import com.sun.net.httpserver.Filter.Chain;
import com.sun.net.httpserver.*;

import javax.net.ssl.SSLEngine;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.channels.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

class BasicServerImpl implements TimeSource {
    private static final int CLOCK_TICK = BasicServerConfig.getClockTick();
    private static final long IDLE_INTERVAL = BasicServerConfig.getIdleInterval();
    private static final int MAX_IDLE_CONNECTIONS = BasicServerConfig.getMaxIdleConnections();
    private static final long TIMER_MILLIS = BasicServerConfig.getTimerMillis();
    private static final Logger logger = Logger.getLogger(BasicServerImpl.class.getName());

    static {
        logger.setLevel(Level.ALL);
    }

    BasicServerImpl.Dispatcher dispatcher;
    private Executor executor;
    private BasicContextList contexts;
    private InetSocketAddress address;
    private ServerSocketChannel schan;
    private Selector selector;
    private SelectionKey listenerKey;
    private Set<BasicHttpConnection> idleConnections;
    private Set<BasicHttpConnection> allConnections;
    private Set<BasicHttpConnection> reqConnections;
    private Set<BasicHttpConnection> rspConnections;
    private List<BasicEvent> events;
    private Object lolock = new Object();
    private volatile boolean finished;
    private volatile boolean terminating;
    private boolean bound = false;
    private boolean started = false;
    private volatile long time;
    private volatile long ticks;
    private HttpServer wrapper;
    private Timer timer;
    private int exchangeCount = 0;
    private HttpHandler defaultHandler = new DefaultHandler();

    BasicServerImpl(HttpServer server, InetSocketAddress socketAddress, int backlog) throws IOException {

        this.wrapper = server;
        this.address = socketAddress;
        this.contexts = new BasicContextList();
        this.schan = ServerSocketChannel.open();
        if (socketAddress != null) {
            ServerSocket var5 = this.schan.socket();
            var5.bind(socketAddress, backlog);
            this.bound = true;
        }

        this.selector = Selector.open();
        this.schan.configureBlocking(false);
        this.listenerKey = this.schan.register(this.selector, 16);
        this.dispatcher = new BasicServerImpl.Dispatcher();
        this.idleConnections = Collections.synchronizedSet(new HashSet());
        this.allConnections = Collections.synchronizedSet(new HashSet());
        this.reqConnections = Collections.synchronizedSet(new HashSet());
        this.rspConnections = Collections.synchronizedSet(new HashSet());
        this.time = System.currentTimeMillis();
        this.timer = new Timer("server-timer", true);
        this.timer.schedule(new BasicServerImpl.ServerTimerTask(), (long) CLOCK_TICK, (long) CLOCK_TICK);
        this.events = new LinkedList();
    }

    static long getTimeMillis(long var0) {
        return var0 == -1L ? -1L : var0 * 1000L;
    }

    static Logger getLogger() {
        return logger;
    }

    static void dprint(Exception e) {
        System.out.println(e);
    }

    public void bind(InetSocketAddress address, int backlog) throws IOException {
        if (this.bound) {
            throw new BindException("HttpServer already bound");
        } else if (address == null) {
            throw new NullPointerException("null address");
        } else {
            ServerSocket var3 = this.schan.socket();
            var3.bind(address, backlog);
            this.bound = true;
        }
    }

    public void start() {
        if (this.bound && !this.started && !this.finished) {
            if (this.executor == null) {
                this.executor = new BasicServerImpl.DefaultExecutor();
            }

            Thread thread = new Thread(this.dispatcher);
            this.started = true;
            thread.start();
            logger.info("Server Started at " + address);
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException("server in wrong state");
        }
    }

    public Executor getExecutor() {

        return this.executor;
    }

    public void setExecutor(Executor var1) {
        if (this.started) {
            throw new IllegalStateException("server already started");
        } else {
            this.executor = var1;
        }
    }

    public void stop(int var1) {
        if (var1 < 0) {
            throw new IllegalArgumentException("negative delay parameter");
        } else {
            this.terminating = true;

            try {
                this.schan.close();
            } catch (IOException var8) {
                ;
            }

            this.selector.wakeup();
            long var2 = System.currentTimeMillis() + (long) (var1 * 1000);

            while (System.currentTimeMillis() < var2) {
                this.delay();
                if (this.finished) {
                    break;
                }
            }

            this.finished = true;
            this.selector.wakeup();
            Set var4 = this.allConnections;
            synchronized (this.allConnections) {
                Iterator var5 = this.allConnections.iterator();

                while (true) {
                    if (!var5.hasNext()) {
                        break;
                    }

                    BasicHttpConnection var6 = (BasicHttpConnection) var5.next();
                    var6.close();
                }
            }

            this.allConnections.clear();
            this.idleConnections.clear();
            this.timer.cancel();
        }
        this.logger.info("Stopped Server...");
        System.out.println("Stopped Server...");
    }

    public synchronized BasicHttpContext createContext(String path, HttpHandler handler) {
        if (handler != null && path != null) {
            BasicHttpContext context = new BasicHttpContext(path, handler, this);
            this.contexts.add(context);
            this.logger.info("creating context for: " + path);
            return context;
        } else {
            throw new NullPointerException("null handler, or path parameter");
        }
    }

    public synchronized BasicHttpContext createContext(String path) {
        if (path == null) {
            throw new NullPointerException("null path parameter");
        } else {
            BasicHttpContext context = new BasicHttpContext(path, defaultHandler, this);
            this.logger.info("creating context for: " + path);
            this.contexts.add(context);
            return context;
        }
    }

    public synchronized void removeContext(String var1) throws IllegalArgumentException {
        if (var1 == null) {
            throw new NullPointerException("null path parameter");
        } else {
            this.contexts.remove(var1);
            this.logger.config("removing context for : " + var1);
        }
    }

    public synchronized void removeContext(HttpContext var1) throws IllegalArgumentException {
        if (!(var1 instanceof BasicHttpContext)) {
            throw new IllegalArgumentException("wrong HttpContext type");
        } else {
            this.contexts.remove((BasicHttpContext) var1);
            this.logger.config("removing context for: " + var1.getPath());
        }
    }

    public InetSocketAddress getAddress() {
        return (InetSocketAddress) AccessController.doPrivileged(new PrivilegedAction<InetSocketAddress>() {
            public InetSocketAddress run() {
                return (InetSocketAddress) BasicServerImpl.this.schan.socket().getLocalSocketAddress();
            }
        });
    }

    Selector getSelector() {
        return this.selector;
    }

    void addEvent(BasicEvent var1) {
        Object var2 = this.lolock;
        synchronized (this.lolock) {
            this.events.add(var1);
            this.selector.wakeup();
        }
    }

    private void closeConnection(BasicHttpConnection var1) {
        var1.close();
        this.allConnections.remove(var1);
        switch (var1.getState()) {
            case REQUEST:
                this.reqConnections.remove(var1);
                break;
            case RESPONSE:
                this.rspConnections.remove(var1);
                break;
            case IDLE:
                this.idleConnections.remove(var1);
        }

        assert !this.reqConnections.remove(var1);

        assert !this.rspConnections.remove(var1);

        assert !this.idleConnections.remove(var1);

    }

    void logReply(int var1, String var2, String var3) {
        if (this.logger.isLoggable(Level.FINE)) {
            if (var3 == null) {
                var3 = "";
            }

            String var4;
            if (var2.length() > 80) {
                var4 = var2.substring(0, 80) + "<TRUNCATED>";
            } else {
                var4 = var2;
            }

            String var5 = var4 + " [" + var1 + " " + Code.msg(var1) + "] (" + var3 + ")";
            this.logger.fine(var5);
        }
    }

    long getTicks() {
        return this.ticks;
    }

    public long getTime() {
        return this.time;
    }

    void delay() {
        Thread.yield();

        try {
            Thread.sleep(200L);
        } catch (InterruptedException var2) {
            ;
        }

    }

    synchronized void startExchange() {
        ++this.exchangeCount;
    }

    synchronized int endExchange() {
        --this.exchangeCount;

        assert this.exchangeCount >= 0;

        return this.exchangeCount;
    }

    HttpServer getWrapper() {
        return this.wrapper;
    }

    void requestStarted(BasicHttpConnection var1) {
        var1.creationTime = this.getTime();
        var1.setState(BasicHttpConnection.State.REQUEST);
        this.reqConnections.add(var1);
    }

    void requestCompleted(BasicHttpConnection var1) {
        assert var1.getState() == BasicHttpConnection.State.REQUEST;

        this.reqConnections.remove(var1);
        var1.rspStartedTime = this.getTime();
        this.rspConnections.add(var1);
        var1.setState(BasicHttpConnection.State.RESPONSE);
    }

    void responseCompleted(BasicHttpConnection var1) {
        assert var1.getState() == BasicHttpConnection.State.RESPONSE;

        this.rspConnections.remove(var1);
        var1.setState(BasicHttpConnection.State.IDLE);
    }

    void logStackTrace(String var1) {
        this.logger.finest(var1);
        StringBuilder var2 = new StringBuilder();
        StackTraceElement[] var3 = Thread.currentThread().getStackTrace();

        for (int var4 = 0; var4 < var3.length; ++var4) {
            var2.append(var3[var4].toString()).append("\n");
        }

        this.logger.finest(var2.toString());
    }

    private static class DefaultExecutor implements Executor {
        private DefaultExecutor() {
        }

        public void execute(Runnable var1) {
            var1.run();
        }
    }

    class ServerTimerTask extends TimerTask {
        ServerTimerTask() {
        }

        public void run() {
            LinkedList var1 = new LinkedList();
            BasicServerImpl.this.time = System.currentTimeMillis();
            BasicServerImpl.this.ticks++;
            synchronized (BasicServerImpl.this.idleConnections) {
                Iterator var3 = BasicServerImpl.this.idleConnections.iterator();

                BasicHttpConnection var4;
                while (var3.hasNext()) {
                    var4 = (BasicHttpConnection) var3.next();
                    if (var4.time <= BasicServerImpl.this.time) {
                        var1.add(var4);
                    }
                }

                var3 = var1.iterator();

                while (var3.hasNext()) {
                    var4 = (BasicHttpConnection) var3.next();
                    BasicServerImpl.this.idleConnections.remove(var4);
                    BasicServerImpl.this.allConnections.remove(var4);
                    var4.close();
                }

            }
        }
    }

    class Exchange implements Runnable {
        SocketChannel chan;
        BasicHttpConnection connection;
        BasicHttpContext context;
        InputStream rawin;
        OutputStream rawout;
        BasicExchangeImpl tx;
        BasicHttpContext ctx;
        boolean rejected = false;

        Exchange(SocketChannel var2, BasicHttpConnection var4) throws IOException {
            this.chan = var2;
            this.connection = var4;
        }

        public void run() {
            this.context = this.connection.getHttpContext();
            SSLEngine var2 = null;
            String var3 = null;

            try {
                boolean var1;
                if (this.context != null) {
                    this.rawin = this.connection.getInputStream();
                    this.rawout = this.connection.getRawOutputStream();
                    var1 = false;
                } else {
                    var1 = true;
                    this.rawin = new BufferedInputStream(new BasicRequest.ReadStream(BasicServerImpl.this, this.chan));
                    this.rawout = new BasicRequest.WriteStream(BasicServerImpl.this, this.chan);
                    this.connection.raw = this.rawin;
                    this.connection.rawout = this.rawout;
                }

                BasicRequest var5 = new BasicRequest(this.rawin, this.rawout);
                var3 = var5.requestLine();
                if (var3 == null) {
                    BasicServerImpl.this.closeConnection(this.connection);
                    return;
                }

                int var6 = var3.indexOf(32);
                if (var6 == -1) {
                    this.reject(400, var3, "Bad BasicRequest line");
                    return;
                }

                String var7 = var3.substring(0, var6);
                int var8 = var6 + 1;
                var6 = var3.indexOf(32, var8);
                if (var6 == -1) {
                    this.reject(400, var3, "Bad request line");
                    return;
                }

                String var9 = var3.substring(var8, var6);
                URI var10 = new URI(var9);
                var8 = var6 + 1;
                String var11 = var3.substring(var8);
                Headers var12 = var5.headers();
                String var13 = var12.getFirst("Transfer-encoding");
                long var14 = 0L;
                if (var13 != null && var13.equalsIgnoreCase("chunked")) {
                    var14 = -1L;
                } else {
                    var13 = var12.getFirst("Content-Length");
                    if (var13 != null) {
                        var14 = Long.parseLong(var13);
                    }

                    if (var14 == 0L) {
                        BasicServerImpl.this.requestCompleted(this.connection);
                    }
                }

                this.ctx = BasicServerImpl.this.contexts.findContext(var10.getPath());
                if (this.ctx == null) {
                    this.reject(404, var3, "No context found for request");
                    return;
                }

                this.connection.setContext(this.ctx);
                if (this.ctx.getHandler() == null) {
                    this.reject(500, var3, "No handler for context");
                    return;
                }

                this.tx = new BasicExchangeImpl(var7, var10, var5, var14, this.connection);
                String var16 = var12.getFirst("Connection");
                Headers var17 = this.tx.getResponseHeaders();
                if (var16 != null && var16.equalsIgnoreCase("close")) {
                    this.tx.close = true;
                }

                if (var11.equalsIgnoreCase("http/1.0")) {
                    this.tx.http10 = true;
                    if (var16 == null) {
                        this.tx.close = true;
                        var17.set("Connection", "close");
                    } else if (var16.equalsIgnoreCase("keep-alive")) {
                        var17.set("Connection", "keep-alive");
                        int var18 = (int) (BasicServerConfig.getIdleInterval() / 1000L);
                        int var19 = BasicServerConfig.getMaxIdleConnections();
                        String var20 = "timeout=" + var18 + ", max=" + var19;
                        var17.set("Keep-Alive", var20);
                    }
                }

                if (var1) {
                    this.connection.setParameters(this.rawin, this.rawout, this.chan, this.ctx, this.rawin);
                }

                String var27 = var12.getFirst("Expect");
                if (var27 != null && var27.equalsIgnoreCase("100-continue")) {
                    BasicServerImpl.this.logReply(100, var3, (String) null);
                    this.sendReply(100, false, (String) null);
                }

                List var28 = this.ctx.getSystemFilters();
                List var29 = this.ctx.getFilters();
                Chain var21 = new Chain(var28, this.ctx.getHandler());
                Chain var22 = new Chain(var29, new BasicServerImpl.Exchange.LinkHandler(var21));
                this.tx.getRequestBody();
                this.tx.getResponseBody();
                var22.doFilter(new BasicHttpExchange(this.tx));
            } catch (IOException var23) {
                BasicServerImpl.this.logger.log(Level.FINER, "ServerImpl.Exchange (1)", var23);
                BasicServerImpl.this.closeConnection(this.connection);
            } catch (NumberFormatException var24) {
                this.reject(400, var3, "NumberFormatException thrown");
            } catch (URISyntaxException var25) {
                this.reject(400, var3, "URISyntaxException thrown");
            } catch (Exception var26) {
                BasicServerImpl.this.logger.log(Level.FINER, "BasicServerImpl.Exchange (2)", var26);
                BasicServerImpl.this.closeConnection(this.connection);
            }

        }

        void reject(int var1, String var2, String var3) {
            this.rejected = true;
            BasicServerImpl.this.logReply(var1, var2, var3);
            this.sendReply(var1, false, "<h1>" + var1 + Code.msg(var1) + "</h1>" + var3);
            BasicServerImpl.this.closeConnection(this.connection);
        }

        void sendReply(int var1, boolean var2, String var3) {
            try {
                StringBuilder var4 = new StringBuilder(512);
                var4.append("HTTP/1.1 ").append(var1).append(Code.msg(var1)).append("\r\n");
                if (var3 != null && var3.length() != 0) {
                    var4.append("Content-Length: ").append(var3.length()).append("\r\n").append("Content-Type: text/html\r\n");
                } else {
                    var4.append("Content-Length: 0\r\n");
                    var3 = "";
                }

                if (var2) {
                    var4.append("Connection: close\r\n");
                }

                var4.append("\r\n").append(var3);
                String var5 = var4.toString();
                byte[] var6 = var5.getBytes("ISO8859_1");
                this.rawout.write(var6);
                this.rawout.flush();
                if (var2) {
                    BasicServerImpl.this.closeConnection(this.connection);
                }
            } catch (IOException var7) {
                BasicServerImpl.this.logger.log(Level.FINER, "BasicServerImpl.sendReply", var7);
                BasicServerImpl.this.closeConnection(this.connection);
            }

        }

        class LinkHandler implements HttpHandler {
            Chain nextChain;

            LinkHandler(Chain var2) {
                this.nextChain = var2;
            }

            public void handle(HttpExchange var1) throws IOException {
                this.nextChain.doFilter(var1);
            }
        }
    }

    class Dispatcher implements Runnable {
        final LinkedList<BasicHttpConnection> connsToRegister = new LinkedList();

        Dispatcher() {
        }

        private void handleEvent(BasicEvent var1) {
            BasicExchangeImpl var2 = var1.exchange;
            BasicHttpConnection var3 = var2.getConnection();

            try {
                if (var1 instanceof WriteFinishedEvent) {
                    int var4 = BasicServerImpl.this.endExchange();
                    if (BasicServerImpl.this.terminating && var4 == 0) {
                        BasicServerImpl.this.finished = true;
                    }

                    BasicServerImpl.this.responseCompleted(var3);
                    LeftOverInputStream var5 = var2.getOriginalInputStream();
                    if (!var5.isEOF()) {
                        var2.close = true;
                    }

                    if (!var2.close && BasicServerImpl.this.idleConnections.size() < BasicServerImpl.MAX_IDLE_CONNECTIONS) {
                        if (var5.isDataBuffered()) {
                            BasicServerImpl.this.requestStarted(var3);
                            this.handle(var3.getChannel(), var3);
                        } else {
                            this.connsToRegister.add(var3);
                        }
                    } else {
                        var3.close();
                        BasicServerImpl.this.allConnections.remove(var3);
                    }
                }
            } catch (IOException var6) {
                BasicServerImpl.this.logger.log(Level.FINER, "Dispatcher (1)", var6);
                var3.close();
            }

        }

        void reRegister(BasicHttpConnection var1) {
            try {
                SocketChannel var2 = var1.getChannel();
                var2.configureBlocking(false);
                SelectionKey var3 = var2.register(BasicServerImpl.this.selector, 1);
                var3.attach(var1);
                var1.selectionKey = var3;
                var1.time = BasicServerImpl.this.getTime() + BasicServerImpl.IDLE_INTERVAL;
                BasicServerImpl.this.idleConnections.add(var1);
            } catch (IOException var4) {
                BasicServerImpl.this.logger.log(Level.FINER, "Dispatcher(8)", var4);
                var1.close();
            }

        }

        public void run() {
            while (!BasicServerImpl.this.finished) {
                try {
                    List var1 = null;
                    synchronized (BasicServerImpl.this.lolock) {
                        if (BasicServerImpl.this.events.size() > 0) {
                            var1 = BasicServerImpl.this.events;
                            BasicServerImpl.this.events = new LinkedList();
                        }
                    }

                    Iterator var2;
                    if (var1 != null) {
                        var2 = var1.iterator();

                        while (var2.hasNext()) {
                            BasicEvent var3 = (BasicEvent) var2.next();
                            this.handleEvent(var3);
                        }
                    }

                    var2 = this.connsToRegister.iterator();

                    while (var2.hasNext()) {
                        BasicHttpConnection var15 = (BasicHttpConnection) var2.next();
                        this.reRegister(var15);
                    }

                    this.connsToRegister.clear();
                    BasicServerImpl.this.selector.select(1000L);
                    Set var14 = BasicServerImpl.this.selector.selectedKeys();
                    Iterator var16 = var14.iterator();

                    while (var16.hasNext()) {
                        SelectionKey var4 = (SelectionKey) var16.next();
                        var16.remove();
                        BasicHttpConnection var7;
                        if (var4.equals(BasicServerImpl.this.listenerKey)) {
                            if (!BasicServerImpl.this.terminating) {
                                SocketChannel var5 = BasicServerImpl.this.schan.accept();
                                if (BasicServerConfig.noDelay()) {
                                    var5.socket().setTcpNoDelay(true);
                                }

                                if (var5 != null) {
                                    var5.configureBlocking(false);
                                    SelectionKey var6 = var5.register(BasicServerImpl.this.selector, 1);
                                    var7 = new BasicHttpConnection();
                                    var7.selectionKey = var6;
                                    var7.setChannel(var5);
                                    var6.attach(var7);
                                    BasicServerImpl.this.requestStarted(var7);
                                    BasicServerImpl.this.allConnections.add(var7);
                                }
                            }
                        } else {
                            try {
                                if (var4.isReadable()) {
                                    SocketChannel var17 = (SocketChannel) var4.channel();
                                    var7 = (BasicHttpConnection) var4.attachment();
                                    var4.cancel();
                                    var17.configureBlocking(true);
                                    if (BasicServerImpl.this.idleConnections.remove(var7)) {
                                        BasicServerImpl.this.requestStarted(var7);
                                    }

                                    this.handle(var17, var7);
                                } else {
                                    assert false;
                                }
                            } catch (CancelledKeyException var8) {
                                this.handleException(var4, (Exception) null);
                            } catch (IOException var9) {
                                this.handleException(var4, var9);
                            }
                        }
                    }

                    BasicServerImpl.this.selector.selectNow();
                } catch (IOException var12) {
                    BasicServerImpl.this.logger.log(Level.FINER, "Dispatcher (4)", var12);
                } catch (Exception var13) {
                    BasicServerImpl.this.logger.log(Level.FINER, "Dispatcher (7)", var13);
                }
            }

            try {
                BasicServerImpl.this.selector.close();
            } catch (Exception var10) {
                ;
            }

        }

        private void handleException(SelectionKey var1, Exception var2) {
            BasicHttpConnection var3 = (BasicHttpConnection) var1.attachment();
            if (var2 != null) {
                BasicServerImpl.this.logger.log(Level.FINER, "Dispatcher (2)", var2);
            }

            BasicServerImpl.this.closeConnection(var3);
        }

        public void handle(SocketChannel var1, BasicHttpConnection var2) throws IOException {
            try {
                BasicServerImpl.Exchange var3 = BasicServerImpl.this.new Exchange(var1, var2);
                BasicServerImpl.this.executor.execute(var3);
            } catch (HttpError var4) {
                BasicServerImpl.this.logger.log(Level.FINER, "Dispatcher (4)", var4);
                BasicServerImpl.this.closeConnection(var2);
            } catch (IOException var5) {
                BasicServerImpl.this.logger.log(Level.FINER, "Dispatcher (5)", var5);
                BasicServerImpl.this.closeConnection(var2);
            }

        }
    }
}

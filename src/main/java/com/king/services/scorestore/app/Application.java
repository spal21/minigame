package com.king.services.scorestore.app;

import com.king.services.scorestore.handler.DefaultHandler;
import com.king.services.scorestore.handler.FetchHighScoreHttpHandler;
import com.king.services.scorestore.handler.LoginHttpHandler;
import com.king.services.scorestore.handler.RegisterScoreHttpAsyncHandler;
import com.king.services.scorestore.server.Code;
import com.king.services.scorestore.dao.ScoreStoreDAO;
import com.king.services.scorestore.dao.impl.InMemoryScoreStoreDAOImpl;
import com.king.services.scorestore.handler.*;
import com.king.services.scorestore.model.Constants;
import com.king.services.scorestore.processor.QueueConsumer;
import com.king.services.scorestore.processor.QueueProcessor;
import com.king.services.scorestore.processor.impl.InMemoryQueueProcessorImpl;
import com.king.services.scorestore.processor.impl.RegisterScoreQueueConsumerImpl;
import com.king.services.scorestore.server.BasicHttpServerProvider;
import com.king.services.scorestore.service.ScoreStoreService;
import com.king.services.scorestore.service.impl.ScoreStoreServiceImpl;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main Application to Instantiate Http Server and register context paths
 * Call run() to start the server
 * Call closeServer() to Terminate the HttpServer gracefully
 */
public class Application {

    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());
    private QueueConsumer queueConsumer;
    private HttpServer server;

    public void run() {
        this.server = instantiateHttpServer(Constants.SERVER_PORT);
        init(server);
    }

    public void runTestServer() {
        this.server = instantiateHttpServer(Constants.TEST_SERVER_PORT);
        init(server);
    }

    private void init(HttpServer server) {
        if (Objects.nonNull(server)) {
            QueueProcessor queueProcessor = getQueueProcessor();

            ScoreStoreDAO scoreStoreDAO = getScoreStoreDAO();
            ScoreStoreService scoreStoreService = getScoreStoreService(scoreStoreDAO);
            BaseHttpHandler baseHttpHandler = getBaseHttpHandler();
            server.createContext(Constants.SLASH, getDefaultHttpHandler());
            server.createContext(Constants.SLASH + Code.PATH_PARAM + Constants.SLASH + Constants.LOGIN_PATH,
                    getLoginHttpHandler(baseHttpHandler, scoreStoreService));
            server.createContext(Constants.SLASH + Code.PATH_PARAM + Constants.SLASH + Constants.SCORE_PATH,
                    //getRegisterScoreHttpSyncHandler(baseHttpHandler, scoreStoreService));
                    getRegisterScoreHttpAsyncHandler(baseHttpHandler, queueProcessor));
            server.createContext(Constants.SLASH + Code.PATH_PARAM + Constants.SLASH + Constants.HIGH_SCORE_LIST_PATH,
                    getFetchHighScoreHttpHandler(baseHttpHandler, scoreStoreService));

            //For Async Processing
            queueConsumer = getQueueConsumer(queueProcessor, scoreStoreService);
            queueConsumer.start();
            server.start();
        }
    }

    private QueueConsumer getQueueConsumer(QueueProcessor queueProcessor, ScoreStoreService scoreStoreService) {
        LOGGER.info("Creating RegisterScoreQueueConsumerImpl Bean");
        return new RegisterScoreQueueConsumerImpl(queueProcessor, scoreStoreService);
    }

    private ScoreStoreDAO getScoreStoreDAO() {
        LOGGER.info("Creating ScoreStoreDAO Bean");
        return new InMemoryScoreStoreDAOImpl();
    }

    private ScoreStoreService getScoreStoreService(ScoreStoreDAO scoreStoreDAO) {
        LOGGER.info("Creating ScoreStoreService Bean");
        return new ScoreStoreServiceImpl(scoreStoreDAO);
    }

    private BaseHttpHandler getBaseHttpHandler() {
        LOGGER.info("Creating BaseHttpHandler Bean");
        return new BaseHttpHandler();
    }

    private DefaultHandler getDefaultHttpHandler() {
        LOGGER.info("Creating DefaultHandler Bean");
        return new DefaultHandler();
    }

    private LoginHttpHandler getLoginHttpHandler(BaseHttpHandler baseHttpHandler, ScoreStoreService scoreStoreService) {
        LOGGER.info("Creating LoginHttpHandler Bean");
        return new LoginHttpHandler(baseHttpHandler, scoreStoreService);
    }

    private RegisterScoreHttpSyncHandler getRegisterScoreHttpSyncHandler(BaseHttpHandler baseHttpHandler,
                                                                         ScoreStoreService scoreStoreService) {
        LOGGER.info("Creating RegisterScoreHttpSyncHandler Bean");
        return new RegisterScoreHttpSyncHandler(baseHttpHandler, scoreStoreService);
    }

    private RegisterScoreHttpAsyncHandler getRegisterScoreHttpAsyncHandler(BaseHttpHandler baseHttpHandler,
                                                                           QueueProcessor queueProcessor) {
        LOGGER.info("Creating RegisterScoreHttpAsyncHandler Bean");
        return new RegisterScoreHttpAsyncHandler(baseHttpHandler, queueProcessor);
    }

    private QueueProcessor getQueueProcessor() {
        LOGGER.info("Creating InMemoryQueueProcessorImpl Bean");
        return new InMemoryQueueProcessorImpl();
    }

    private FetchHighScoreHttpHandler getFetchHighScoreHttpHandler(BaseHttpHandler baseHttpHandler,
                                                                   ScoreStoreService scoreStoreService) {
        LOGGER.info("Creating FetchHighScoreHttpHandler Bean");
        return new FetchHighScoreHttpHandler(baseHttpHandler, scoreStoreService);
    }

    private HttpServer instantiateHttpServer(int port) {
        LOGGER.info("Creating HttpServer Bean");
        HttpServer httpServer = null;
        BasicHttpServerProvider provider = BasicHttpServerProvider.newInstance();
        try {
            httpServer = provider.createHttpServer(new InetSocketAddress(port));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"IOException encountered in instantiateHttpServer Bean : " + e);
        }
        return httpServer;
    }

    public void closeServer() throws InterruptedException {
        if (Objects.nonNull(queueConsumer)) {
            queueConsumer.stop();
        }
        server.stop(0);
    }
}

package com.king.services.scorestore.processor.impl;

import com.king.services.scorestore.exception.ServiceException;
import com.king.services.scorestore.model.Constants;
import com.king.services.scorestore.model.UserScoreDetails;
import com.king.services.scorestore.processor.QueueConsumer;
import com.king.services.scorestore.processor.QueueProcessor;
import com.king.services.scorestore.service.ScoreStoreService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Queue Consumer Implementation for Async Processing for Registering Scroes for a given Level and SessionID
 * Uses Fixed Thread Pool From Executor Service to processing in separate Thread.
 */
public class RegisterScoreQueueConsumerImpl implements QueueConsumer {

    private static final Logger LOGGER = Logger.getLogger(RegisterScoreQueueConsumerImpl.class.getName());
    private final QueueProcessor<UserScoreDetails> queueProcessor;
    private final ExecutorService executorService;
    private final ScoreStoreService scoreStoreService;

    public RegisterScoreQueueConsumerImpl(QueueProcessor queueProcessor, ScoreStoreService scoreStoreService) {
        this.queueProcessor = queueProcessor;
        this.scoreStoreService = scoreStoreService;
        this.executorService = Executors.newFixedThreadPool(Constants.QUEUE_CONSUMER_THREAD_POOL_SIZE);
    }

    @Override
    public void start() {
        executorService.submit(this::run);
    }

    @Override
    public void stop() throws InterruptedException {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
            LOGGER.info("Stopping Consumer Thread...");
            executorService.awaitTermination(20, TimeUnit.MILLISECONDS);
            LOGGER.info("Stopped Consumer Thread...");
        }
    }

    private void run() {
        UserScoreDetails userScore;
        try {
            while (true) {
                userScore = queueProcessor.take();
                int retryCounter = 0;
                if (userScore != null) {
                    try {

                        scoreStoreService.registerScore(userScore.getSessionID(),
                                userScore.getLevel(), userScore.getScore());
                    } catch (ServiceException e) {
                        LOGGER.log(Level.SEVERE, "ServiceException Encountered in RegisterScoreQueueConsumerImpl run .. Retrying: ", e);
                        while (retryCounter < Constants.REGISTER_SCORE_RETRY_COUNT) {
                            try {
                                scoreStoreService.registerScore(userScore.getSessionID(),
                                        userScore.getLevel(), userScore.getScore());
                            } catch (ServiceException ex) {
                                retryCounter++;
                                LOGGER.log(Level.SEVERE, "ServiceException Encountered in RegisterScoreQueueConsumerImpl run .. on retry " +
                                        ":  " + retryCounter + " of " + Constants.REGISTER_SCORE_RETRY_COUNT + " error: " + ex);
                                if (retryCounter >= Constants.REGISTER_SCORE_RETRY_COUNT) {
                                    LOGGER.log(Level.SEVERE, "ServiceException Encountered in RegisterScoreQueueConsumerImpl run .. Max retries " +
                                            "exceeded error: " + ex);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "InterruptedException Encountered in RegisterScoreQueueConsumerImpl run: ", e);
            Thread.currentThread().interrupt();
        }
    }
}

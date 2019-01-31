package com.king.services.scorestore.processor.impl;

import com.king.services.scorestore.dao.ScoreStoreDAO;
import com.king.services.scorestore.dao.impl.InMemoryScoreStoreDAOImpl;
import com.king.services.scorestore.exception.DAOException;
import com.king.services.scorestore.model.User;
import com.king.services.scorestore.processor.QueueConsumer;
import com.king.services.scorestore.processor.QueueProcessor;
import com.king.services.scorestore.service.ScoreStoreService;
import com.king.services.scorestore.service.impl.ScoreStoreServiceImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

public class RegisterScoreQueueConsumerImplTest {

    private static QueueProcessor<User> queueProcessor;
    private static QueueConsumer queueConsumer;
    private static ScoreStoreService scoreStoreService;
    private static ScoreStoreDAO scoreStoreDAO;

    @BeforeClass
    public static void setup() {
        queueProcessor = new InMemoryQueueProcessorImpl();
        scoreStoreDAO = new InMemoryScoreStoreDAOImpl();
        scoreStoreService = new ScoreStoreServiceImpl(scoreStoreDAO);
        queueConsumer = new RegisterScoreQueueConsumerImpl(queueProcessor, scoreStoreService);
        queueConsumer.start();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException {
        queueConsumer.stop();
    }

    @Test
    public void testQueueMessageFlow() throws InterruptedException, DAOException {
        scoreStoreDAO.saveLoginInfo(1234, System.currentTimeMillis());
        scoreStoreDAO.saveSessionInfo("POIUYT", 1234);
        queueProcessor.put(new User(2344, 0, "POIUYT"));
        Thread.sleep(10);
        final Set<User> usersForLevel = scoreStoreDAO.getUsersForLevel(0);
        Assert.assertNotNull(usersForLevel);
        Assert.assertTrue(usersForLevel.size() > 0);
        usersForLevel.stream().forEach((u) -> {
            Assert.assertEquals(1234, u.getLoginId());
            Assert.assertEquals(2344, u.getScore());
            Assert.assertEquals(0, u.getLevel());
        });
    }
}
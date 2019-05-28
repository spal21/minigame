package com.king.services.scorestore.processor.impl;

import com.king.services.scorestore.cache.ObjectCache;
import com.king.services.scorestore.cache.impl.UserScoreCache;
import com.king.services.scorestore.dao.ScoreStoreDAO;
import com.king.services.scorestore.dao.impl.InMemoryScoreStoreDAOImpl;
import com.king.services.scorestore.exception.DAOException;
import com.king.services.scorestore.model.UserScore;
import com.king.services.scorestore.model.UserScoreDetails;
import com.king.services.scorestore.model.UserSession;
import com.king.services.scorestore.processor.QueueConsumer;
import com.king.services.scorestore.processor.QueueProcessor;
import com.king.services.scorestore.service.ScoreStoreService;
import com.king.services.scorestore.service.impl.ScoreStoreServiceImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class RegisterScoreQueueConsumerImplTest {

    private static QueueProcessor<UserScoreDetails> queueProcessor;
    private static QueueConsumer queueConsumer;
    private static ScoreStoreService scoreStoreService;
    private static ScoreStoreDAO scoreStoreDAO;
    private static ObjectCache<Integer, List<UserScore>> userScoreCache;

    @BeforeClass
    public static void setup() {
        queueProcessor = new InMemoryQueueProcessorImpl();
        scoreStoreDAO = new InMemoryScoreStoreDAOImpl();
        userScoreCache = new UserScoreCache();
        scoreStoreService = new ScoreStoreServiceImpl(scoreStoreDAO, userScoreCache);
        queueConsumer = new RegisterScoreQueueConsumerImpl(queueProcessor, scoreStoreService);
        queueConsumer.start();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException {
        queueConsumer.stop();
    }

    @Test
    public void testQueueMessageFlow() throws InterruptedException, DAOException {
        final UserSession userSession = new UserSession(1234, "POIUYT", System.currentTimeMillis());
        scoreStoreDAO.persistUserSession(userSession);
        queueProcessor.put(new UserScoreDetails("POIUYT", 0, 2344, System.currentTimeMillis()));
        Thread.sleep(100);
        final Set<UserScore> usersForLevel = scoreStoreDAO.getUserScoresForLevel(1234,0);
        Assert.assertNotNull(usersForLevel);
        Assert.assertTrue(usersForLevel.size() > 0);
        usersForLevel.stream().forEach((u) -> {
            Assert.assertEquals(1234, u.getLoginID());
            Assert.assertEquals(2344, u.getScore());
            Assert.assertEquals(0, u.getLevel());
        });
    }
}
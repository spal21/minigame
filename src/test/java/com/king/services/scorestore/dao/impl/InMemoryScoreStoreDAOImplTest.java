package com.king.services.scorestore.dao.impl;

import com.king.services.scorestore.dao.ScoreStoreDAO;
import com.king.services.scorestore.exception.DAOException;
import com.king.services.scorestore.model.UserScore;
import com.king.services.scorestore.model.UserSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class InMemoryScoreStoreDAOImplTest {

    private static ScoreStoreDAO scoreStoreDAO;
    private static Set<UserScore> userSet;
    private static long time;

    @BeforeClass
    public static void setup() throws DAOException {
        time = System.currentTimeMillis();
        scoreStoreDAO = new InMemoryScoreStoreDAOImpl();

        UserSession us1 = new UserSession(1234, "ABCDEF", 12344456);
        UserSession us2 = new UserSession(1235, "SDFSDF", 12344456);
        scoreStoreDAO.persistUserSession(us1);
        scoreStoreDAO.persistUserSession(us2);
        scoreStoreDAO.persistUserScore(new UserScore(1234, 1, 4322, 1L));
        scoreStoreDAO.persistUserScore(new UserScore(1235, 1, 4323, 1L));
        userSet = new HashSet<>();
    }

    @Test
    public void getUserScoresForLevel() throws DAOException {
        UserSession us1 = new UserSession(1236, "HJBVJ", System.currentTimeMillis());
        scoreStoreDAO.persistUserScore(new UserScore(1236, 1, 4321, System.currentTimeMillis()));
        scoreStoreDAO.persistUserScore(new UserScore(1236, 1, 4322, System.currentTimeMillis()));
        Set<UserScore> users = scoreStoreDAO.getUserScoresForLevel(1236,1);
        Assert.assertNotNull(users);
        Assert.assertTrue(users.size() == 2);
    }

    @Test
    public void getUser() throws DAOException {
        final Optional<UserSession> userOptional = scoreStoreDAO.getUserSession(1234);
        Assert.assertNotNull(userOptional);
        Assert.assertTrue(userOptional.isPresent());
        Assert.assertNotNull(userOptional.get());
        Assert.assertNotNull(userOptional.get().getLoginId());
        Assert.assertNotNull(userOptional.get().getSessionID());
        Assert.assertEquals(1234, userOptional.get().getLoginId());
        Assert.assertEquals("ABCDEF", userOptional.get().getSessionID());
    }

    @Test
    public void getUserBySessionID() throws DAOException {
        final Optional<UserSession> userOptional = scoreStoreDAO.getUserSession("ABCDEF");
        Assert.assertNotNull(userOptional);
        Assert.assertTrue(userOptional.isPresent());
        Assert.assertNotNull(userOptional.get());
        Assert.assertNotNull(userOptional.get().getLoginId());
        Assert.assertNotNull(userOptional.get().getSessionID());
        Assert.assertEquals(1234, userOptional.get().getLoginId());
        Assert.assertEquals("ABCDEF", userOptional.get().getSessionID());
    }

    @Test
    public void removeSessionID() throws DAOException {
        scoreStoreDAO.removeUserSession("ABCDEF");
        final Optional<UserSession> optionalUserSession = scoreStoreDAO.getUserSession(1234);
        Assert.assertNotNull(optionalUserSession);
        Assert.assertFalse(optionalUserSession.isPresent());
        UserSession us1 = new UserSession(1234, "ABCDEF", 12344456);
        scoreStoreDAO.persistUserSession(us1);
        scoreStoreDAO.persistUserScore(new UserScore(1234, 1, 4322, 1L));
    }

    @Test
    public void userExistsForLevel() throws DAOException {
        Set<UserScore> userExistsForLevel = scoreStoreDAO.getUserScoresForLevel(1234, 1);
        Assert.assertTrue(userExistsForLevel.size()>0);
        userExistsForLevel = scoreStoreDAO.getUserScoresForLevel(2, 123);
        Assert.assertFalse(userExistsForLevel.size()>0);
    }

    @Test
    public void getTopNUsersForLevelDescOrder() throws DAOException {
        final Set<UserScore> nUsersForLevel = scoreStoreDAO.getTopNUserScoresForLevel(1, 15);
        Assert.assertNotNull(nUsersForLevel);
        Assert.assertTrue(nUsersForLevel.size() > 0);
    }

    @Test
    public void getTopNUsersForLevelDescOrderSingleRecord() throws DAOException {
        final Set<UserScore> nUsersForLevel = scoreStoreDAO.getTopNUserScoresForLevel(1, 15);
        Assert.assertNotNull(nUsersForLevel);
        Assert.assertTrue(nUsersForLevel.size() > 0);
    }
}

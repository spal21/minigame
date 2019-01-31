package com.king.services.scorestore.dao.impl;

import com.king.services.scorestore.exception.DAOException;
import com.king.services.scorestore.model.User;
import com.king.services.scorestore.dao.ScoreStoreDAO;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class InMemoryScoreStoreDAOImplTest {

    private static ScoreStoreDAO scoreStoreDAO;
    private static Set<User> userSet;
    private static long time;

    @BeforeClass
    public static void setup() throws DAOException {
        time = System.currentTimeMillis();
        scoreStoreDAO = new InMemoryScoreStoreDAOImpl();
        scoreStoreDAO.saveSessionInfo("ABCDEF", 1);
        scoreStoreDAO.saveLoginInfo(1, time);
        scoreStoreDAO.saveUserInfoForLevel("ADJHD",1, 1234, 4322);
        scoreStoreDAO.saveUserInfoForLevel("SDFSDF",1, 1234, 4321);
        scoreStoreDAO.saveUserInfoForLevel("DSFDS",1, 1235, 4323);
        userSet = new HashSet<>();
        User u1 = new User(1234, 4321, 1, "AVNHJ");
        userSet.add(u1);
    }

    @Test
    public void getUsersForLevel() throws DAOException {
        scoreStoreDAO.saveUserInfoForLevel("HJBVJ",1, 1234, 4321);
        Set<User> users = scoreStoreDAO.getUsersForLevel(1);
        Assert.assertNotNull(users);
        Assert.assertTrue(users.size() > 0);
    }

    @Test
    public void getTopNUsersForLevelDescOrder() throws DAOException {
        final Optional<String> nUsersForLevel = scoreStoreDAO.getTopNScoresForLevelDesc(1, 10);
        Assert.assertNotNull(nUsersForLevel);
        Assert.assertTrue(nUsersForLevel.isPresent());
        Assert.assertEquals("1235=4323,1234=4322", nUsersForLevel.get());
    }

    @Test
    public void getTopNUsersForLevelDescOrderSingleRecord() throws DAOException {
        final Optional<String> nUsersForLevel = scoreStoreDAO.getTopNScoresForLevelDesc(1, 1);
        Assert.assertNotNull(nUsersForLevel);
        Assert.assertTrue(nUsersForLevel.isPresent());
        Assert.assertEquals("1235=4323", nUsersForLevel.get());
    }

    @Test
    public void getSessionID() throws DAOException {
        final Optional<String> sessionID = scoreStoreDAO.getSessionID(1);
        Assert.assertNotNull(sessionID);
        Assert.assertTrue(sessionID.isPresent());
        Assert.assertEquals("ABCDEF", sessionID.get());
    }

    @Test
    public void getLoginID() throws DAOException {
        final Optional<Integer> loginID = scoreStoreDAO.getLoginID("ABCDEF");
        Assert.assertNotNull(loginID);
        Assert.assertTrue(loginID.isPresent());
        Assert.assertEquals(1L, loginID.get().longValue());
    }

    @Test
    public void getLoginInfo() throws DAOException {
        final Optional<Long> loginTime = scoreStoreDAO.getLoginInfo(1);
        Assert.assertNotNull(loginTime);
        Assert.assertTrue(loginTime.isPresent());
        Assert.assertEquals(time, loginTime.get().longValue());
    }

    @Test
    public void removeSessionID() throws DAOException {
        scoreStoreDAO.removeSessionID("ABCDEF");
        final Optional<String> sessionID = scoreStoreDAO.getSessionID(1);
        Assert.assertNotNull(sessionID);
        Assert.assertFalse(sessionID.isPresent());
        scoreStoreDAO.saveSessionInfo("ABCDEF", 1);
    }

    @Test
    public void userExistsForLevel() throws DAOException {

        boolean userExistsForLevel = scoreStoreDAO.userExistsForLevel(1, 1234);
        Assert.assertTrue(userExistsForLevel);
        userExistsForLevel = scoreStoreDAO.userExistsForLevel(1, 123);
        Assert.assertFalse(userExistsForLevel);
    }

}

package com.king.services.scorestore.service.impl;

import com.king.services.scorestore.cache.ObjectCache;
import com.king.services.scorestore.dao.ScoreStoreDAO;
import com.king.services.scorestore.exception.DAOException;
import com.king.services.scorestore.exception.ServiceException;
import com.king.services.scorestore.model.UserScore;
import com.king.services.scorestore.model.UserSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class ScoreStoreServiceImplTest {

    private static Set<UserScore> userScoreSet;
    private static List<UserScore> userScoreList;
    private static UserSession user1 = new UserSession(1234, "ABCDEFG", System.currentTimeMillis());
    private static UserSession user2 = new UserSession(1235, "ANCDTY", System.currentTimeMillis());
    private static UserScore u1 = new UserScore(1234, 1, 4321, 1);
    private static UserScore u2 = new UserScore(1235, 1, 4322, 1);
    @Mock
    private ScoreStoreDAO scoreStoreDAO;
    @Mock
    private ObjectCache<Integer, List<UserScore>> userScoreCache;
    @InjectMocks
    private ScoreStoreServiceImpl scoreStoreService;

    @BeforeClass
    public static void setUp() {

        userScoreSet = new HashSet<>();
        userScoreSet.add(u1);
        userScoreSet.add(u2);
        userScoreList = new ArrayList<>();
        userScoreList.add(u2);
        userScoreList.add(u1);
    }

    @Test
    public void getUsersForLevel() throws ServiceException, DAOException {
        Mockito.when(scoreStoreDAO.getUserScoresForLevel(Mockito.anyInt(), Mockito.anyInt())).thenReturn(userScoreSet);
//        Mockito.when(userScoreCache.get(Mockito.anyInt())).thenReturn(userScoreSet);
        final Set<UserScore> usersForLevel = scoreStoreService.getUserScoresForLevel(1234,1);
        Assert.assertNotNull(usersForLevel);
        Assert.assertTrue(usersForLevel.size() > 0);
        usersForLevel.stream().forEach((u) -> {
            if (u.getLoginID() == 1234) {
                Assert.assertEquals(1234, u.getLoginID());
                Assert.assertEquals(4321, u.getScore());
                Assert.assertEquals(1, u.getLevel());
            } else if (u.getLoginID() == 1235) {
                Assert.assertEquals(1235, u.getLoginID());
                Assert.assertEquals(4322, u.getScore());
                Assert.assertEquals(1, u.getLevel());
            }

        });
    }

    @Test(expected = ServiceException.class)
    public void getUsersForLevelThrowsException() throws ServiceException, DAOException {
        Mockito.when(scoreStoreDAO.getUserScoresForLevel(Mockito.anyInt(), Mockito.anyInt())).thenThrow(DAOException.class);
//        Mockito.when(userScoreCache.get(Mockito.anyInt())).thenThrow(DAOException.class);
        scoreStoreService.getUserScoresForLevel(1234,1);
    }

    @Test
    public void generateSessionID() throws ServiceException, DAOException {
        Mockito.when(scoreStoreDAO.persistUserSession(Mockito.any(UserSession.class))).thenReturn(Optional.of(user1));

        final Optional<String> sessionID = scoreStoreService.generateSessionID(321);
        Assert.assertNotNull(sessionID);
        Assert.assertNotNull(sessionID.get());
        Assert.assertEquals(7, sessionID.get().length());
    }

    @Test(expected = ServiceException.class)
    public void generateSessionIDThrowsException() throws ServiceException, DAOException {
        Mockito.when(scoreStoreDAO.getUserSession(Mockito.anyString())).thenThrow(DAOException.class);
        scoreStoreService.generateSessionID(321);
    }

    @Test
    public void registerScoreInvalidSessionID() throws DAOException {
        Mockito.when(scoreStoreDAO.getUserSession(Mockito.anyString())).thenReturn(Optional.of(user1));
        try {
            scoreStoreService.registerScore("ABCDEFG", 0, 100);
        } catch (ServiceException e) {
            Assert.assertEquals("Invalid SessionKey ", e.getMessage());
        }
    }

    @Test
    public void registerScoreLevelZero() throws DAOException {
        Mockito.when(scoreStoreDAO.getUserSession(Mockito.anyString())).thenReturn(Optional.of(user1));
        try {
            scoreStoreService.registerScore("ABCDEFG", 0, 100);
        } catch (ServiceException e) {
            Assert.assertEquals("Invalid Level info. Level Must be provided and greater than 0.", e.getMessage());
        }
    }

    @Test
    public void registerScoreLevelsSkipped() throws DAOException {
        Mockito.when(scoreStoreDAO.getUserSession(Mockito.anyString())).thenReturn(Optional.of(user1));
        Mockito.when(scoreStoreDAO.getUserScoresForLevel(Mockito.anyInt(), Mockito.anyInt())).thenReturn(Collections.emptySet());
        try {
            scoreStoreService.registerScore("ABCDEFG", 2, 100);
        } catch (ServiceException e) {
            Assert.assertEquals("User cannot skip levels. SessionID : ABCDEFG", e.getMessage());
        }
    }

    @Test(expected = ServiceException.class)
    public void registerScoreThrowsDAOException() throws DAOException, ServiceException {
        scoreStoreService.registerScore("ABCDEFG", 2, 100);
    }

    @Test(expected = ServiceException.class)
    public void getTopNUsersForLevelNoUserThrowsException() throws ServiceException, DAOException {
        Mockito.when(userScoreCache.get(Mockito.anyInt())).thenThrow(DAOException.class);
        // Mockito.when(scoreStoreDAO.getUserScoresForLevel(Mockito.anyInt())).thenThrow(DAOException.class);
        scoreStoreService.getTopNScoresForLevelDesc(1);
    }

    @Test
    public void getTopNUsersForLevel() throws DAOException, ServiceException {
        String output = "1235=4322,1234=4321";

        // Mockito.when(scoreStoreDAO.getUserScoresForLevel(Mockito.anyInt())).thenReturn(userScoreSet);
        Mockito.when(userScoreCache.get(Mockito.anyInt())).thenReturn(userScoreList);
        final String topNUsersForLevel = scoreStoreService.getTopNScoresForLevelDesc(1);
        Assert.assertNotNull(topNUsersForLevel);
        Assert.assertTrue(topNUsersForLevel.length() > 0);
        Assert.assertEquals(output, topNUsersForLevel);
    }

    @Test
    public void getTopNUsersForLevelNoUser() throws ServiceException, DAOException {
//        Mockito.when(scoreStoreDAO.getUserScoresForLevel(Mockito.anyInt())).thenReturn(Collections.emptySet());
        //       Mockito.when(userScoreCache.get(Mockito.anyInt())).thenReturn(Collections.emptySet());
        final String topNUsersForLevel = scoreStoreService.getTopNScoresForLevelDesc(1);
        Assert.assertNotNull(topNUsersForLevel);
        Assert.assertFalse(topNUsersForLevel.length() > 0);
    }
}
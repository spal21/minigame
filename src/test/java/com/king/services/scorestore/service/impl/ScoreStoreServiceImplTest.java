package com.king.services.scorestore.service.impl;

import com.king.services.scorestore.dao.ScoreStoreDAO;
import com.king.services.scorestore.exception.DAOException;
import com.king.services.scorestore.model.User;
import com.king.services.scorestore.exception.ServiceException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class ScoreStoreServiceImplTest {

    private static Set<User> userSet;
    @Mock
    private ScoreStoreDAO scoreStoreDAO;
    @InjectMocks
    private ScoreStoreServiceImpl scoreStoreService;

    @BeforeClass
    public static void setUp() {
        User user1 = new User(1234, 4321, 1, "ANCDEF");
        User user2 = new User(1235, 4322, 1, "ANCDTY");
        userSet = new HashSet<>();
        userSet.add(user1);
        userSet.add(user2);
    }

    @Test
    public void getUsersForLevel() throws ServiceException, DAOException {
        Mockito.when(scoreStoreDAO.getUsersForLevel(Mockito.anyInt())).thenReturn(userSet);
        final Set<User> usersForLevel = scoreStoreService.getUsersForLevel(1);
        Assert.assertNotNull(usersForLevel);
        Assert.assertTrue(usersForLevel.size() > 0);
        usersForLevel.stream().forEach((u) -> {
            if (u.getLoginId() == 1234) {
                Assert.assertEquals(1234, u.getLoginId());
                Assert.assertEquals(4321, u.getScore());
                Assert.assertEquals(1, u.getLevel());
            } else if (u.getLoginId() == 1235) {
                Assert.assertEquals(1235, u.getLoginId());
                Assert.assertEquals(4322, u.getScore());
                Assert.assertEquals(1, u.getLevel());
            }

        });
    }

    @Test(expected = ServiceException.class)
    public void getUsersForLevelThrowsException() throws ServiceException, DAOException {
        Mockito.when(scoreStoreDAO.getUsersForLevel(Mockito.anyInt())).thenThrow(DAOException.class);
        scoreStoreService.getUsersForLevel(1);
    }

    @Test
    public void getTopNUsersForLevel() throws DAOException, ServiceException {
        String output = "1234=4321";
        Mockito.when(scoreStoreDAO.getTopNScoresForLevelDesc(Mockito.anyInt(), Mockito.anyInt())).thenReturn(Optional.of(output));
        final String topNUsersForLevel = scoreStoreService.getTopNScoresForLevelDesc(1);
        Assert.assertNotNull(topNUsersForLevel);
        Assert.assertTrue(topNUsersForLevel.length() > 0);
        Assert.assertEquals(output, topNUsersForLevel);
    }

    @Test
    public void getTopNUsersForLevelNoUser() throws ServiceException, DAOException {
        Mockito.when(scoreStoreDAO.getTopNScoresForLevelDesc(Mockito.anyInt(), Mockito.anyInt())).thenReturn(Optional.empty());
        final String topNUsersForLevel = scoreStoreService.getTopNScoresForLevelDesc(1);
        Assert.assertNotNull(topNUsersForLevel);
        Assert.assertFalse(topNUsersForLevel.length() > 0);
    }

    @Test(expected = ServiceException.class)
    public void getTopNUsersForLevelNoUserThrowsException() throws ServiceException, DAOException {
        Mockito.when(scoreStoreDAO.getTopNScoresForLevelDesc(Mockito.anyInt(), Mockito.anyInt())).thenThrow(DAOException.class);
        scoreStoreService.getTopNScoresForLevelDesc(1);
    }

    @Test
    public void generateSessionID() throws ServiceException {
        final Optional<String> sessionID = scoreStoreService.generateSessionID(321);
        Assert.assertNotNull(sessionID);
        Assert.assertNotNull(sessionID.get());
        Assert.assertEquals(7, sessionID.get().length());
    }

    @Test(expected = ServiceException.class)
    public void generateSessionIDThrowsException() throws ServiceException, DAOException {
        Mockito.when(scoreStoreDAO.getLoginID(Mockito.anyString())).thenThrow(DAOException.class);
        scoreStoreService.generateSessionID(321);
    }

    @Test
    public void registerScoreInvalidSessionID() throws DAOException {
        Mockito.when(scoreStoreDAO.getLoginID(Mockito.anyString())).thenReturn(Optional.empty());
        try {
            scoreStoreService.registerScore("ABCDEF", 1, 100);
        } catch (ServiceException e) {
            Assert.assertEquals("Invalid SessionKey : ABCDEF", e.getMessage());
        }
    }

    @Test
    public void registerScoreSessionIDExpired() throws DAOException {
        Mockito.when(scoreStoreDAO.getLoginID(Mockito.anyString())).thenReturn(Optional.of(1234));
        Mockito.when(scoreStoreDAO.getLoginInfo(Mockito.anyInt())).thenReturn(Optional.of(0L));
        try {
            scoreStoreService.registerScore("ABCDEF", 1, 100);
        } catch (ServiceException e) {
            Assert.assertEquals("Session Expired.", e.getMessage());
        }
    }

    @Test
    public void registerScoreLevelZero() throws DAOException {
        Mockito.when(scoreStoreDAO.getLoginID(Mockito.anyString())).thenReturn(Optional.of(1234));
        Mockito.when(scoreStoreDAO.getLoginInfo(Mockito.anyInt())).thenReturn(Optional.of(System.currentTimeMillis()));
        try {
            scoreStoreService.registerScore("ABCDEF", 0, 100);
        } catch (ServiceException e) {
            Assert.assertEquals("Invalid Level info. Level Must be provided and greater than 0.", e.getMessage());
        }
    }

    @Test
    public void registerScoreLevelsSkipped() throws DAOException {
        Mockito.when(scoreStoreDAO.getLoginID(Mockito.anyString())).thenReturn(Optional.of(1234));
        Mockito.when(scoreStoreDAO.getLoginInfo(Mockito.anyInt())).thenReturn(Optional.of(System.currentTimeMillis()));
        Mockito.when(scoreStoreDAO.userExistsForLevel(Mockito.anyInt(), Mockito.anyInt())).thenReturn(false);
        try {
            scoreStoreService.registerScore("ABCDEF", 2, 100);
        } catch (ServiceException e) {
            Assert.assertEquals("User cannot skip levels. SessionID : ABCDEF", e.getMessage());
        }
    }

    @Test(expected = ServiceException.class)
    public void registerScoreThrowsDAOException() throws DAOException, ServiceException {
        Mockito.when(scoreStoreDAO.getLoginID(Mockito.anyString())).thenThrow(DAOException.class);
        scoreStoreService.registerScore("ABCDEF", 2, 100);
    }
}
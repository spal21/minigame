package com.king.services.scorestore.service.impl;

import com.king.services.scorestore.cache.ObjectCache;
import com.king.services.scorestore.dao.ScoreStoreDAO;
import com.king.services.scorestore.exception.DAOException;
import com.king.services.scorestore.exception.ServiceException;
import com.king.services.scorestore.model.Constants;
import com.king.services.scorestore.model.UserScore;
import com.king.services.scorestore.model.UserSession;
import com.king.services.scorestore.service.ScoreStoreService;
import com.king.services.scorestore.util.Utils;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of the Service Layer. Fetching Data is delegated to the DAO layer Implementation.
 * Uses Reentrant ReadWriteLock for Transaction control.
 */
public class ScoreStoreServiceImpl implements ScoreStoreService {

    private static final Logger LOGGER = Logger.getLogger(ScoreStoreServiceImpl.class.getName());
    private final ScoreStoreDAO scoreStoreDAO;
    private final ReadWriteLock userSessionLock;
    private final Lock userSessionWriteLock;
    private final Lock userSessionReadLock;
    private final ReadWriteLock userScoreLock;
    private final Lock userScoreReadLock;
    private final Lock userScoreWriteLock;
    private final ObjectCache<Integer, List<UserScore>> userScoreCache;


    public ScoreStoreServiceImpl(ScoreStoreDAO scoreStoreDAO, ObjectCache<Integer, List<UserScore>> userScoreCache) {
        this.scoreStoreDAO = scoreStoreDAO;
        userSessionLock = new ReentrantReadWriteLock();
        userSessionReadLock = userSessionLock.readLock();
        userSessionWriteLock = userSessionLock.writeLock();
        userScoreLock = new ReentrantReadWriteLock();
        userScoreReadLock = userScoreLock.readLock();
        userScoreWriteLock = userScoreLock.writeLock();
        this.userScoreCache = userScoreCache;

    }

    @Override
    public Set<UserScore> getUserScoresForLevel(int loginID, int level) throws ServiceException {
        try {
            userScoreReadLock.lock();
            Set<UserScore> scores = scoreStoreDAO.getUserScoresForLevel(loginID, level);//
            return Objects.isNull(scores) ? Collections.emptySet() : scores;
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "DAOException encountered in getUserScoresForLevel for level : " + level + " : ", e);
            throw new ServiceException("DAOException encountered in getUserScoresForLevel : ", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in getUserScoresForLevel for level : " + level + " : ", e);
            throw new ServiceException("Exception encountered in getUserScoresForLevel : ", e);
        } finally {
            userScoreReadLock.unlock();
        }
    }

    @Override
    public String getTopNScoresForLevelDesc(int level) throws ServiceException {
        try {
            final List<UserScore> scores = userScoreCache.get(level);
            return Objects.isNull(scores) ? "" :
                    scores.stream().map(e -> e.getLoginID() + "=" + e.getScore()).collect(Collectors.joining(","));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in getTopNScoresForLevelDesc for level : " + level + " : ", e);
            throw new ServiceException("Exception encountered in getTopNScoresForLevelDesc : ", e);
        }
    }

    @Override
    public Optional<String> generateSessionID(int loginID) throws ServiceException {
        try {
            userSessionWriteLock.lock();
            String randString = Utils.generateRandomString();
            String sessionID = randString;
            while (scoreStoreDAO.getUserSession(sessionID).isPresent()) {
                sessionID = Utils.generateRandomString();
            }
            Optional<UserSession> existingUserOptional = scoreStoreDAO.getUserSession(loginID);
            if (existingUserOptional.isPresent()) {
                // We dont allow Multiple sessions per LoginID
                scoreStoreDAO.removeUserSession(existingUserOptional.get().getSessionID());
            }
            Optional<UserSession> userSessionOptional = scoreStoreDAO.persistUserSession(new UserSession(loginID,
                    sessionID, System.currentTimeMillis()));
            if(userSessionOptional.isPresent()){
                return Optional.of(userSessionOptional.get().getSessionID());
            }else{
             return Optional.empty();
            }
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "DAOException encountered in generateSessionID : ", e);
            throw new ServiceException("DAOException encountered in generateSessionID : ", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in generateSessionID : ", e);
            throw new ServiceException("Exception encountered in generateSessionID : ", e);
        } finally {
            userSessionWriteLock.unlock();
        }
    }

    @Override
    public Optional<UserSession> getUserSession(String sessionID) throws ServiceException {
        try {
            userSessionReadLock.lock();
            return scoreStoreDAO.getUserSession(sessionID);
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "DAOException encountered in getUserSession for sessionID : " + sessionID + " : ", e);
            throw new ServiceException("DAOException encountered in getUserSession : ", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in getUserSession for sessionID : " + sessionID + " : ", e);
            throw new ServiceException("Exception encountered in getUserSession : ", e);
        } finally {
            userSessionReadLock.unlock();
        }
    }

    @Override
    public void registerScore(String sessionID, int level, int score) throws ServiceException {
        boolean cacheRefreshFlag = false;
        try {
            userScoreWriteLock.lock();
            Optional<UserSession> userSessionOptional = scoreStoreDAO.getUserSession(sessionID);
            if (userSessionOptional.isPresent()) {
                if (userSessionOptional.get().isValid()) {
                    if (level > 0) {
                        if (scoreStoreDAO.getUserScoresForLevel( userSessionOptional.get().getLoginId(),
                                level - 1).size()==0) {
                            throw new ServiceException(Constants.LEVELS_SKIP_ERROR_MESSAGE + sessionID);
                        }
                    }
                    scoreStoreDAO.persistUserScore(
                            new UserScore(userSessionOptional.get().getLoginId(), level, score, System.currentTimeMillis()));
                    cacheRefreshFlag = true;
                } else {
                    throw new ServiceException(Constants.SESSIONKEY_EXPIRED_ERROR_MESSAGE);
                }
            } else {
                throw new ServiceException(Constants.SESSIONKEY_INVALID + sessionID);
            }
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "DAOException encountered in registerScore : ", e);
            throw new ServiceException("DAOException encountered in registerScore : ", e);
        } finally {
            if (cacheRefreshFlag) {
                Set<UserScore> userScore = getTopNScoresForLevlDesc(level);
                userScoreCache.put(level, parseUserScores(userScore));
            }
            userScoreWriteLock.unlock();
        }
    }

    private List<UserScore> parseUserScores(Set<UserScore> scores) {

        return scores.stream().collect(Collectors.toMap(UserScore::getLoginID,
                Function.identity(), (u1, u2) -> (u1.getScore() > u2.getScore() ? u1 : u2))).entrySet().stream().sorted((e1, e2) ->
                ((Integer) e2.getValue().getScore()).compareTo(((Integer) e1.getValue().getScore()))).map(e -> e.getValue()).
                limit(Constants.SCORE_LIST_FETCH_SIZE).
                collect(Collectors.toList());
    }

    private Set<UserScore> getTopNScoresForLevlDesc(int level) throws ServiceException {
        try {
            return scoreStoreDAO.getTopNUserScoresForLevel(level, Constants.SCORE_LIST_FETCH_SIZE);
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "DAOException encountered in getUserScoresForLevel for level : " + level + " : ", e);
            throw new ServiceException("DAOException encountered in getUserScoresForLevel : ", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in getUserScoresForLevel for level : " + level + " : ", e);
            throw new ServiceException("Exception encountered in getUserScoresForLevel : ", e);
        }
    }
}

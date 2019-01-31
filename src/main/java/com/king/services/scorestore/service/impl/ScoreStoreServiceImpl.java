package com.king.services.scorestore.service.impl;

import com.king.services.scorestore.model.User;
import com.king.services.scorestore.dao.ScoreStoreDAO;
import com.king.services.scorestore.exception.DAOException;
import com.king.services.scorestore.exception.ServiceException;
import com.king.services.scorestore.model.Constants;
import com.king.services.scorestore.service.ScoreStoreService;
import com.king.services.scorestore.util.Utils;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the Service Layer. Fetching Data is delegated to the DAO layer Implementation.
 * Uses Reentrant ReadWriteLock for Transaction control.
 */
public class ScoreStoreServiceImpl implements ScoreStoreService {

    private static final Logger LOGGER = Logger.getLogger(ScoreStoreServiceImpl.class.getName());
    private final ScoreStoreDAO scoreStoreDAO;
    private final ReadWriteLock lock;
    private final Lock readLock;
    private final Lock writeLock;

    public ScoreStoreServiceImpl(ScoreStoreDAO scoreStoreDAO) {
        this.scoreStoreDAO = scoreStoreDAO;
        lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    @Override
    public Set<User> getUsersForLevel(int level) throws ServiceException {
        try {
            readLock.lock();
            return scoreStoreDAO.getUsersForLevel(level);
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "DAOException encountered in getUsersForLevel for level : " + level + " : ", e);
            throw new ServiceException("DAOException encountered in getUsersForLevel : ", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in getUsersForLevel for level : " + level + " : ", e);
            throw new ServiceException("Exception encountered in getUsersForLevel : ", e);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getTopNScoresForLevelDesc(int level) throws ServiceException {
        try {
            readLock.lock();
            Optional<String> optionalString;
            if ((optionalString = scoreStoreDAO.getTopNScoresForLevelDesc(level, Constants.SCORE_LIST_FETCH_SIZE)).isPresent()) {
                return optionalString.get();
            } else {
                return "";
            }
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "DAOException encountered in getTopNScoresForLevelDesc for level : " + level + " : ", e);
            throw new ServiceException("DAOException encountered in getTopNScoresForLevelDesc : ", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in getTopNScoresForLevelDesc for level : " + level + " : ", e);
            throw new ServiceException("Exception encountered in getTopNScoresForLevelDesc : ", e);
        } finally {
            readLock.unlock();
        }

    }

    @Override
    public Optional<String> generateSessionID(int loginID) throws ServiceException {
        try {
            writeLock.lock();
            String randString = Utils.generateRandomString();
            String sessionID = randString;
            while (scoreStoreDAO.getLoginID(sessionID).isPresent()) {
                sessionID = Utils.generateRandomString();
            }

            Optional<String> existingSessionIDOptional = scoreStoreDAO.getSessionID(loginID);
            if (existingSessionIDOptional.isPresent()) {
                scoreStoreDAO.removeSessionID(existingSessionIDOptional.get());
            }
            scoreStoreDAO.saveLoginInfo(loginID, System.currentTimeMillis());
            scoreStoreDAO.saveSessionInfo(sessionID, loginID);
            return Optional.of(sessionID);
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "DAOException encountered in generateSessionID : ", e);
            throw new ServiceException("DAOException encountered in generateSessionID : ", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in generateSessionID : ", e);
            throw new ServiceException("Exception encountered in generateSessionID : ", e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void registerScore(String sessionID, int level, int scrore) throws ServiceException {
        try {
            writeLock.lock();
            Optional<Integer> loginIDOptional = scoreStoreDAO.getLoginID(sessionID);
            if (loginIDOptional.isPresent()) {
                Optional<Long> lastLoginTimeOptional = scoreStoreDAO.getLoginInfo(loginIDOptional.get());
                if (System.currentTimeMillis() < (lastLoginTimeOptional.get() + (Constants.SESSION_EXPIRATION_TIME_MINS * 60 * 1000))) {
                    if (level > 0) {
                        if (!scoreStoreDAO.userExistsForLevel(level - 1, loginIDOptional.get())) {
                            throw new ServiceException(Constants.LEVELS_SKIP_ERROR_MESSAGE + sessionID);
                        }
                    }
                    scoreStoreDAO.saveUserInfoForLevel(sessionID, level, loginIDOptional.get(), scrore);
                } else {
                    throw new ServiceException(Constants.SESSIONKEY_EXPIRED_ERROR_MESSAGE);
                }
            } else {
                throw new ServiceException("Invalid SessionKey : " + sessionID);
            }
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "DAOException encountered in registerScore : ", e);
            throw new ServiceException("DAOException encountered in registerScore : ", e);
        } finally {
            writeLock.unlock();
        }
    }
}

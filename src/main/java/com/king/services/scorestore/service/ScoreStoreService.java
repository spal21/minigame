package com.king.services.scorestore.service;

import com.king.services.scorestore.exception.ServiceException;
import com.king.services.scorestore.model.UserScore;
import com.king.services.scorestore.model.UserSession;

import java.util.Optional;
import java.util.Set;

/**
 * Service Layer for Score Store
 */
public interface ScoreStoreService {

    /**
     * Fetches Users for a given Level
     *
     * @param loginID
     * @param level
     * @return Set of User
     * @throws ServiceException
     */
    Set<UserScore> getUserScoresForLevel(int loginID, int level) throws ServiceException;

    /**
     * Fetches Top N Scores For a given Level in comma separated format of LoginID=Score in Desc Order
     *
     * @param level
     * @return
     * @throws ServiceException
     */
    String getTopNScoresForLevelDesc(int level) throws ServiceException;

    /**
     * Generate Session ID for a given Login ID
     *
     * @param loginID
     * @return Optional String Session ID
     * @throws ServiceException
     */
    Optional<String> generateSessionID(int loginID) throws ServiceException;

    /**
     * Get UserSession for a given Login ID
     *
     * @param sessionID
     * @return Optional userSession
     * @throws ServiceException
     */
    Optional<UserSession> getUserSession(String sessionID) throws ServiceException;


    /**
     * Registers Score for a given Session, Level
     *
     * @param sessionID
     * @param level
     * @param score
     * @throws ServiceException
     */
    void registerScore(String sessionID, int level, int score) throws ServiceException;

}

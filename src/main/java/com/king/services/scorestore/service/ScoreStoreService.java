package com.king.services.scorestore.service;

import com.king.services.scorestore.model.User;
import com.king.services.scorestore.exception.ServiceException;

import java.util.Optional;
import java.util.Set;

/**
 * Service Layer for Score Store
 */
public interface ScoreStoreService {

    /**
     * Fetches Users for a given Level
     *
     * @param level
     * @return Set of User
     * @throws ServiceException
     */
    Set<User> getUsersForLevel(int level) throws ServiceException;

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
     * Registers Score for a given Session, Level
     *
     * @param sessionID
     * @param level
     * @param scrore
     * @throws ServiceException
     */
    void registerScore(String sessionID, int level, int scrore) throws ServiceException;

}

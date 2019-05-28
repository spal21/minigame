package com.king.services.scorestore.dao;

import com.king.services.scorestore.exception.DAOException;
import com.king.services.scorestore.model.UserScore;
import com.king.services.scorestore.model.UserSession;

import java.util.Optional;
import java.util.Set;

/**
 * Interface to Handle DAO Layer Functionalities
 */
public interface ScoreStoreDAO {

    /**
     * Fetch Users for given Level and loginID
     *
     * @param loginID - loginID
     * @param level - Game level
     * @return Set of UserScores
     * @throws DAOException
     */
    Set<UserScore> getUserScoresForLevel(int loginID, int level) throws DAOException;

    /**
     * Fetch Top N UserScores for a given Level in desc given Order
     *
     * @param level
     * @param num   number of scores to fetch
     * @return Set of UserScores
     * @throws DAOException
     */
    Set<UserScore> getTopNUserScoresForLevel(int level, int num) throws DAOException;

    /**
     * Fetch UserSession object for a given Login ID
     *
     * @param loginID
     * @return UserSession Object Optional
     * @throws DAOException
     */
    Optional<UserSession> getUserSession(int loginID) throws DAOException;

    /**
     * Fetch UserSession Object for a given SessionID
     *
     * @param sessionID
     * @return UserSession Object Optional
     * @throws DAOException
     */
    Optional<UserSession> getUserSession(String sessionID) throws DAOException;

    /**
     * Persist Session and Login Info
     *
     * @param userSession UserSession Object
     * @return UserSession Object Optional
     * @throws DAOException
     */
    Optional<UserSession> persistUserSession(UserSession userSession) throws DAOException;

    /**
     * Remove SessionID and related UserSession Info
     *
     * @param sessionID
     * @throws DAOException If removal fails
     */
    void removeUserSession(String sessionID) throws DAOException;

     /**
     * Persists User Level Score
     *
     * @param userScore object
     * @throws DAOException If persistence fails
     */
    void persistUserScore(UserScore userScore) throws DAOException;
}

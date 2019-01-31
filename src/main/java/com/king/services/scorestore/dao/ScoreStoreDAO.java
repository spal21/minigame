package com.king.services.scorestore.dao;

import com.king.services.scorestore.exception.DAOException;
import com.king.services.scorestore.model.User;
import java.util.Optional;
import java.util.Set;

/**
 * Interface to Handle DAO Layer Functionalities
 *
 */
public interface ScoreStoreDAO {

    /**
     * Fetch Users for given Level
     *
     * @param level - Game level
     * @return Set of Users
     * @throws DAOException
     */
    Set<User> getUsersForLevel(int level) throws DAOException;

    /**
     * Fetch Top N scores for a given Level in decreasing Order
     *
     * @param level - Game level
     * @param num - Number of scores to fetch
     * @return Optional String in LoginID=Score format separated by comma
     * @throws DAOException
     */
    Optional<String> getTopNScoresForLevelDesc(int level, int num) throws DAOException;

    /**
     * Fetch SessionID for a given Login ID
     *
     * @param loginID
     * @return SessionID
     * @throws DAOException
     */
    Optional<String> getSessionID(int loginID) throws DAOException;

    /**
     * Fetch LoginID for a given SessionID
     *
     * @param sessionID
     * @return LoginID
     * @throws DAOException
     */
    Optional<Integer> getLoginID(String sessionID) throws DAOException;

    /**
     * Persist Session and Login Info
     *
     * @param sessionID
     * @param loginID
     * @throws DAOException - If persistence fails
     */
    void saveSessionInfo(String sessionID, int loginID) throws DAOException;

    /**
     * Persist Login ID and TIme Info
     *
     * @param loginID
     * @param time
     * @throws DAOException  If persistence fails
     */
    void saveLoginInfo(int loginID, long time) throws DAOException;

    /**
     * Fetch LoginID creationTime
     *
     * @param loginID
     * @return CreationTime in Long format
     * @throws DAOException
     */
    Optional<Long> getLoginInfo(int loginID) throws DAOException;

    /**
     * Remove SessionID and related LoginID Info
     *
     * @param sessionID
     * @throws DAOException If removal fails
     */
    void removeSessionID(String sessionID) throws DAOException;

    /**
     * Check if User Exists for a given Level
     *
     * @param level
     * @param loginID
     * @return true if User Record exists for a give level
     * @throws DAOException
     */
    boolean userExistsForLevel(int level, int loginID) throws DAOException;

    /**
     * Persists User Level Info
     *
     * @param sessionID
     * @param level
     * @param loginID
     * @param score
     * @throws DAOException If persistence fails
     */
    void saveUserInfoForLevel(String sessionID, int level, int loginID, int score) throws DAOException;
}

package com.king.services.scorestore.dao.impl;

import com.king.services.scorestore.dao.ScoreStoreDAO;
import com.king.services.scorestore.exception.DAOException;
import com.king.services.scorestore.model.UserScore;
import com.king.services.scorestore.model.UserSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * InMemory Implementation of DAO layer.
 * Uses Concurrent HashMap Implementation to persist Key Value pairs.
 */
public class InMemoryScoreStoreDAOImpl implements ScoreStoreDAO {

    private static final Logger LOGGER = Logger.getLogger(InMemoryScoreStoreDAOImpl.class.getName());
    /**
     * Map to store level and collection of UserScores
     */
    private final ConcurrentMap<Integer, Set<UserScore>> levelUserScoreMap;
    /**
     * Map to store loginID and UserSession
     */
    private final ConcurrentMap<Integer, UserSession> userSessionMap;

    public InMemoryScoreStoreDAOImpl() {
        levelUserScoreMap = new ConcurrentHashMap<>();
        userSessionMap = new ConcurrentHashMap<>();
    }


    @Override
    public Set<UserScore> getUserScoresForLevel(int loginID, int level) throws DAOException {
        try {
            return levelUserScoreMap.getOrDefault(level, Collections.emptySet()).stream().
                    filter( u -> u.getLoginID() == loginID && u.getLevel() == level).
                    collect(Collectors.toSet());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in getUserScoresForLevel :", e);
            throw new DAOException("Exception encountered in getUserScoresForLevel : ", e);
        }
    }

    @Override
    public Set<UserScore> getTopNUserScoresForLevel(int level, int num) throws DAOException {
        try {
            return levelUserScoreMap.getOrDefault(level, Collections.emptySet());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in getTopNScoresForLevelDesc : :", e);
            throw new DAOException("Exception encountered in getTopNScoresForLevelDesc : ", e);
        }
    }

    @Override
    public Optional<UserSession> getUserSession(int loginID) throws DAOException {
        try {
            return Optional.ofNullable(userSessionMap.get(loginID));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in getUserSession by LoginID :", e);
            throw new DAOException("Exception encountered in getUserSession by LoginID :", e);
        }
    }

    @Override
    public Optional<UserSession> getUserSession(String sessionID) throws DAOException {
        try {
            return retrieveUserSession(sessionID);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in getUserSession by SessionID :", e);
            throw new DAOException("Exception encountered in getUserSession by SessionID :", e);
        }
    }

    @Override
    public Optional<UserSession> persistUserSession(UserSession userSession) throws DAOException {
        UserSession session = null;
        try {

            userSessionMap.put(userSession.getLoginId(), session = new UserSession(userSession.getLoginId(),
                    userSession.getSessionID(), new Date().getTime()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in persistUserSession :", e);
            throw new DAOException("Exception encountered in persistUserSession :", e);
        }
        return Optional.ofNullable(session);
    }

    @Override
    public void removeUserSession(String sessionID) throws DAOException {
        try {
            Optional<UserSession> userSessionOptional = retrieveUserSession(sessionID);
            if(userSessionOptional.isPresent()){
                if (Objects.nonNull(userSessionOptional))
                    userSessionMap.remove(userSessionOptional.get().getLoginId());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in removeUserSession :", e);
            throw new DAOException("Exception encountered in removeUserSession :", e);
        }
    }

    @Override
    public void persistUserScore(UserScore userScore) throws DAOException {
        try {

            levelUserScoreMap.computeIfAbsent(userScore.getLevel(), k -> new HashSet()).add(userScore);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in persistUserScore :", e);
            throw new DAOException("Exception encountered in persistUserScore :", e);
        }
    }

    private Optional<UserSession> retrieveUserSession(String sessionID) {
        final Map<Integer, UserSession> userSessionMap = this.userSessionMap;
        Optional<Map.Entry<Integer, UserSession>> userOptional = userSessionMap.entrySet().stream().filter(entry ->
                (sessionID.equals(entry.getValue().getSessionID()))).findFirst();
        if(userOptional.isPresent()){
            return Optional.ofNullable(userOptional.get().getValue());
        }else{
            return Optional.empty();
        }


    }
}

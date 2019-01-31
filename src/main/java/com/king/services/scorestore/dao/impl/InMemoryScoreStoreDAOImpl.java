package com.king.services.scorestore.dao.impl;

import com.king.services.scorestore.exception.DAOException;
import com.king.services.scorestore.model.User;
import com.king.services.scorestore.dao.ScoreStoreDAO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * InMemory Implementation of DAO layer.
 * Uses Concurrent HashMap Implementation to persist Key Value pairs.
 *
 */
public class InMemoryScoreStoreDAOImpl implements ScoreStoreDAO {

    private static final Logger LOGGER = Logger.getLogger(InMemoryScoreStoreDAOImpl.class.getName());
    private final ConcurrentMap<Integer, Set<User>> levelUserMap;
    private final ConcurrentMap<Integer, Long> userLoginMap;
    private final ConcurrentMap<String, Integer> sessionUserMap;

    public InMemoryScoreStoreDAOImpl() {
        levelUserMap = new ConcurrentHashMap<>();
        userLoginMap = new ConcurrentHashMap<>();
        sessionUserMap = new ConcurrentHashMap<>();
    }


    @Override
    public Set<User> getUsersForLevel(int level) throws DAOException {
        try {
            return levelUserMap.get(level);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in getUsersForLevel :", e);
            throw new DAOException("Exception encountered in getUsersForLevel : ", e);
        }
    }

    @Override
    public Optional<String> getTopNScoresForLevelDesc(int level, int num) throws DAOException {
        try {
            Set<User> users;
            if ((users = levelUserMap.getOrDefault(level, Collections.emptySet())).size() > 0) {
                return Optional.of(users.stream().sorted(Comparator.comparingInt(User::getScore)).
                        collect(Collectors.toMap(User::getLoginId, User::getScore, (integer, integer2) ->
                                Math.max(integer, integer2))).entrySet().stream().sorted(Map.Entry.
                        comparingByValue(Comparator.reverseOrder())).map((e) -> e.getKey() + "=" + e.getValue()).
                        limit(num).collect(Collectors.joining(",")));

            }
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in getTopNScoresForLevelDesc : :", e);
            throw new DAOException("Exception encountered in getTopNScoresForLevelDesc : ", e);
        }
    }

    @Override
    public Optional<String> getSessionID(int loginID) throws DAOException {
        try {
            return sessionUserMap.entrySet().stream().filter(entry -> (loginID == entry.getValue())).
                    map(e -> e.getKey()).findAny();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in getSessionID : :", e);
            throw new DAOException("Exception encountered in getSessionID : ", e);
        }
    }

    @Override
    public Optional<Integer> getLoginID(String sessionID) throws DAOException {
        try {
            return Optional.ofNullable(sessionUserMap.get(sessionID));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in getLoginID : :", e);
            throw new DAOException("Exception encountered in getLoginID : ", e);
        }
    }

    @Override
    public void saveSessionInfo(String sessionID, int loginID) throws DAOException {
        try {
            sessionUserMap.put(sessionID, loginID);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in saveSessionInfo : :", e);
            throw new DAOException("Exception encountered in saveSessionInfo : ", e);
        }
    }

    @Override
    public void saveLoginInfo(int loginID, long time) throws DAOException {
        try {
            userLoginMap.put(loginID, time);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in saveLoginInfo : :", e);
            throw new DAOException("Exception encountered in saveLoginInfo : ", e);
        }
    }

    @Override
    public Optional<Long> getLoginInfo(int loginID) throws DAOException {
        try {
            return Optional.of(userLoginMap.get(loginID));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in getLoginInfo : :", e);
            throw new DAOException("Exception encountered in getLoginInfo : ", e);
        }
    }

    @Override
    public void removeSessionID(String sessionID) throws DAOException {
        try {
            sessionUserMap.remove(sessionID);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in removeSessionID : :", e);
            throw new DAOException("Exception encountered in removeSessionID : ", e);
        }
    }

    @Override
    public boolean userExistsForLevel(int level, int loginID) throws DAOException {
        try {
            return levelUserMap.computeIfAbsent(level, k -> new HashSet<User>()).stream().
                    anyMatch(u -> (loginID == u.getLoginId() && level == u.getLevel()));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in userExistsForLevel : :", e);
            throw new DAOException("Exception encountered in userExistsForLevel : ", e);
        }
    }

    @Override
    public void saveUserInfoForLevel(String sessionID, int level, int loginID, int score) throws DAOException {
        try {
            levelUserMap.computeIfAbsent(level, k -> new HashSet()).add(new User(loginID, score, level, sessionID));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered in saveUserInfoForLevel : :", e);
            throw new DAOException("Exception encountered in saveUserInfoForLevel : ", e);
        }
    }
}

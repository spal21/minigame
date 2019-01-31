package com.king.services.scorestore.model;

import java.util.Objects;

/**
 * The User object persisted for Login, Score, Level, SessionID Info
 */
public class User {

    private int loginId;
    private final int score;
    private final int level;
    private final String sessionID;

    public User(int score, int level, String sessionID) {
        this.score = score;
        this.level = level;
        this.sessionID = sessionID;
    }

    public User(int loginId, int score, int level, String sessionID) {
        this.loginId = loginId;
        this.score = score;
        this.level = level;
        this.sessionID = sessionID;
    }

    public int getLoginId() {
        return loginId;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return loginId == user.loginId &&
                score == user.score &&
                level == user.level &&
                Objects.equals(sessionID, user.sessionID);
    }

    @Override
    public int hashCode() {

        return Objects.hash(loginId, score, level, sessionID);
    }

    @Override
    public String toString() {
        return "User{" +
                "loginId=" + loginId +
                ", score=" + score +
                ", level=" + level +
                ", sessionID='" + sessionID + '\'' +
                '}';
    }

    public String getSessionID() {
        return sessionID;
    }
}

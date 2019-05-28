package com.king.services.scorestore.model;

import java.util.Objects;

/**
 * The UserSessionDetails object used for Async processing.
 *
 */
public final class UserScoreDetails {

    private final String sessionID;
    private final int level;
    private final int score;
    private final long creationTime;

    public UserScoreDetails(String sessionID, int level, int score, long creationTime) {
        this.sessionID = sessionID;
        this.level = level;
        this.score = score;
        this.creationTime = creationTime;
    }

    public String getSessionID() {
        return sessionID;
    }

    public int getLevel() {
        return level;
    }

    public int getScore() {
        return score;
    }

    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserScoreDetails that = (UserScoreDetails) o;
        return level == that.level &&
                score == that.score &&
                creationTime == that.creationTime &&
                Objects.equals(sessionID, that.sessionID);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sessionID, level, score, creationTime);
    }

    @Override
    public String toString() {
        return "UserScoreDetails{" +
                "sessionID='" + sessionID + '\'' +
                ", level=" + level +
                ", score=" + score +
                ", creationTime=" + creationTime +
                '}';
    }
}

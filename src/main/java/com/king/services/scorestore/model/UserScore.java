package com.king.services.scorestore.model;

import java.util.Objects;

/**
 * The UserScore object persisted for LoginID, level, score and creationTime Info.
 */
public final class UserScore {

    private final int loginID;
    private final int level;
    private final int score;
    private final long creationTime;

    public UserScore(int loginID, int level, int score, long creationTime) {
        this.loginID = loginID;
        this.level = level;
        this.score = score;
        this.creationTime = creationTime;
    }

    public int getLoginID() {
        return loginID;
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
    public String toString() {
        return "UserScore{" +
                "loginID=" + loginID +
                ", level=" + level +
                ", score=" + score +
                ", creationTime=" + creationTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserScore userScore = (UserScore) o;
        return loginID == userScore.loginID &&
                level == userScore.level &&
                score == userScore.score &&
                creationTime == userScore.creationTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(loginID, level, score, creationTime);
    }
}

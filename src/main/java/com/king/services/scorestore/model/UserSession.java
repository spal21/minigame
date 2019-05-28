package com.king.services.scorestore.model;

import java.util.Objects;

/**
 * The UserSession object persisted for LoginID, SessionID and loginTime Info.
 *
 */
public class UserSession {

    private final int loginId;
    private final String sessionID;
    private final long loginTime;

    public UserSession(int loginId, String sessionID, long loginTime) {
        this.loginId = loginId;
        this.sessionID = sessionID;
        this.loginTime = loginTime;
    }

    public int getLoginId() {
        return loginId;
    }

    public String getSessionID() {
        return sessionID;
    }

    public long getLoginTime() {
        return loginTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSession that = (UserSession) o;
        return loginId == that.loginId &&
                loginTime == that.loginTime &&
                Objects.equals(sessionID, that.sessionID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loginId, sessionID, loginTime);
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "loginId=" + loginId +
                ", sessionID='" + sessionID + '\'' +
                ", loginTime=" + loginTime +
                '}';
    }

    public boolean isValid() {
        return (System.currentTimeMillis() < (loginTime + Constants.SESSION_EXPIRATION_TIME_MINS  * 60 * 1000));
    }
}

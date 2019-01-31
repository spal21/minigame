package com.king.services.scorestore.exception;

/**
 * Custom Exception class for Exceptions from DAO Layer
 */
public class DAOException extends Exception {

    private static final long serialVersionUID = 1L;

    public DAOException(String msg) {
        super(msg);
    }

    public DAOException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}

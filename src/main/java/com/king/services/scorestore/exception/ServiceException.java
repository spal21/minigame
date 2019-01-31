package com.king.services.scorestore.exception;

/**
 * Custom Exception class for Exceptions from Service Layer
 */
public class ServiceException extends Exception {

    private static final long serialVersionUID = 3L;

    public ServiceException(String msg) {
        super(msg);
    }

    public ServiceException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}

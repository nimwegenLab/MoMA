package com.jug.exceptions;

public class IlpSetupException extends RuntimeException {
    public IlpSetupException() {
    }

    public IlpSetupException(String message) {
        super(message);
    }

    public IlpSetupException(String message, Throwable cause) {
        super(message, cause);
    }

    public IlpSetupException(Throwable cause) {
        super(cause);
    }

    public IlpSetupException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

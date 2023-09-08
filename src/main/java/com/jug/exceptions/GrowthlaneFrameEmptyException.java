package com.jug.exceptions;

public class GrowthlaneFrameEmptyException extends RuntimeException {
    public GrowthlaneFrameEmptyException() {
    }

    public GrowthlaneFrameEmptyException(String message) {
        super(message);
    }

    public GrowthlaneFrameEmptyException(String message, Throwable cause) {
        super(message, cause);
    }

    public GrowthlaneFrameEmptyException(Throwable cause) {
        super(cause);
    }

    public GrowthlaneFrameEmptyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

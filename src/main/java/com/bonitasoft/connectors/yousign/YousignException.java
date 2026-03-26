package com.bonitasoft.connectors.yousign;

/**
 * Typed exception for Yousign connector.
 */
public class YousignException extends Exception {

    private final int statusCode;
    private final boolean retryable;

    public YousignException(String message) {
        super(message);
        this.statusCode = -1;
        this.retryable = false;
    }

    public YousignException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.retryable = false;
    }

    public YousignException(String message, int statusCode, boolean retryable) {
        super(message);
        this.statusCode = statusCode;
        this.retryable = retryable;
    }

    public YousignException(String message, int statusCode, boolean retryable, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.retryable = retryable;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isRetryable() {
        return retryable;
    }
}

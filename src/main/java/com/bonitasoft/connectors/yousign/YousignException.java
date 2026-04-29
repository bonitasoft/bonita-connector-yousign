package com.bonitasoft.connectors.yousign;

/**
 * Typed exception for Yousign connector.
 */
public class YousignException extends Exception {

    /** HTTP status code returned by the Yousign API, or -1 when not available. */
    private final int statusCode;

    /** Whether the failure is retryable (transient: 5xx, network errors). */
    private final boolean retryable;

    /**
     * Creates an exception without HTTP context.
     *
     * @param message human-readable description of the failure
     */
    public YousignException(String message) {
        super(message);
        this.statusCode = -1;
        this.retryable = false;
    }

    /**
     * Creates an exception wrapping a lower-level cause.
     *
     * @param message human-readable description of the failure
     * @param cause   the underlying exception
     */
    public YousignException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.retryable = false;
    }

    /**
     * Creates an exception with HTTP context.
     *
     * @param message    human-readable description of the failure
     * @param statusCode HTTP status code returned by the API
     * @param retryable  whether the failure is transient and worth retrying
     */
    public YousignException(String message, int statusCode, boolean retryable) {
        super(message);
        this.statusCode = statusCode;
        this.retryable = retryable;
    }

    /**
     * Creates an exception with HTTP context and a wrapped cause.
     *
     * @param message    human-readable description of the failure
     * @param statusCode HTTP status code returned by the API
     * @param retryable  whether the failure is transient and worth retrying
     * @param cause      the underlying exception
     */
    public YousignException(String message, int statusCode, boolean retryable, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.retryable = retryable;
    }

    /**
     * @return the HTTP status code from the Yousign API, or -1 when not available
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return {@code true} when the failure is transient and the connector retry policy
     *         may try the operation again
     */
    public boolean isRetryable() {
        return retryable;
    }
}

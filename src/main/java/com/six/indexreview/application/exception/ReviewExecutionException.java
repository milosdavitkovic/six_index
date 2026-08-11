package com.six.indexreview.application.exception;

/**
 * Core ReviewExecutionException component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public class ReviewExecutionException extends RuntimeException {
    public ReviewExecutionException(String message) {
        super(message);
    }

    public ReviewExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

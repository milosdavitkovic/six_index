package com.six.indexreview.application.exception;

public class ReviewExecutionException extends RuntimeException {
    public ReviewExecutionException(String message) {
        super(message);
    }

    public ReviewExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

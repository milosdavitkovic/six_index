package com.six.indexreview.application.exception;

import com.six.indexreview.validation.ValidationError;

import java.util.List;

/**
 * Core ReviewValidationException component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public class ReviewValidationException extends RuntimeException {
    private final List<ValidationError> validationErrors;

    public ReviewValidationException(String message, List<ValidationError> validationErrors) {
        super(message);
        this.validationErrors = List.copyOf(validationErrors == null ? List.of() : validationErrors);
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
}

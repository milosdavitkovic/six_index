package com.six.indexreview.application.exception;

import com.six.indexreview.validation.ValidationError;

import java.util.List;

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

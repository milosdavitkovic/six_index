package com.six.indexreview.validation;

import com.six.indexreview.domain.model.SecurityId;

import java.util.Objects;

/**
 * Immutable data carrier for ValidationError.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record ValidationError(
        String code,
        String field,
        String message,
        SecurityId securityId,
        ValidationSeverity severity) {

    public ValidationError {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(field, "field must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
    }

    public static ValidationError error(String code, String field, String message) {
        return new ValidationError(code, field, message, null, ValidationSeverity.ERROR);
    }

    public static ValidationError error(String code, String field, String message, SecurityId securityId) {
        return new ValidationError(code, field, message, securityId, ValidationSeverity.ERROR);
    }

    public static ValidationError warning(String code, String field, String message, SecurityId securityId) {
        return new ValidationError(code, field, message, securityId, ValidationSeverity.WARNING);
    }
}

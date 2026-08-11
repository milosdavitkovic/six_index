package com.six.indexreview.validation;

import com.six.indexreview.domain.model.SecurityId;

public record ValidationError(
        String code,
        String field,
        String message,
        SecurityId securityId,
        ValidationSeverity severity) {

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

package com.six.indexreview.api.dto;

public record ValidationErrorResponse(String code, String field, String message, Integer securityId, String severity) {
}

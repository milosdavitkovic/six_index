package com.six.indexreview.api.dto;

/**
 * Immutable data carrier for ValidationError response.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record ValidationErrorResponse(String code, String field, String message, Integer securityId, String severity) {
}

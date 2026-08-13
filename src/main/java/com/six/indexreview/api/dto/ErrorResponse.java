package com.six.indexreview.api.dto;

import java.time.Instant;
import java.util.List;

/**
 * Immutable data carrier for Error response.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record ErrorResponse(Instant timestamp, int status, String errorCode, String message,
                            String details, String traceId, List<ValidationErrorResponse> validationErrors) {
}

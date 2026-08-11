package com.six.indexreview.api.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(Instant timestamp, int status, String errorCode, String message,
                            String details, List<ValidationErrorResponse> validationErrors) {
}

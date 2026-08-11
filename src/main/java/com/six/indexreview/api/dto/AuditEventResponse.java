package com.six.indexreview.api.dto;

import java.time.Instant;

/**
 * Immutable data carrier for AuditEvent response.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public record AuditEventResponse(Instant timestamp, String ruleCode, Integer securityId,
                                 String message, String inputValue, String outputValue) {
}

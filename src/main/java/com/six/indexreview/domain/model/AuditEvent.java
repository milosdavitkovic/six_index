package com.six.indexreview.domain.model;

import java.time.Instant;

/**
 * Immutable data carrier for AuditEvent.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record AuditEvent(
        Instant timestamp,
        String ruleCode,
        SecurityId securityId,
        String message,
        String inputValue,
        String outputValue) {
}

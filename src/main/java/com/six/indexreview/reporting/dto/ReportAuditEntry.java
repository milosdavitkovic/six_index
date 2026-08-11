package com.six.indexreview.reporting.dto;

import java.time.Instant;

/**
 * Immutable data carrier for ReportAuditEntry.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public record ReportAuditEntry(Instant timestamp, String ruleCode, Integer securityId,
                               String message, String inputValue, String outputValue) {
}

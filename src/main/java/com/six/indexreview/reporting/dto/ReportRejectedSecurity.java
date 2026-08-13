package com.six.indexreview.reporting.dto;

/**
 * Immutable data carrier for ReportRejectedSecurity.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record ReportRejectedSecurity(int securityId, String reason) {
}

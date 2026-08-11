package com.six.indexreview.domain.model;

/**
 * Immutable data carrier for RejectedSecurity.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public record RejectedSecurity(SecurityId securityId, String reason) {
}

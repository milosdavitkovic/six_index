package com.six.indexreview.api.dto;

/**
 * Immutable data carrier for RejectedSecurity response.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record RejectedSecurityResponse(int securityId, String reason) {
}

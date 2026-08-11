package com.six.indexreview.api.dto;

/**
 * Immutable data carrier for Decision response.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public record DecisionResponse(int securityId, String decisionType, String reason) {
}

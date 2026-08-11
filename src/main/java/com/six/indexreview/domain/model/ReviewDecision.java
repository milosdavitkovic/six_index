package com.six.indexreview.domain.model;

/**
 * Immutable data carrier for ReviewDecision.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public record ReviewDecision(SecurityId securityId, DecisionType decisionType, String reason) {
}

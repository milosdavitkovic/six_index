package com.six.indexreview.domain.model;

/**
 * Immutable data carrier for ReviewDecision.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record ReviewDecision(SecurityId securityId, DecisionType decisionType, String reason) {
}

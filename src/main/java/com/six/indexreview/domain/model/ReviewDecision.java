package com.six.indexreview.domain.model;

public record ReviewDecision(SecurityId securityId, DecisionType decisionType, String reason) {
}

package com.six.indexreview.reporting.dto;

import com.six.indexreview.domain.model.DecisionType;

/** Report-facing classification of a security decision. This keeps decision
 * outcomes readable without exposing internal entity or rule structures. */
public record ReportDecision(int securityId, DecisionType decisionType, String reason) {
}

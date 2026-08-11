package com.six.indexreview.reporting.dto;

import com.six.indexreview.domain.model.DecisionType;

public record ReportDecision(int securityId, DecisionType decisionType, String reason) {
}

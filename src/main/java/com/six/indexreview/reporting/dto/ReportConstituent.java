package com.six.indexreview.reporting.dto;

import com.six.indexreview.domain.model.DecisionType;

import java.math.BigDecimal;

/** Report-facing constituent view including rank, weights, and capping state.
 * The DTO preserves evidence needed to explain selection, redistribution,
 * and final weight application in an audit or technical review. */
public record ReportConstituent(int securityId, int rank, BigDecimal ffmcap, BigDecimal rawWeight,
                                BigDecimal finalWeight, BigDecimal cappingFactor,
                                DecisionType decisionType, String decisionReason, boolean capped) {
}

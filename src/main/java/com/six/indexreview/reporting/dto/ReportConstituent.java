package com.six.indexreview.reporting.dto;

import com.six.indexreview.domain.model.DecisionType;

import java.math.BigDecimal;

public record ReportConstituent(int securityId, int rank, BigDecimal ffmcap, BigDecimal rawWeight,
                                BigDecimal finalWeight, BigDecimal cappingFactor,
                                DecisionType decisionType, String decisionReason, boolean capped) {
}

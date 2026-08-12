package com.six.indexreview.domain.model;

import java.math.BigDecimal;

/**
 * Immutable data carrier for SelectedConstituent.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record SelectedConstituent(
        SecurityId securityId,
        int rank,
        BigDecimal ffmcap,
        boolean currentConstituent,
        BigDecimal rawWeight,
        BigDecimal finalWeight,
        BigDecimal cappingFactor,
        DecisionType decisionType,
        String decisionReason,
        boolean capped) {

    public SelectedConstituent(SecurityId securityId, int rank, BigDecimal ffmcap,
                               boolean currentConstituent) {
        this(securityId, rank, ffmcap, currentConstituent, null, null, null, null, null, false);
    }

    public SelectedConstituent withRawWeight(BigDecimal value) {
        return new SelectedConstituent(securityId, rank, ffmcap, currentConstituent, value,
                finalWeight, cappingFactor, decisionType, decisionReason, capped);
    }

    public SelectedConstituent withFinalWeight(BigDecimal value, BigDecimal factor, boolean isCapped) {
        return new SelectedConstituent(securityId, rank, ffmcap, currentConstituent, rawWeight,
                value, factor, decisionType, decisionReason, isCapped);
    }

    public SelectedConstituent withDecision(DecisionType type, String reason) {
        return new SelectedConstituent(securityId, rank, ffmcap, currentConstituent, rawWeight,
                finalWeight, cappingFactor, type, reason, capped);
    }
}

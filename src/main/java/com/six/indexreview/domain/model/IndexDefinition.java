package com.six.indexreview.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record IndexDefinition(
        IndexCode indexCode,
        String name,
        int constituentCount,
        BigDecimal maxWeight,
        String rankingRule,
        String selectionRule,
        String bufferRule,
        String reviewPeriod,
        ReviewDates reviewDates,
        List<String> tieBreakers,
        boolean enabled,
        int bufferRetentionRank) {

    public IndexDefinition {
        if (constituentCount <= 0) {
            throw new IllegalArgumentException("Constituent count must be positive");
        }
        if (maxWeight == null || maxWeight.compareTo(BigDecimal.ZERO) <= 0
                || maxWeight.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Maximum weight must be between 0 and 1");
        }
        tieBreakers = tieBreakers == null ? List.of() : List.copyOf(tieBreakers);
    }
}

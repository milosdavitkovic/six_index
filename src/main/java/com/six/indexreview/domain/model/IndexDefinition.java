package com.six.indexreview.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Immutable data carrier for IndexDefinition.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
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
        Objects.requireNonNull(indexCode, "indexCode must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(rankingRule, "rankingRule must not be null");
        Objects.requireNonNull(selectionRule, "selectionRule must not be null");
        Objects.requireNonNull(bufferRule, "bufferRule must not be null");
        Objects.requireNonNull(reviewPeriod, "reviewPeriod must not be null");
        Objects.requireNonNull(reviewDates, "reviewDates must not be null");
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

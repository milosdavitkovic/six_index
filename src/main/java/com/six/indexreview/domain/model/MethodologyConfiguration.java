package com.six.indexreview.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Configuration-driven methodology parameters for an index family.
 *
 * Methodology rules are configuration-driven to minimize future implementation
 * effort when index rules evolve.
 * @author Milos Davitkovic
 */
public record MethodologyConfiguration(
        int constituentCount,
        BigDecimal maxWeight,
        String rankingRule,
        String selectionRule,
        String bufferRule,
        List<String> tieBreakers,
        int bufferRetentionRank) {

    public MethodologyConfiguration {
        if (constituentCount <= 0) {
            throw new IllegalArgumentException("Constituent count must be positive");
        }
        Objects.requireNonNull(maxWeight, "maxWeight must not be null");
        Objects.requireNonNull(rankingRule, "rankingRule must not be null");
        Objects.requireNonNull(selectionRule, "selectionRule must not be null");
        Objects.requireNonNull(bufferRule, "bufferRule must not be null");
        tieBreakers = tieBreakers == null ? List.of() : List.copyOf(tieBreakers);
    }
}


package com.six.indexreview.domain.model;

import java.math.BigDecimal;

/**
 * Immutable data carrier for RankedSecurity.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record RankedSecurity(
        SecurityId securityId,
        int rank,
        BigDecimal price,
        BigDecimal shares,
        BigDecimal freeFloat,
        BigDecimal ffmcap,
        boolean currentConstituent) {
}

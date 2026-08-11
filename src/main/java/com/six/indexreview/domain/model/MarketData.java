package com.six.indexreview.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable data carrier for MarketData.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public record MarketData(
        SecurityId securityId,
        LocalDate date,
        BigDecimal price,
        BigDecimal shares,
        BigDecimal freeFloat) {

    public MarketData {
        Objects.requireNonNull(securityId, "securityId must not be null");
        Objects.requireNonNull(date, "date must not be null");
    }
}

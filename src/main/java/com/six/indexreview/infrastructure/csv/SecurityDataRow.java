package com.six.indexreview.infrastructure.csv;

import com.six.indexreview.domain.model.SecurityId;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Immutable data carrier for SecurityDataRow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public record SecurityDataRow(SecurityId securityId, LocalDate date, BigDecimal price,
                              BigDecimal freeFloat, BigDecimal shares) {

    public SecurityDataRow {
        if (securityId == null || date == null) {
            throw new IllegalArgumentException("securityId and date must not be null");
        }
    }
}

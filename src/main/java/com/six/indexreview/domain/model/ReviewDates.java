package com.six.indexreview.domain.model;

import java.time.LocalDate;

/**
 * Immutable data carrier for ReviewDates.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record ReviewDates(LocalDate cutOffDate, LocalDate reviewDate) {
    public ReviewDates {
        if (cutOffDate == null || reviewDate == null) {
            throw new IllegalArgumentException("Both review dates are required");
        }
        if (!cutOffDate.isBefore(reviewDate)) {
            throw new IllegalArgumentException("Cut-off date must be before review date");
        }
    }
}

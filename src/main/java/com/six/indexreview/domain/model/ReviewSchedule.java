package com.six.indexreview.domain.model;

/**
 * Immutable data carrier for ReviewSchedule.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record ReviewSchedule(String reviewPeriod, ReviewDates dates) {
}

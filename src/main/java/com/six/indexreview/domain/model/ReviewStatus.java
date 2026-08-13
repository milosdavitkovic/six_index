package com.six.indexreview.domain.model;

/**
 * Enumeration of review status values.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public enum ReviewStatus {
    COMPLETED,
    COMPLETED_WITH_WARNINGS,
    FAILED
}

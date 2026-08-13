package com.six.indexreview.api.dto;

/**
 * Immutable data carrier for Import response.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record ImportResponse(String dataset, int inputRows, int storedRows, int deduplicatedRows) {
}

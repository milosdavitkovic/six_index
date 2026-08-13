package com.six.indexreview.api.dto;

/**
 * Immutable data carrier for Health response.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record HealthResponse(String status, String service) {
}

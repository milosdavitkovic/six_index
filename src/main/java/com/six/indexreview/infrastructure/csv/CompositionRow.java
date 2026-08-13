package com.six.indexreview.infrastructure.csv;

import com.six.indexreview.domain.model.SecurityId;

/**
 * Immutable data carrier for CompositionRow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record CompositionRow(SecurityId securityId) {
}

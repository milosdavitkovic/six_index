package com.six.indexreview.infrastructure.csv;

import com.six.indexreview.domain.model.SecurityId;

import java.time.LocalDate;

/**
 * Immutable data carrier for SpiUniverseRow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record SpiUniverseRow(LocalDate date, SecurityId securityId) {
}

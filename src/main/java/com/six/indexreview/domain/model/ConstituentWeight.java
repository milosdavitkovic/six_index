package com.six.indexreview.domain.model;

import java.math.BigDecimal;

/**
 * Immutable data carrier for ConstituentWeight.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public record ConstituentWeight(SecurityId securityId, BigDecimal rawWeight, BigDecimal finalWeight,
                                BigDecimal cappingFactor, boolean capped) {
}

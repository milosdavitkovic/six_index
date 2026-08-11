package com.six.indexreview.api.dto;

import java.math.BigDecimal;

/**
 * Immutable data carrier for Constituent response.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public record ConstituentResponse(int securityId, int rank, BigDecimal ffmcap, BigDecimal rawWeight,
                                  BigDecimal finalWeight, BigDecimal cappingFactor,
                                  String decisionType, String decisionReason, boolean capped) {
}

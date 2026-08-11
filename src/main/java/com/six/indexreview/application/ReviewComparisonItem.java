package com.six.indexreview.application;

import java.math.BigDecimal;

/**
 * One security-level movement entry within a review comparison.
 */
public record ReviewComparisonItem(Integer securityId, Integer baselineRank, Integer comparisonRank,
                                   Integer rankDelta, BigDecimal baselineWeight, BigDecimal comparisonWeight,
                                   BigDecimal weightDelta, ComparisonChange change) {
}


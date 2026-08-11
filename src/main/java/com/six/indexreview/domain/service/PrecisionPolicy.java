package com.six.indexreview.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Central financial precision policy used by every calculation rule. */
public record PrecisionPolicy(int internalScale, int outputScale, RoundingMode roundingMode) {

    public PrecisionPolicy {
        if (internalScale < 16) {
            throw new IllegalArgumentException("Internal scale must be at least 16");
        }
        if (outputScale < 1 || outputScale > internalScale) {
            throw new IllegalArgumentException("Output scale must be positive and no greater than internal scale");
        }
        if (roundingMode == null) {
            throw new IllegalArgumentException("Rounding mode is required");
        }
    }

    public BigDecimal internal(BigDecimal value) {
        return value.setScale(internalScale, roundingMode);
    }

    public BigDecimal output(BigDecimal value) {
        return value.setScale(outputScale, roundingMode);
    }

    public BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
        return numerator.divide(denominator, internalScale, roundingMode);
    }
}

package com.six.indexreview.domain.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Central financial precision policy used by every calculation rule.
 *
 * A single policy keeps rounding deterministic and makes methodology changes
 * easier to review, test, and version.
 * @author Milos Davitkovic
 */
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
        // Internal precision is kept higher than the published output scale so
        // intermediate redistribution steps do not accumulate rounding drift.
        return value.setScale(internalScale, roundingMode);
    }

    public BigDecimal output(BigDecimal value) {
        // Output values are rounded only once, at the point they become part of
        // the auditable review result.
        return value.setScale(outputScale, roundingMode);
    }

    public BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
        // Deterministic division protects the review from JVM/default-scale
        // differences and keeps repeated runs reproducible.
        return numerator.divide(denominator, internalScale, roundingMode);
    }

    /**
     * Math context for iterative calculations that must retain more precision
     * than the published weight scale. Keeping this here prevents individual
     * rules from silently choosing different precision or rounding settings.
     */
    public MathContext internalMathContext() {
        return new MathContext(Math.max(34, internalScale + 12), roundingMode);
    }
}

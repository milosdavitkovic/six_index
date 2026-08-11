package com.six.indexreview.domain.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Calculates free-float market capitalisation for a security.
 *
 * BigDecimal is used to avoid floating-point drift and keep financial inputs
 * reproducible across review runs and environments.
 */
@Component
public class FreeFloatMarketCapCalculator {

    public BigDecimal calculate(BigDecimal price, BigDecimal shares, BigDecimal freeFloat) {
        if (price == null || shares == null || freeFloat == null) {
            throw new IllegalArgumentException("Price, shares and free float are required for FFMCAP");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0 || shares.compareTo(BigDecimal.ZERO) <= 0
                || freeFloat.compareTo(BigDecimal.ZERO) < 0 || freeFloat.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Invalid values for FFMCAP calculation");
        }
        // Keep the calculation intentionally direct so the audit trail can point
        // to a single reproducible formula.
        return price.multiply(shares).multiply(freeFloat);
    }
}

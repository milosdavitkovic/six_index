package com.six.indexreview.domain.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
        return price.multiply(shares).multiply(freeFloat);
    }
}

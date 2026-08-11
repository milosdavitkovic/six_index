package com.six.indexreview.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MarketData(
        SecurityId securityId,
        LocalDate date,
        BigDecimal price,
        BigDecimal shares,
        BigDecimal freeFloat) {

    public MarketData {
        if (securityId == null || date == null) {
            throw new IllegalArgumentException("Market data security ID and date are required");
        }
    }
}

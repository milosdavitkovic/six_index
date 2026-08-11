package com.six.indexreview.domain.model;

import java.math.BigDecimal;

public record RankedSecurity(
        SecurityId securityId,
        int rank,
        BigDecimal price,
        BigDecimal shares,
        BigDecimal freeFloat,
        BigDecimal ffmcap,
        boolean currentConstituent) {
}

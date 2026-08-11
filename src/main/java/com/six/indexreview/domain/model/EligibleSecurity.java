package com.six.indexreview.domain.model;

import java.math.BigDecimal;

public record EligibleSecurity(
        SecurityId securityId,
        BigDecimal price,
        BigDecimal shares,
        BigDecimal freeFloat,
        BigDecimal ffmcap,
        boolean currentConstituent) {
}

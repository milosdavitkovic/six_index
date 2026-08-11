package com.six.indexreview.domain.model;

import java.math.BigDecimal;

public record ConstituentWeight(SecurityId securityId, BigDecimal rawWeight, BigDecimal finalWeight,
                                BigDecimal cappingFactor, boolean capped) {
}

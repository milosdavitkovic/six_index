package com.six.indexreview.api.dto;

import java.math.BigDecimal;

public record ConstituentResponse(int securityId, int rank, BigDecimal ffmcap, BigDecimal rawWeight,
                                  BigDecimal finalWeight, BigDecimal cappingFactor,
                                  String decisionType, String decisionReason, boolean capped) {
}

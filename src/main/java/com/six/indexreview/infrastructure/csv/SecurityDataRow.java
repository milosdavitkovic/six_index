package com.six.indexreview.infrastructure.csv;

import com.six.indexreview.domain.model.SecurityId;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SecurityDataRow(SecurityId securityId, LocalDate date, BigDecimal price,
                              BigDecimal freeFloat, BigDecimal shares) {
}

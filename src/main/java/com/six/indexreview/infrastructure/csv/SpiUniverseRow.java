package com.six.indexreview.infrastructure.csv;

import com.six.indexreview.domain.model.SecurityId;

import java.time.LocalDate;

public record SpiUniverseRow(LocalDate date, SecurityId securityId) {
}

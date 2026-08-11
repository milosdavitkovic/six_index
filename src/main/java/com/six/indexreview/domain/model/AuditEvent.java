package com.six.indexreview.domain.model;

import java.time.Instant;

public record AuditEvent(
        Instant timestamp,
        String ruleCode,
        SecurityId securityId,
        String message,
        String inputValue,
        String outputValue) {
}

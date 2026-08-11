package com.six.indexreview.reporting.dto;

import java.time.Instant;

public record ReportAuditEntry(Instant timestamp, String ruleCode, Integer securityId,
                               String message, String inputValue, String outputValue) {
}

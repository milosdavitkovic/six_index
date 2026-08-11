package com.six.indexreview.api.dto;

import java.time.Instant;

public record AuditEventResponse(Instant timestamp, String ruleCode, Integer securityId,
                                 String message, String inputValue, String outputValue) {
}

package com.six.indexreview.domain.service;

import com.six.indexreview.domain.model.AuditEvent;
import com.six.indexreview.domain.model.SecurityId;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

/**
 * Core AuditEventFactory component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
@Component
public class AuditEventFactory {
    private final Clock clock;

    public AuditEventFactory(Clock clock) {
        this.clock = clock;
    }

    public AuditEvent create(String ruleCode, SecurityId securityId, String message,
                             String inputValue, String outputValue) {
        return create(Instant.now(clock), ruleCode, securityId, message, inputValue, outputValue);
    }

    public AuditEvent create(Instant timestamp, String ruleCode, SecurityId securityId,
                             String message, String inputValue, String outputValue) {
        return new AuditEvent(timestamp, ruleCode, securityId, message, inputValue, outputValue);
    }
}

package com.six.indexreview.domain.service;

import com.six.indexreview.domain.model.SecurityId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Milos Davitkovic
 */
class AuditEventFactoryTest {
    @Test
    void createsStableTimestampedAuditEvent() {
        Instant instant = Instant.parse("2026-09-21T00:00:00Z");
        AuditEventFactory factory = new AuditEventFactory(Clock.fixed(instant, ZoneOffset.UTC));
        var event = factory.create("RULE", new SecurityId(7), "message", "in", "out");
        assertThat(event.timestamp()).isEqualTo(instant);
        assertThat(event.securityId()).isEqualTo(new SecurityId(7));
        assertThat(event.outputValue()).isEqualTo("out");
    }
}

package com.six.indexreview.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

/**
 * Core AuditEventEntity component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
/**
 * @author Milos Davitkovic
 */
@Getter
@Entity
@Table(name = "audit_event", indexes = @Index(name = "ix_audit_event_result_security", columnList = "review_result_id,security_id"))
public class AuditEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_result_id", nullable = false)
    private ReviewResultEntity reviewResult;

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "rule_code", nullable = false, length = 64)
    private String ruleCode;

    @Column(name = "security_id")
    private Integer securityId;

    @Column(nullable = false, length = 1024)
    private String message;

    @Column(name = "input_value", length = 2048)
    private String inputValue;

    @Column(name = "output_value", length = 2048)
    private String outputValue;

    protected AuditEventEntity() {
    }

    public AuditEventEntity(int sequenceNumber, Instant timestamp, String ruleCode,
                            Integer securityId, String message, String inputValue,
                            String outputValue) {
        this.sequenceNumber = sequenceNumber;
        this.timestamp = timestamp;
        this.ruleCode = ruleCode;
        this.securityId = securityId;
        this.message = message;
        this.inputValue = inputValue;
        this.outputValue = outputValue;
    }

    void attachTo(ReviewResultEntity result) {
        this.reviewResult = result;
    }
}

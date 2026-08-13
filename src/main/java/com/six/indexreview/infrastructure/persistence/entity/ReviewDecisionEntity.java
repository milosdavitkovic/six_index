package com.six.indexreview.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * Core ReviewDecisionEntity component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
/**
 * @author Milos Davitkovic
 */
@Getter
@Entity
@Table(name = "review_decision")
public class ReviewDecisionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_result_id", nullable = false)
    private ReviewResultEntity reviewResult;

    @Column(name = "security_id", nullable = false)
    private Integer securityId;

    @Column(name = "decision_type", nullable = false, length = 32)
    private String decisionType;

    @Column(nullable = false, length = 1024)
    private String reason;

    protected ReviewDecisionEntity() {
    }

    public ReviewDecisionEntity(Integer securityId, String decisionType, String reason) {
        this.securityId = securityId;
        this.decisionType = decisionType;
        this.reason = reason;
    }

    void attachTo(ReviewResultEntity result) {
        this.reviewResult = result;
    }
}

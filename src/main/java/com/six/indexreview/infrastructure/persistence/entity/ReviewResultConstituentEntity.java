package com.six.indexreview.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Core ReviewResultConstituentEntity component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
@Getter
@Builder
@Entity
@Table(name = "review_result_constituent")
public class ReviewResultConstituentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_result_id", nullable = false)
    private ReviewResultEntity reviewResult;

    @Column(name = "security_id", nullable = false)
    private Integer securityId;

    @Column(nullable = false)
    private int rank;

    @Column(precision = 38, scale = 16, nullable = false)
    private BigDecimal ffmcap;

    @Column(name = "raw_weight", precision = 38, scale = 16, nullable = false)
    private BigDecimal rawWeight;

    @Column(name = "final_weight", precision = 38, scale = 10, nullable = false)
    private BigDecimal finalWeight;

    @Column(name = "capping_factor", precision = 38, scale = 16, nullable = false)
    private BigDecimal cappingFactor;

    @Column(name = "decision_type", nullable = false, length = 32)
    private String decisionType;

    @Column(name = "decision_reason", nullable = false, length = 512)
    private String decisionReason;

    @Column(nullable = false)
    private boolean capped;

    protected ReviewResultConstituentEntity() {
    }

    private ReviewResultConstituentEntity(Long id, ReviewResultEntity reviewResult, Integer securityId,
                                          int rank, BigDecimal ffmcap, BigDecimal rawWeight,
                                          BigDecimal finalWeight, BigDecimal cappingFactor,
                                          String decisionType, String decisionReason, boolean capped) {
        this.id = id;
        this.reviewResult = reviewResult;
        this.securityId = securityId;
        this.rank = rank;
        this.ffmcap = ffmcap;
        this.rawWeight = rawWeight;
        this.finalWeight = finalWeight;
        this.cappingFactor = cappingFactor;
        this.decisionType = decisionType;
        this.decisionReason = decisionReason;
        this.capped = capped;
    }

    void attachTo(ReviewResultEntity result) {
        this.reviewResult = result;
    }
}

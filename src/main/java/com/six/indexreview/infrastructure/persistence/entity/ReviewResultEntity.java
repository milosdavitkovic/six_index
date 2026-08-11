package com.six.indexreview.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@Table(name = "review_result", indexes = @Index(name = "ix_review_result_index_period_created", columnList = "index_code,review_period,created_at"))
public class ReviewResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "index_code", nullable = false, length = 32)
    private String indexCode;

    @Column(name = "review_period", nullable = false, length = 32)
    private String reviewPeriod;

    @Column(name = "cut_off_date", nullable = false)
    private LocalDate cutOffDate;

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "total_eligible", nullable = false)
    private int totalEligible;

    @Column(name = "total_selected", nullable = false)
    private int totalSelected;

    @OneToMany(mappedBy = "reviewResult", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("rank ASC")
    @Builder.Default
    private List<ReviewResultConstituentEntity> constituents = new ArrayList<>();

    @OneToMany(mappedBy = "reviewResult", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("securityId ASC")
    @Builder.Default
    private List<ReviewDecisionEntity> decisions = new ArrayList<>();

    @OneToMany(mappedBy = "reviewResult", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequenceNumber ASC")
    @Builder.Default
    private List<AuditEventEntity> auditEvents = new ArrayList<>();

    protected ReviewResultEntity() {
    }

    private ReviewResultEntity(Long id, String indexCode, String reviewPeriod, LocalDate cutOffDate,
                               LocalDate reviewDate, String status, Instant createdAt, int totalEligible,
                               int totalSelected, List<ReviewResultConstituentEntity> constituents,
                               List<ReviewDecisionEntity> decisions, List<AuditEventEntity> auditEvents) {
        this.id = id;
        this.indexCode = indexCode;
        this.reviewPeriod = reviewPeriod;
        this.cutOffDate = cutOffDate;
        this.reviewDate = reviewDate;
        this.status = status;
        this.createdAt = createdAt;
        this.totalEligible = totalEligible;
        this.totalSelected = totalSelected;
        this.constituents = constituents;
        this.decisions = decisions;
        this.auditEvents = auditEvents;
    }

    public void addConstituent(ReviewResultConstituentEntity constituent) {
        constituents.add(constituent);
        constituent.attachTo(this);
    }

    public void addDecision(ReviewDecisionEntity decision) {
        decisions.add(decision);
        decision.attachTo(this);
    }

    public void addAuditEvent(AuditEventEntity event) {
        auditEvents.add(event);
        event.attachTo(this);
    }
}

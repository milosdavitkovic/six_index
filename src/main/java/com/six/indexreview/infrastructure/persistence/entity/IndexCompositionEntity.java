package com.six.indexreview.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * Core IndexCompositionEntity component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
/**
 * @author Milos Davitkovic
 */
@Getter
@Entity
@Table(name = "index_composition", uniqueConstraints = @UniqueConstraint(name = "uk_composition_index_period_security", columnNames = {"index_code", "review_period", "security_id"}))
public class IndexCompositionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "index_code", nullable = false, length = 32)
    private String indexCode;

    @Column(name = "review_period", nullable = false, length = 32)
    private String reviewPeriod;

    @Column(name = "security_id", nullable = false)
    private Integer securityId;

    protected IndexCompositionEntity() {
    }

    public IndexCompositionEntity(String indexCode, String reviewPeriod, Integer securityId) {
        this.indexCode = indexCode;
        this.reviewPeriod = reviewPeriod;
        this.securityId = securityId;
    }
}

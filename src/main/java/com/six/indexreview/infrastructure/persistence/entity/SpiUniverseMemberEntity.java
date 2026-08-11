package com.six.indexreview.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Core SpiUniverseMemberEntity component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
@Getter
@Entity
@Table(name = "spi_universe_member", uniqueConstraints = @UniqueConstraint(name = "uk_spi_universe_security_date", columnNames = {"security_id", "universe_date"}))
public class SpiUniverseMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "security_id", nullable = false)
    private Integer securityId;

    @Column(name = "universe_date", nullable = false)
    private LocalDate date;

    protected SpiUniverseMemberEntity() {
    }

    public SpiUniverseMemberEntity(Integer securityId, LocalDate date) {
        this.securityId = securityId;
        this.date = date;
    }
}

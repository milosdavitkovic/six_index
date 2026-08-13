package com.six.indexreview.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Core MarketDataEntity component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
/**
 * @author Milos Davitkovic
 */
@Getter
@Entity
@Table(name = "market_data", uniqueConstraints = @UniqueConstraint(name = "uk_market_data_security_date", columnNames = {"security_id", "data_date"}))
public class MarketDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "security_id", nullable = false)
    private Integer securityId;

    @Column(name = "data_date", nullable = false)
    private LocalDate date;

    @Column(precision = 38, scale = 16)
    private BigDecimal price;

    @Column(precision = 38, scale = 16)
    private BigDecimal shares;

    @Column(name = "free_float", precision = 38, scale = 16)
    private BigDecimal freeFloat;

    protected MarketDataEntity() {
    }

    public MarketDataEntity(Integer securityId, LocalDate date, BigDecimal price,
                            BigDecimal shares, BigDecimal freeFloat) {
        this.securityId = securityId;
        this.date = date;
        this.price = price;
        this.shares = shares;
        this.freeFloat = freeFloat;
    }
}

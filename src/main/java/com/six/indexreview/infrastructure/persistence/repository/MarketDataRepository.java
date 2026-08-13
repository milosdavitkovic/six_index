package com.six.indexreview.infrastructure.persistence.repository;

import com.six.indexreview.infrastructure.persistence.entity.MarketDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Strategy contract for MarketDataRepository in the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public interface MarketDataRepository extends JpaRepository<MarketDataEntity, Long> {
    List<MarketDataEntity> findAllByDate(LocalDate date);
}

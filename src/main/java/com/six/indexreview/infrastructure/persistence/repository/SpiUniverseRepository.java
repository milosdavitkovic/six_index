package com.six.indexreview.infrastructure.persistence.repository;

import com.six.indexreview.infrastructure.persistence.entity.SpiUniverseMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Strategy contract for SpiUniverseRepository in the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public interface SpiUniverseRepository extends JpaRepository<SpiUniverseMemberEntity, Long> {
    List<SpiUniverseMemberEntity> findAllByDate(LocalDate date);
}

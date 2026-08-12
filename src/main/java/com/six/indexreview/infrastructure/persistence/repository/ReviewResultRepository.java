package com.six.indexreview.infrastructure.persistence.repository;

import com.six.indexreview.infrastructure.persistence.entity.ReviewResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Strategy contract for ReviewResultRepository in the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public interface ReviewResultRepository extends JpaRepository<ReviewResultEntity, Long> {
    Optional<ReviewResultEntity> findById(Long id);

    Optional<ReviewResultEntity> findTopByIndexCodeIgnoreCaseAndReviewPeriodIgnoreCaseOrderByCreatedAtDescIdDesc(String indexCode,
                                                                                                                String reviewPeriod);
}

package com.six.indexreview.domain.repository;

import com.six.indexreview.infrastructure.persistence.entity.ReviewResultEntity;

import java.util.Optional;

/**
 * Repository abstraction for storing and retrieving review results.
 *
 * Repository abstraction allows audit and review history persistence without
 * impacting methodology logic.
 */
public interface ReviewResultRepositoryPort {
    ReviewResultEntity saveReviewResult(ReviewResultEntity entity);

    Optional<ReviewResultEntity> findReviewResultById(Long id);

    Optional<ReviewResultEntity> findTopByIndexCodeAndReviewPeriodOrderByCreatedAtDescIdDesc(String indexCode, String reviewPeriod);
}



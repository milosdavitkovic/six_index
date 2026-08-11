package com.six.indexreview.infrastructure.persistence.repository;

import com.six.indexreview.domain.repository.ReviewResultRepositoryPort;
import com.six.indexreview.infrastructure.persistence.entity.ReviewResultEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter that exposes review-result persistence through a technology-neutral port.
 *
 * Repository abstraction allows audit and review history persistence without
 * impacting methodology logic.
 */
@Component
@RequiredArgsConstructor
public class ReviewResultRepositoryAdapter implements ReviewResultRepositoryPort {
    private final ReviewResultRepository delegate;

    @Override
    public ReviewResultEntity saveReviewResult(ReviewResultEntity entity) {
        return delegate.save(entity);
    }

    @Override
    public Optional<ReviewResultEntity> findReviewResultById(Long id) {
        return delegate.findById(id);
    }

    @Override
    public Optional<ReviewResultEntity> findTopByIndexCodeAndReviewPeriodOrderByCreatedAtDescIdDesc(String indexCode, String reviewPeriod) {
        return delegate.findTopByIndexCodeAndReviewPeriodOrderByCreatedAtDescIdDesc(indexCode, reviewPeriod);
    }
}



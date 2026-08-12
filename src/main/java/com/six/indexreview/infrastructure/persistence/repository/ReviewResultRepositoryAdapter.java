package com.six.indexreview.infrastructure.persistence.repository;

import com.six.indexreview.domain.repository.ReviewResultRepositoryPort;
import com.six.indexreview.domain.model.ReviewResult;
import com.six.indexreview.infrastructure.persistence.entity.ReviewResultEntity;
import com.six.indexreview.infrastructure.persistence.mapper.ReviewResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter that exposes review-result persistence through a technology-neutral port.
 *
 * Repository abstraction allows audit and review history persistence without
 * impacting methodology logic.
 */
/**
 * @author Milos Davitkovic
 */
@Component
@RequiredArgsConstructor
public class ReviewResultRepositoryAdapter implements ReviewResultRepositoryPort {
    private final ReviewResultRepository delegate;
    private final ReviewResultMapper mapper;

    @Override
    public ReviewResult saveReviewResult(ReviewResult result) {
        return mapper.toDomain(delegate.save(mapper.toEntity(result)));
    }

    @Override
    public Optional<ReviewResult> findReviewResultById(Long id) {
        return delegate.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<ReviewResult> findTopByIndexCodeIgnoreCaseAndReviewPeriodIgnoreCaseOrderByCreatedAtDescIdDesc(String indexCode,
                                                                                                                      String reviewPeriod) {
        return delegate.findTopByIndexCodeIgnoreCaseAndReviewPeriodIgnoreCaseOrderByCreatedAtDescIdDesc(indexCode, reviewPeriod)
                .map(mapper::toDomain);
    }
}



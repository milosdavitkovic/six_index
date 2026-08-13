package com.six.indexreview.domain.repository;

import com.six.indexreview.domain.model.ReviewResult;

import java.util.Optional;

/**
 * Repository abstraction for storing and retrieving review results.
 *
 * Repository abstraction allows audit and review history persistence without
 * impacting methodology logic.
 * @author Milos Davitkovic
 */
public interface ReviewResultRepositoryPort {
    ReviewResult saveReviewResult(ReviewResult result);

    Optional<ReviewResult> findReviewResultById(Long id);

    Optional<ReviewResult> findTopByIndexCodeIgnoreCaseAndReviewPeriodIgnoreCaseOrderByCreatedAtDescIdDesc(String indexCode,
                                                                                                               String reviewPeriod);
}



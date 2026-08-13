package com.six.indexreview.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Compares two stored review runs for governance and operational analysis.
 *
 * Historical comparison supports index governance, review transparency, and
 * operational analysis.
 */
/**
 * @author Milos Davitkovic
 */
@Service
@RequiredArgsConstructor
public class ReviewComparisonService {
    private final ReviewComparisonOrchestrator comparisonOrchestrator;

    @Transactional(readOnly = true)
    public ReviewComparisonReport compare(Long baselineReviewResultId, Long comparisonReviewResultId) {
        return comparisonOrchestrator.compare(baselineReviewResultId, comparisonReviewResultId);
    }
}



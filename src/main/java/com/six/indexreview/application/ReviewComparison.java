package com.six.indexreview.application;

import java.util.List;
import java.util.Map;

/**
 * Comparison report for two review runs.
 * @author Milos Davitkovic
 */
public record ReviewComparison(Long baselineReviewResultId, Long comparisonReviewResultId, String indexCode,
                               String baselineReviewPeriod, String comparisonReviewPeriod,
                               Map<?, ?> baselineWarnings, Map<?, ?> comparisonWarnings,
                               List<ReviewComparisonItem> items, String baselineStatus, String comparisonStatus,
                               List<Integer> joiners, List<Integer> leavers,
                               List<ReviewComparisonItem> rankMovements, List<ReviewComparisonItem> weightMovements) {
}


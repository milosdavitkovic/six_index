package com.six.indexreview.application;

import com.six.indexreview.domain.model.SecurityId;

import java.util.List;
import java.util.Map;

/**
 * Comparison report for two review runs.
 */
public record ReviewComparisonReport(Long baselineReviewResultId, Long comparisonReviewResultId, String indexCode,
                                     String baselineReviewPeriod, String comparisonReviewPeriod,
                                     Map<SecurityId, String> baselineWarnings, Map<SecurityId, String> comparisonWarnings,
                                     List<ReviewComparisonItem> items, String baselineStatus, String comparisonStatus,
                                     List<Integer> joiners, List<Integer> leavers,
                                     List<ReviewComparisonItem> rankMovements, List<ReviewComparisonItem> weightMovements) {
}


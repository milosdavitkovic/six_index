package com.six.indexreview.application;

import com.six.indexreview.domain.model.ReviewResult;
import com.six.indexreview.domain.model.SelectedConstituent;
import com.six.indexreview.domain.repository.ReviewResultRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds review comparison reports.
 */
/**
 * @author Milos Davitkovic
 */
@Component
@RequiredArgsConstructor
public class ReviewComparisonOrchestrator {
    private final ReviewResultRepositoryPort reviewResultRepository;

    public ReviewComparisonReport compare(Long baselineReviewResultId, Long comparisonReviewResultId) {
        return build(load(baselineReviewResultId), load(comparisonReviewResultId));
    }

    private ReviewResult load(Long id) {
        return reviewResultRepository.findReviewResultById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review result not found: " + id));
    }

    public ReviewComparisonReport build(ReviewResult baseline, ReviewResult comparison) {
        List<ReviewComparisonItem> items = compareConstituents(constituentsBySecurity(baseline), constituentsBySecurity(comparison));
        return new ReviewComparisonReport(baseline.id(), comparison.id(), baseline.indexCode().value(), baseline.reviewPeriod(),
                comparison.reviewPeriod(), baseline.validationWarnings(), comparison.validationWarnings(), items,
                baseline.status().name(), comparison.status().name(), joiners(items), leavers(items), rankMovements(items), weightMovements(items));
    }

    private Map<Integer, SelectedConstituent> constituentsBySecurity(ReviewResult result) {
        return result.constituents().stream()
                .collect(Collectors.toMap(value -> value.securityId().value(), value -> value, (left, right) -> left, LinkedHashMap::new));
    }

    private List<ReviewComparisonItem> compareConstituents(Map<Integer, SelectedConstituent> baselineBySecurity,
                                                           Map<Integer, SelectedConstituent> comparisonBySecurity) {
        List<ReviewComparisonItem> items = new ArrayList<>();
        Set<Integer> allSecurities = new java.util.TreeSet<>();
        allSecurities.addAll(baselineBySecurity.keySet());
        allSecurities.addAll(comparisonBySecurity.keySet());
        for (Integer securityId : allSecurities) {
            SelectedConstituent before = baselineBySecurity.get(securityId);
            SelectedConstituent after = comparisonBySecurity.get(securityId);
            ComparisonChange change = classifyChange(before, after);
            Integer baselineRank = before == null ? null : before.rank();
            Integer comparisonRank = after == null ? null : after.rank();
            BigDecimal baselineWeight = before == null ? null : before.finalWeight();
            BigDecimal comparisonWeight = after == null ? null : after.finalWeight();
            Integer rankDelta = baselineRank == null || comparisonRank == null ? null : comparisonRank - baselineRank;
            BigDecimal weightDelta = baselineWeight == null || comparisonWeight == null ? null : comparisonWeight.subtract(baselineWeight);
            items.add(new ReviewComparisonItem(securityId, baselineRank, comparisonRank, rankDelta, baselineWeight,
                    comparisonWeight, weightDelta, change));
        }
        return items;
    }

    private ComparisonChange classifyChange(SelectedConstituent before, SelectedConstituent after) {
        if (before == null) return ComparisonChange.JOINER;
        if (after == null) return ComparisonChange.LEAVER;
        return ComparisonChange.PRESENT;
    }

    private List<Integer> joiners(List<ReviewComparisonItem> items) {
        return items.stream().filter(item -> item.change() == ComparisonChange.JOINER).map(ReviewComparisonItem::securityId).toList();
    }

    private List<Integer> leavers(List<ReviewComparisonItem> items) {
        return items.stream().filter(item -> item.change() == ComparisonChange.LEAVER).map(ReviewComparisonItem::securityId).toList();
    }

    private List<ReviewComparisonItem> rankMovements(List<ReviewComparisonItem> items) {
        return items.stream().filter(item -> item.rankDelta() != null && item.rankDelta() != 0).toList();
    }

    private List<ReviewComparisonItem> weightMovements(List<ReviewComparisonItem> items) {
        return items.stream().filter(item -> item.weightDelta() != null && item.weightDelta().compareTo(BigDecimal.ZERO) != 0).toList();
    }
}


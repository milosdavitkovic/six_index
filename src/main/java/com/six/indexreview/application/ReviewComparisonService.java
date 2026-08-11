package com.six.indexreview.application;

import com.six.indexreview.domain.model.ReviewResult;
import com.six.indexreview.domain.model.SelectedConstituent;
import com.six.indexreview.infrastructure.persistence.entity.ReviewResultEntity;
import com.six.indexreview.infrastructure.persistence.repository.ReviewResultRepository;
import com.six.indexreview.infrastructure.persistence.mapper.ReviewResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Compares two stored review runs for governance and operational analysis.
 *
 * Historical comparison supports index governance, review transparency, and
 * operational analysis.
 */
@Service
@RequiredArgsConstructor
public class ReviewComparisonService {
    private final ReviewResultRepository reviewResultRepository;
    private final ReviewResultMapper reviewResultMapper;

    @Transactional(readOnly = true)
    public ReviewComparison compare(Long baselineReviewResultId, Long comparisonReviewResultId) {
        ReviewResult baseline = load(baselineReviewResultId);
        ReviewResult comparison = load(comparisonReviewResultId);
        Map<Integer, SelectedConstituent> baselineBySecurity = baseline.constituents().stream()
                .collect(Collectors.toMap(value -> value.securityId().value(), value -> value, (left, right) -> left, LinkedHashMap::new));
        Map<Integer, SelectedConstituent> comparisonBySecurity = comparison.constituents().stream()
                .collect(Collectors.toMap(value -> value.securityId().value(), value -> value, (left, right) -> left, LinkedHashMap::new));

        List<ReviewComparisonItem> items = new java.util.ArrayList<>();
        Set<Integer> allSecurities = new java.util.TreeSet<>();
        allSecurities.addAll(baselineBySecurity.keySet());
        allSecurities.addAll(comparisonBySecurity.keySet());
        for (Integer securityId : allSecurities) {
            SelectedConstituent before = baselineBySecurity.get(securityId);
            SelectedConstituent after = comparisonBySecurity.get(securityId);
            ComparisonChange change;
            if (before == null) {
                change = ComparisonChange.JOINER;
            } else if (after == null) {
                change = ComparisonChange.LEAVER;
            } else {
                change = ComparisonChange.PRESENT;
            }
            items.add(new ReviewComparisonItem(securityId,
                    before == null ? null : before.rank(), after == null ? null : after.rank(),
                    before == null ? null : before.finalWeight(), after == null ? null : after.finalWeight(), change));
        }

        return new ReviewComparison(baseline.id(), comparison.id(), baseline.indexCode().value(), baseline.reviewPeriod(),
                comparison.reviewPeriod(), baseline.validationWarnings(), comparison.validationWarnings(), items,
                baseline.status().name(), comparison.status().name());
    }

    private ReviewResult load(Long id) {
        ReviewResultEntity entity = reviewResultRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review result not found: " + id));
        return reviewResultMapper.toDomain(entity);
    }

    public record ReviewComparison(Long baselineReviewResultId, Long comparisonReviewResultId, String indexCode,
                                   String baselineReviewPeriod, String comparisonReviewPeriod,
                                   Map<?, ?> baselineWarnings, Map<?, ?> comparisonWarnings,
                                   List<ReviewComparisonItem> items, String baselineStatus, String comparisonStatus) {
    }

    public enum ComparisonChange { JOINER, LEAVER, PRESENT }

    public record ReviewComparisonItem(Integer securityId, Integer baselineRank, Integer comparisonRank,
                                       BigDecimal baselineWeight, BigDecimal comparisonWeight,
                                       ComparisonChange change) {
    }
}



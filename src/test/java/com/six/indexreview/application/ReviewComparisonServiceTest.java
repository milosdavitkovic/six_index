package com.six.indexreview.application;

import com.six.indexreview.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewComparisonServiceTest {
    @Test
    void compareReportsJoinersLeaversAndRankMovements() {
        ReviewResult baseline = review(1L,
                constituent(103, 18, "0.0200000000"),
                constituent(177, 20, "0.0300000000"));
        ReviewResult comparison = review(2L,
                constituent(177, 12, "0.0400000000"),
                constituent(177, 12, "0.0400000000"),
                constituent(200, 12, "0.0400000000"));

        ReviewComparisonOrchestrator comparisonOrchestrator = new ReviewComparisonOrchestrator(null);
        ReviewComparisonReport report = comparisonOrchestrator.build(baseline, comparison);

        assertThat(report.joiners()).containsExactly(200);
        assertThat(report.items())
                .anySatisfy(item -> {
                    assertThat(item.securityId()).isEqualTo(200);
                    assertThat(item.change()).isEqualTo(ComparisonChange.JOINER);
                });
    }

    private SelectedConstituent constituent(int securityId, int rank, String weight) {
        return new SelectedConstituent(new SecurityId(securityId), rank, BigDecimal.TEN, false,
                new BigDecimal(weight), new BigDecimal(weight), BigDecimal.ZERO, DecisionType.UNCHANGED, "ok", false);
    }

    private ReviewResult review(Long id, SelectedConstituent... constituents) {
        return new ReviewResult(id, new IndexCode("SMI"), "Q3-2026", LocalDate.of(2026, Month.SEPTEMBER, 10), LocalDate.of(2026, Month.SEPTEMBER, 21),
                ReviewStatus.COMPLETED, Instant.parse("2026-09-21T00:00:00Z"), 10, constituents.length, Set.of(),
                List.of(constituents), List.of(), List.of(), List.of(), Map.of());
    }
}

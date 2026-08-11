package com.six.indexreview.application;

import com.six.indexreview.domain.model.*;
import com.six.indexreview.domain.repository.ReviewResultRepositoryPort;
import com.six.indexreview.infrastructure.persistence.entity.ReviewResultEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewComparisonServiceTest {
    @Mock
    private ReviewComparisonOrchestrator comparisonOrchestrator;

    @InjectMocks
    private ReviewComparisonService service;

    @Test
    void compareReportsJoinersLeaversAndRankMovements() {
        ReviewResult baseline = review(1L,
                constituent(103, 18, "0.0200000000"),
                constituent(177, 20, "0.0300000000"));
        ReviewResult comparison = review(2L,
                constituent(177, 12, "0.0400000000"),
                constituent(177, 12, "0.0400000000"),
                constituent(177, 12, "0.0400000000"));

        ReviewComparisonReport expected = new ReviewComparisonReport(1L, 2L, "SMI", "Q3-2026", "Q3-2026", Map.of(), Map.of(), List.of(), "COMPLETED", "COMPLETED", List.of(), List.of(), List.of(), List.of());
        when(comparisonOrchestrator.compare(1L, 2L)).thenReturn(expected);

        ReviewComparisonReport report = service.compare(1L, 2L);
        assertThat(report).isSameAs(expected);
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

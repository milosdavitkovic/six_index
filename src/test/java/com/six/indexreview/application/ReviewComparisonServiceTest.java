package com.six.indexreview.application;

import com.six.indexreview.domain.model.*;
import com.six.indexreview.infrastructure.persistence.mapper.ReviewResultMapper;
import com.six.indexreview.infrastructure.persistence.entity.ReviewResultEntity;
import com.six.indexreview.infrastructure.persistence.repository.ReviewResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewComparisonServiceTest {
    @Mock
    private ReviewResultRepository repository;

    @Mock
    private ReviewResultMapper mapper;

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

        ReviewResultEntity baselineEntity = ReviewResultEntity.builder().id(1L).indexCode("SMI").reviewPeriod("Q3-2026")
                .cutOffDate(LocalDate.of(2026, 9, 10)).reviewDate(LocalDate.of(2026, 9, 21)).status("COMPLETED")
                .createdAt(Instant.parse("2026-09-21T00:00:00Z")).totalEligible(10).totalSelected(2).build();
        ReviewResultEntity comparisonEntity = ReviewResultEntity.builder().id(2L).indexCode("SMI").reviewPeriod("Q3-2026")
                .cutOffDate(LocalDate.of(2026, 9, 10)).reviewDate(LocalDate.of(2026, 9, 21)).status("COMPLETED")
                .createdAt(Instant.parse("2026-09-21T00:00:00Z")).totalEligible(10).totalSelected(3).build();
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(baselineEntity));
        when(repository.findById(2L)).thenReturn(java.util.Optional.of(comparisonEntity));
        when(mapper.toDomain(baselineEntity)).thenReturn(baseline);
        when(mapper.toDomain(comparisonEntity)).thenReturn(comparison);

        ReviewComparisonService.ReviewComparison report = service.compare(1L, 2L);

        assertThat(report.items()).anySatisfy(item -> {
            if (item.securityId().equals(103)) {
                assertThat(item.change()).isEqualTo(ReviewComparisonService.ComparisonChange.LEAVER);
                assertThat(item.baselineRank()).isEqualTo(18);
                assertThat(item.comparisonRank()).isNull();
            }
        });
        assertThat(report.items()).anySatisfy(item -> {
            if (item.securityId().equals(177)) {
                assertThat(item.baselineRank()).isEqualTo(20);
                assertThat(item.comparisonRank()).isEqualTo(12);
                assertThat(item.change()).isEqualTo(ReviewComparisonService.ComparisonChange.PRESENT);
            }
        });
    }

    private SelectedConstituent constituent(int securityId, int rank, String weight) {
        return new SelectedConstituent(new SecurityId(securityId), rank, BigDecimal.TEN, false,
                new BigDecimal(weight), new BigDecimal(weight), BigDecimal.ZERO, DecisionType.UNCHANGED, "ok", false);
    }

    private ReviewResult review(Long id, SelectedConstituent... constituents) {
        return new ReviewResult(id, new IndexCode("SMI"), "Q3-2026", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 21),
                ReviewStatus.COMPLETED, Instant.parse("2026-09-21T00:00:00Z"), 10, constituents.length, Set.of(),
                List.of(constituents), List.of(), List.of(), List.of(), Map.of());
    }

}




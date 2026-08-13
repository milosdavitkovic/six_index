package com.six.indexreview.application;

import com.six.indexreview.api.dto.ReviewResponse;
import com.six.indexreview.application.exception.ResourceNotFoundException;
import com.six.indexreview.domain.model.IndexCode;
import com.six.indexreview.domain.model.ReviewResult;
import com.six.indexreview.domain.model.ReviewStatus;
import com.six.indexreview.domain.repository.AuditRepositoryPort;
import com.six.indexreview.domain.repository.ReviewResultRepositoryPort;
import com.six.indexreview.domain.service.PrecisionPolicy;
import com.six.indexreview.infrastructure.persistence.entity.ReviewResultEntity;
import com.six.indexreview.infrastructure.persistence.mapper.ReviewResultMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Milos Davitkovic
 */
@ExtendWith(MockitoExtension.class)
class IndexReviewServiceTest {
    @Mock
    private ReviewResultRepositoryPort reviewResultRepository;

    @Mock
    private ReviewResultMapper reviewResultMapper;

    @Mock
    private ReviewResultAssembler reviewResultAssembler;

    private IndexReviewService indexReviewService;

    @BeforeEach
    void setUp() {
        indexReviewService = new IndexReviewService(null, null, null, null,
                reviewResultRepository, reviewResultMapper, reviewResultAssembler, null, Clock.systemUTC());
    }

    @Test
    void latestCanonicalizesTheCallerProvidedReviewPeriod() {
        ReviewResult domain = new ReviewResult(7L, new IndexCode("SMI"), "Q3-2026",
                LocalDate.of(2026, Month.SEPTEMBER, 10), LocalDate.of(2026, Month.SEPTEMBER, 21), ReviewStatus.COMPLETED,
                Instant.parse("2026-08-11T00:00:00Z"), 204, 20, Set.of(), List.of(), List.of(), List.of(), List.of(), Map.of());
        ReviewResponse response = new ReviewResponse(7L, "SMI", "Q3-2026",
                LocalDate.of(2026, Month.SEPTEMBER, 10), LocalDate.of(2026, Month.SEPTEMBER, 21), "COMPLETED",
                Instant.parse("2026-08-11T00:00:00Z"), 204, 20,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

        when(reviewResultRepository.findTopByIndexCodeIgnoreCaseAndReviewPeriodIgnoreCaseOrderByCreatedAtDescIdDesc("SMI", "Q3-2026"))
                .thenReturn(java.util.Optional.of(domain));
        when(reviewResultAssembler.toResponse(domain)).thenReturn(response);

        ReviewResponse latest = indexReviewService.latest("SMI", "q3-2026");

        assertThat(latest.reviewResultId()).isEqualTo(7L);
        assertThat(latest.reviewPeriod()).isEqualTo("Q3-2026");
        verify(reviewResultRepository).findTopByIndexCodeIgnoreCaseAndReviewPeriodIgnoreCaseOrderByCreatedAtDescIdDesc("SMI", "Q3-2026");
    }

    @Test
    void latestStillThrowsWhenNoMatchingResultExists() {
        when(reviewResultRepository.findTopByIndexCodeIgnoreCaseAndReviewPeriodIgnoreCaseOrderByCreatedAtDescIdDesc("SMI", "Q3-2026"))
                .thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> indexReviewService.latest("SMI", "q3-2026"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("SMI/q3-2026");
    }
}

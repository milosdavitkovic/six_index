package com.six.indexreview.infrastructure.persistence.mapper;

import com.six.indexreview.domain.model.SecurityId;
import com.six.indexreview.infrastructure.persistence.entity.ReviewResultEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Milos Davitkovic
 */
class ReviewResultMapperTest {
    private final ReviewResultMapper mapper = new ReviewResultMapper();

    @Test
    void restoresPersistedValidationWarnings() {
        ReviewResultEntity entity = ReviewResultEntity.builder()
                .id(7L)
                .indexCode("SMI")
                .reviewPeriod("Q3-2026")
                .cutOffDate(LocalDate.of(2026, 9, 10))
                .reviewDate(LocalDate.of(2026, 9, 21))
                .status("COMPLETED_WITH_WARNINGS")
                .createdAt(Instant.parse("2026-09-21T00:00:00Z"))
                .totalEligible(204)
                .totalSelected(20)
                .build();
        entity.addValidationWarning(166, "Missing or non-positive review-date shares");

        var result = mapper.toDomain(entity);

        assertThat(result.status().name()).isEqualTo("COMPLETED_WITH_WARNINGS");
        assertThat(result.validationWarnings())
                .containsEntry(new SecurityId(166), "Missing or non-positive review-date shares");
    }

    @Test
    void restoresPersistedRejectionEvidenceAlongsideWarnings() {
        ReviewResultEntity entity = ReviewResultEntity.builder()
                .id(8L).indexCode("SMI").reviewPeriod("Q3-2026")
                .cutOffDate(LocalDate.of(2026, 9, 10)).reviewDate(LocalDate.of(2026, 9, 21))
                .status("COMPLETED_WITH_WARNINGS").createdAt(Instant.parse("2026-09-21T00:00:00Z"))
                .totalEligible(204).totalSelected(20).build();
        entity.addDecision(new com.six.indexreview.infrastructure.persistence.entity.ReviewDecisionEntity(
                166, "REJECTED", "Missing review-date market data"));
        entity.addValidationWarning(166, "Missing or non-positive review-date shares");

        var result = mapper.toDomain(entity);

        assertThat(result.rejectedSecurities())
                .extracting(value -> value.securityId().value(), value -> value.reason())
                .containsExactly(org.assertj.core.groups.Tuple.tuple(166, "Missing review-date market data"));
        assertThat(result.validationWarnings()).containsKey(new SecurityId(166));
    }
}

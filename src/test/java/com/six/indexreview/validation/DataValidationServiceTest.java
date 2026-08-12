package com.six.indexreview.validation;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.engine.ReviewDataSnapshot;
import com.six.indexreview.domain.model.*;
import com.six.indexreview.domain.rule.RuleTestFixtures;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Milos Davitkovic
 */
class DataValidationServiceTest {
    @Test
    void reportsMissingReviewDataAsWarningSoSecurityCanBeAuditedAsRejected() {
        SecurityId id = new SecurityId(1);
        IndexDefinition definition = new IndexDefinition(new IndexCode("SMI"), "Test", 1, BigDecimal.ONE,
                "FFMCAP", "TOP_N", "NONE", "Q3-2026",
                new ReviewDates(LocalDate.of(2026, Month.SEPTEMBER, 10), LocalDate.of(2026, Month.SEPTEMBER, 21)),
                Set.of().stream().map(Object::toString).toList(), true, 0);
        IndexReviewContext context = new IndexReviewContext(definition,
                new ReviewDataSnapshot(Set.of(id), Map.of(id, new MarketData(id, definition.reviewDates().cutOffDate(),
                        BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE)), Map.of(), Set.of(id)),
                RuleTestFixtures.precision(), Instant.EPOCH);

        var errors = new DataValidationService().validate(context);

        assertThat(errors).isNotEmpty().allMatch(value -> value.severity() == ValidationSeverity.WARNING);
    }
}

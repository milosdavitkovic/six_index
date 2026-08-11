package com.six.indexreview;

import com.six.indexreview.api.dto.HealthResponse;
import com.six.indexreview.api.dto.ImportResponse;
import com.six.indexreview.application.exception.ReviewExecutionException;
import com.six.indexreview.domain.engine.RuleExecutionResult;
import com.six.indexreview.domain.model.*;
import com.six.indexreview.reporting.ReportFormat;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoverageBasicsTest {
    @Test
    void valueObjectsExposeTheirComponentsAndPreserveDeterministicBehavior() {
        assertThat(new HealthResponse("UP", "six-index").status()).isEqualTo("UP");
        assertThat(new ImportResponse("SPI_UNIVERSE", 3, 2, 1).storedRows()).isEqualTo(2);
        assertThat(new RuleExecutionResult("R1", 4).auditEventCount()).isEqualTo(4);
        assertThat(new ConstituentWeight(new SecurityId(7), new BigDecimal("0.25"), new BigDecimal("0.20"), new BigDecimal("0.80"), true).capped()).isTrue();
        assertThat(new ReviewSchedule("Q3-2026", new ReviewDates(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 21))).reviewPeriod()).isEqualTo("Q3-2026");
        assertThat(new Security(new SecurityId(42)).securityId()).isEqualTo(new SecurityId(42));
        assertThat(ReportFormat.JSON.name()).isEqualTo("JSON");
    }

    @Test
    void currentCompositionCopiesInputAndRejectsInvalidDates() {
        var members = new LinkedHashSet<>(java.util.Set.of(new SecurityId(1), new SecurityId(2)));
        var composition = new CurrentComposition(members);

        members.add(new SecurityId(3));
        assertThat(composition.members()).containsExactlyInAnyOrder(new SecurityId(1), new SecurityId(2));

        assertThatThrownBy(() -> new ReviewDates(LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cut-off date must be before review date");
    }

    @Test
    void reviewExecutionExceptionPreservesMessageAndCause() {
        var cause = new IllegalStateException("boom");
        var exception = new ReviewExecutionException("failed", cause);

        assertThat(exception).hasMessage("failed");
        assertThat(exception.getCause()).isSameAs(cause);
    }
}

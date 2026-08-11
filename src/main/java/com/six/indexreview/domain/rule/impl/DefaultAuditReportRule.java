package com.six.indexreview.domain.rule.impl;

import com.six.indexreview.application.exception.ReviewValidationException;
import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.rule.AuditReportRule;
import com.six.indexreview.validation.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Core DefaultAuditReportRule component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
@Slf4j
@Component
public class DefaultAuditReportRule implements AuditReportRule {
    @Override
    public String code() {
        return "GENERATE_AUDIT_TRAIL";
    }

    @Override
    public String description() {
        return "Validate completed output and append an auditable review summary";
    }

    @Override
    public IndexReviewContext apply(IndexReviewContext context) {
        log.info("Starting rule {}", code());
        BigDecimal sum = context.selectedConstituents().stream()
                .map(value -> value.finalWeight() == null ? BigDecimal.ZERO : value.finalWeight())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expected = context.precisionPolicy().output(BigDecimal.ONE);
        if (sum.compareTo(expected) != 0) {
            ValidationError error = ValidationError.error("WEIGHT_SUM_DRIFT", "finalWeights",
                    "Final weights sum to " + sum + " instead of " + expected);
            throw new ReviewValidationException("Final weight validation failed", List.of(error));
        }
        for (var constituent : context.selectedConstituents()) {
            if (constituent.finalWeight().compareTo(context.precisionPolicy().output(context.definition().maxWeight())) > 0) {
                ValidationError error = ValidationError.error("WEIGHT_CAP_BREACH", "finalWeight",
                        "Final weight exceeds configured cap", constituent.securityId());
                throw new ReviewValidationException("Final weight validation failed", List.of(error));
            }
        }
        long joiners = context.decisions().stream().filter(value -> value.decisionType().name().equals("JOINER")).count();
        long leavers = context.decisions().stream().filter(value -> value.decisionType().name().equals("LEAVER")).count();
        context.audit(code(), "Audit trail generated; final weights validated.",
                "eligible=" + context.eligibleSecurities().size() + ",selected=" + context.selectedConstituents().size(),
                "weightSum=" + sum + ",joiners=" + joiners + ",leavers=" + leavers);
        log.info("Completed rule {} auditEvents={} weightSum={}", code(), context.auditEvents().size(), sum);
        return context;
    }
}

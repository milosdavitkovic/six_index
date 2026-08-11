package com.six.indexreview.domain.rule.impl;

import com.six.indexreview.application.exception.ReviewValidationException;
import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.rule.WeightCalculationRule;
import com.six.indexreview.domain.service.WeightingStrategy;
import com.six.indexreview.validation.ValidationError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Calculates raw constituent weights from selected FFMCAP values.
 *
 * The rule remains methodology-specific so alternative weighting schemes can
 * be added later without changing the capping step or review flow.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FfmcapWeightCalculationRule implements WeightCalculationRule {
    private final WeightingStrategy weightCalculator;

    @Override
    public String code() {
        return "CALCULATE_RAW_WEIGHTS";
    }

    @Override
    public String description() {
        return "Calculate each selected constituent's FFMCAP divided by total selected FFMCAP";
    }

    @Override
    public IndexReviewContext apply(IndexReviewContext context) {
        log.info("Starting rule {} selected={}", code(), context.selectedConstituents().size());
        try {
            // The raw weight is the uncapped share of selected FFMCAP against
            // the total selected FFMCAP, which provides the starting point for
            // all later capping and redistribution logic.
            context.replaceSelected(weightCalculator.calculate(context.selectedConstituents()));
        } catch (IllegalArgumentException exception) {
            throw new ReviewValidationException("Selected constituent weight input is invalid", java.util.List.of(
                    ValidationError.error("INVALID_SELECTED_FFMCAP", "selectedConstituents", exception.getMessage())));
        }
        context.selectedConstituents().forEach(value -> {
            log.debug("Raw weight securityId={} ffmcap={} rawWeight={}", value.securityId(), value.ffmcap(), value.rawWeight());
            context.audit(code(), value.securityId(), "Raw weight calculated as FFMCAP divided by selected FFMCAP total.",
                    value.ffmcap().toPlainString(), value.rawWeight().toPlainString());
        });
        return context;
    }
}

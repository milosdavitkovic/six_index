package com.six.indexreview.domain.rule.impl;

import com.six.indexreview.application.exception.ReviewValidationException;
import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.model.EligibleSecurity;
import com.six.indexreview.domain.model.MarketData;
import com.six.indexreview.domain.model.RejectedSecurity;
import com.six.indexreview.domain.model.SecurityId;
import com.six.indexreview.domain.rule.ReviewRule;
import com.six.indexreview.domain.service.FreeFloatMarketCapCalculator;
import com.six.indexreview.validation.ValidationError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Core SpiUniverseEligibilityRule component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpiUniverseEligibilityRule implements ReviewRule {
    private final FreeFloatMarketCapCalculator ffmcapCalculator;

    @Override
    public String code() {
        return "BUILD_ELIGIBLE_UNIVERSE";
    }

    @Override
    public String description() {
        return "Keep securities in the configured SPI universe with valid required market data";
    }

    @Override
    public IndexReviewContext apply(IndexReviewContext context) {
        log.info("Starting rule {} universeSize={}", code(), context.universe().size());
        List<EligibleSecurity> eligible = new ArrayList<>();
        for (SecurityId id : context.universe()) {
            MarketData cutOff = context.cutOffMarketData().get(id);
            MarketData review = context.reviewMarketData().get(id);
            String rejection = rejectionReason(cutOff, review);
            if (rejection != null) {
                context.rejectedSecurities().add(new RejectedSecurity(id, rejection));
                context.audit(code(), id, rejection, marketDataSummary(cutOff, review), "REJECTED");
                continue;
            }
            boolean current = context.currentComposition().contains(id);
            eligible.add(new EligibleSecurity(id, cutOff.price(), review.shares(), review.freeFloat(), null, current));
            context.audit(code(), id, "Security passed eligibility checks.", marketDataSummary(cutOff, review), "ELIGIBLE");
        }
        context.replaceEligible(eligible);
        log.info("Completed rule {} eligible={} rejected={}", code(), eligible.size(), context.rejectedSecurities().size());
        if (eligible.size() < context.definition().constituentCount()) {
            ValidationError error = ValidationError.error("ELIGIBLE_UNIVERSE_TOO_SMALL", "eligibleUniverse",
                    "Only " + eligible.size() + " eligible securities are available; "
                            + context.definition().constituentCount() + " are required");
            context.addValidationErrors(List.of(error));
            throw new ReviewValidationException("Eligible universe is too small", List.of(error));
        }
        return context;
    }

    private String rejectionReason(MarketData cutOff, MarketData review) {
        if (cutOff == null || cutOff.price() == null) {
            return "Missing price on cut-off date.";
        }
        if (cutOff.price().compareTo(BigDecimal.ZERO) <= 0) {
            return "Price on cut-off date must be positive.";
        }
        if (review == null || review.shares() == null) {
            return "Missing shares on review date.";
        }
        if (review.shares().compareTo(BigDecimal.ZERO) <= 0) {
            return "Shares on review date must be positive.";
        }
        if (review.freeFloat() == null) {
            return "Missing free float on review date.";
        }
        if (review.freeFloat().compareTo(BigDecimal.ZERO) < 0 || review.freeFloat().compareTo(BigDecimal.ONE) > 0) {
            return "Free float on review date must be between 0 and 1.";
        }
        return null;
    }

    private String marketDataSummary(MarketData cutOff, MarketData review) {
        return "cutOffPrice=" + (cutOff == null ? null : cutOff.price())
                + ",reviewShares=" + (review == null ? null : review.shares())
                + ",reviewFreeFloat=" + (review == null ? null : review.freeFloat());
    }
}

package com.six.indexreview.validation;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.model.MarketData;
import com.six.indexreview.domain.model.SecurityId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Core DataValidationService component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
@Slf4j
@Service
public class DataValidationService {

    public List<ValidationError> validate(IndexReviewContext context) {
        return doValidate(context);
    }

    private List<ValidationError> doValidate(IndexReviewContext context) {
        Objects.requireNonNull(context, "context must not be null");
        List<ValidationError> errors = new ArrayList<>();
        validateCollections(context, errors);
        validateMarketData(context, errors);
        return errors;
    }

    private void validateCollections(IndexReviewContext context, List<ValidationError> errors) {
        if (context.universe().isEmpty()) {
            errors.add(ValidationError.error("UNIVERSE_MISSING", "spiUniverse",
                    "No SPI universe rows were imported for the review date"));
        }
        if (context.currentComposition().isEmpty()) {
            errors.add(ValidationError.error("COMPOSITION_MISSING", "composition",
                    "No current composition rows were imported"));
        }
        if (context.currentComposition().size() != context.definition().constituentCount()) {
            errors.add(ValidationError.error("COMPOSITION_SIZE", "composition",
                    "Current composition contains " + context.currentComposition().size()
                            + " members; expected " + context.definition().constituentCount()));
        }
        if (context.universe().size() < context.definition().constituentCount()) {
            errors.add(ValidationError.error("UNIVERSE_TOO_SMALL", "spiUniverse",
                    "Universe contains fewer securities than the configured constituent count"));
        }
    }

    private void validateMarketData(IndexReviewContext context, List<ValidationError> errors) {

        for (SecurityId id : context.universe()) {
            validateCutOff(context.cutOffMarketData().get(id), id, errors);
            validateReview(context.reviewMarketData().get(id), id, errors);
        }
    }

    private void validateCutOff(MarketData cutOff, SecurityId id, List<ValidationError> errors) {
        BigDecimal cutOffPrice = cutOff == null ? null : cutOff.price();
        if (cutOffPrice == null || cutOffPrice.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Security {} has no valid cut-off price; it will be rejected", id);
            errors.add(ValidationError.warning("MISSING_CUT_OFF_PRICE", "price", "Missing or non-positive cut-off price", id));
        }
    }

    private void validateReview(MarketData review, SecurityId id, List<ValidationError> errors) {
        BigDecimal reviewShares = review == null ? null : review.shares();
        BigDecimal reviewFreeFloat = review == null ? null : review.freeFloat();
        if (reviewShares == null || reviewShares.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Security {} has no valid review-date shares; it will be rejected", id);
            errors.add(ValidationError.warning("MISSING_REVIEW_SHARES", "shares", "Missing or non-positive review-date shares", id));
        }
        if (reviewFreeFloat == null || reviewFreeFloat.compareTo(BigDecimal.ZERO) < 0 || reviewFreeFloat.compareTo(BigDecimal.ONE) > 0) {
            log.warn("Security {} has an invalid review-date free float; it will be rejected", id);
            errors.add(ValidationError.warning("INVALID_REVIEW_FREE_FLOAT", "freeFloat",
                    "Review-date free float must be present and between 0 and 1", id));
        }
    }
}

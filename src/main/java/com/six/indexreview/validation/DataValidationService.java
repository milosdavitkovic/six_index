package com.six.indexreview.validation;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.model.MarketData;
import com.six.indexreview.domain.model.SecurityId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DataValidationService {

    public List<ValidationError> validate(IndexReviewContext context) {
        List<ValidationError> errors = new ArrayList<>();
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

        for (SecurityId id : context.universe()) {
            MarketData cutOff = context.cutOffMarketData().get(id);
            MarketData review = context.reviewMarketData().get(id);
            if (cutOff == null || cutOff.price() == null || cutOff.price().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Security {} has no valid cut-off price; it will be rejected", id);
                errors.add(ValidationError.warning("MISSING_CUT_OFF_PRICE", "price", "Missing or non-positive cut-off price", id));
            }
            if (review == null || review.shares() == null || review.shares().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Security {} has no valid review-date shares; it will be rejected", id);
                errors.add(ValidationError.warning("MISSING_REVIEW_SHARES", "shares", "Missing or non-positive review-date shares", id));
            }
            if (review == null || review.freeFloat() == null || review.freeFloat().compareTo(BigDecimal.ZERO) < 0
                    || review.freeFloat().compareTo(BigDecimal.ONE) > 0) {
                log.warn("Security {} has an invalid review-date free float; it will be rejected", id);
                errors.add(ValidationError.warning("INVALID_REVIEW_FREE_FLOAT", "freeFloat",
                        "Review-date free float must be present and between 0 and 1", id));
            }
        }
        return errors;
    }
}

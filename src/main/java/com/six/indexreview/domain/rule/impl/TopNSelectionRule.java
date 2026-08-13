package com.six.indexreview.domain.rule.impl;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.rule.SelectionRule;
import com.six.indexreview.domain.service.ConstituentSelector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Applies the configured top-N constituent selection rule.
 *
 * Selection is intentionally separate from ranking so alternative index
 * families can reuse the same ranking logic with different thresholds.
 */
/**
 * @author Milos Davitkovic
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TopNSelectionRule implements SelectionRule {
    private final ConstituentSelector selector;

    @Override
    public String code() {
        return "APPLY_TOP_N_SELECTION";
    }

    @Override
    public String description() {
        return "Select the configured number of highest-ranked securities";
    }

    @Override
    public IndexReviewContext apply(IndexReviewContext context) {
        log.info("Starting rule {} constituentCount={}", code(), context.definition().methodology().constituentCount());
        if (!"TOP_N".equalsIgnoreCase(context.definition().methodology().selectionRule())
                && !"TOP_N_WITH_BUFFER".equalsIgnoreCase(context.definition().methodology().selectionRule())) {
            throw new IllegalArgumentException("Unsupported selection rule: " + context.definition().methodology().selectionRule());
        }
        // The ranked list is already deterministic; truncation preserves that
        // order and therefore the final selected set.
        int constituentCount = context.definition().methodology().constituentCount();
        context.replaceSelected(selector.select(context.rankedSecurities(), constituentCount));
        context.selectedConstituents().forEach(value -> context.audit(code(), value.securityId(),
                "Security selected by top-N rule.", "rank=" + value.rank(), "SELECTED"));
        context.rankedSecurities().stream()
                .filter(value -> value.rank() > constituentCount)
                .forEach(value -> context.audit(code(), value.securityId(),
                        "Security excluded by top-N rule because its rank is outside the selected range.",
                        "rank=" + value.rank() + ",constituentCount=" + constituentCount, "NOT_SELECTED"));
        log.info("Completed rule {} selected={}", code(), context.selectedConstituents().size());
        return context;
    }
}

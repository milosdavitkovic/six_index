package com.six.indexreview.domain.rule.impl;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.rule.SelectionRule;
import com.six.indexreview.domain.service.ConstituentSelector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        log.info("Starting rule {} constituentCount={}", code(), context.definition().constituentCount());
        if (!"TOP_N".equalsIgnoreCase(context.definition().selectionRule())
                && !"TOP_N_WITH_BUFFER".equalsIgnoreCase(context.definition().selectionRule())) {
            throw new IllegalArgumentException("Unsupported selection rule: " + context.definition().selectionRule());
        }
        context.replaceSelected(selector.select(context.rankedSecurities(), context.definition().constituentCount()));
        context.selectedConstituents().forEach(value -> context.audit(code(), value.securityId(),
                "Security selected by top-N rule.", "rank=" + value.rank(), "SELECTED"));
        log.info("Completed rule {} selected={}", code(), context.selectedConstituents().size());
        return context;
    }
}

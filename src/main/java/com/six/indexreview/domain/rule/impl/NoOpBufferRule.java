package com.six.indexreview.domain.rule.impl;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.rule.BufferRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Disables buffer retention for methodologies that require pure top-N
 * selection.
 *
 * This rule exists so the engine can switch buffer behavior by configuration
 * instead of branching in the orchestration layer.
 */
/**
 * @author Milos Davitkovic
 */
@Slf4j
@Component("noOpBufferRule")
public class NoOpBufferRule implements BufferRule {
    @Override
    public String code() {
        return "BUFFER_NONE";
    }

    @Override
    public String description() {
        return "Do not retain current constituents outside the pure top-N selection";
    }

    @Override
    public IndexReviewContext apply(IndexReviewContext context) {
        // Explicitly record the absence of buffer retention so the audit trail
        // can explain why a current constituent was not kept.
        log.warn("Buffer rule configured as NONE. Pure top-N selection applied.");
        context.audit(code(), "Buffer rule configured as NONE. Pure top-N selection applied.",
                context.definition().selectionRule(), "NO_BUFFER");
        return context;
    }
}

package com.six.indexreview.domain.rule.impl;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.rule.BufferRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        log.warn("Buffer rule configured as NONE. Pure top-N selection applied.");
        context.audit(code(), "Buffer rule configured as NONE. Pure top-N selection applied.",
                context.definition().selectionRule(), "NO_BUFFER");
        return context;
    }
}

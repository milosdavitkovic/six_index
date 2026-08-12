package com.six.indexreview.domain.rule;

import com.six.indexreview.domain.engine.IndexReviewContext;

/**
 * Strategy contract for ReviewRule in the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public interface ReviewRule {
    String code();

    String description();

    IndexReviewContext apply(IndexReviewContext context);
}

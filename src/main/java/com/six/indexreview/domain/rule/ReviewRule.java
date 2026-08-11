package com.six.indexreview.domain.rule;

import com.six.indexreview.domain.engine.IndexReviewContext;

public interface ReviewRule {
    String code();

    String description();

    IndexReviewContext apply(IndexReviewContext context);
}

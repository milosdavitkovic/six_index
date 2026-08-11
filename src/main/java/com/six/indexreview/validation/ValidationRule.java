package com.six.indexreview.validation;

import com.six.indexreview.domain.engine.IndexReviewContext;

import java.util.List;

public interface ValidationRule {
    List<ValidationError> validate(IndexReviewContext context);
}

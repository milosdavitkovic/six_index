package com.six.indexreview.validation;

import com.six.indexreview.domain.engine.IndexReviewContext;

import java.util.List;

/**
 * Strategy contract for pre-review validation.
 *
 * Additional validation families can be added without changing the review
 * engine, keeping input quality checks separate from methodology logic.
 */
public interface ValidationRule {
    List<ValidationError> validate(IndexReviewContext context);
}

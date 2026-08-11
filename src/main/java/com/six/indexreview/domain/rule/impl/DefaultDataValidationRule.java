package com.six.indexreview.domain.rule.impl;

import com.six.indexreview.application.exception.ReviewValidationException;
import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.rule.ReviewRule;
import com.six.indexreview.validation.DataValidationService;
import com.six.indexreview.validation.ValidationError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultDataValidationRule implements ReviewRule {
    private final DataValidationService dataValidationService;

    @Override
    public String code() {
        return "VALIDATE_INPUT_DATA";
    }

    @Override
    public String description() {
        return "Validate imported data, configuration and current composition before calculation";
    }

    @Override
    public IndexReviewContext apply(IndexReviewContext context) {
        log.info("Starting rule {}", code());
        List<ValidationError> errors = dataValidationService.validate(context);
        context.addValidationErrors(errors.stream().filter(error -> error.severity().name().equals("ERROR")).toList());
        errors.stream().filter(error -> error.severity().name().equals("WARNING"))
                .forEach(context::addValidationWarning);
        List<ValidationError> blockingErrors = errors.stream()
                .filter(error -> error.severity().name().equals("ERROR")).toList();
        if (!blockingErrors.isEmpty()) {
            throw new ReviewValidationException("Input validation failed", blockingErrors);
        }
        context.audit(code(), "Input validation passed", Integer.toString(context.universe().size()), "VALID");
        log.info("Completed rule {}", code());
        return context;
    }
}

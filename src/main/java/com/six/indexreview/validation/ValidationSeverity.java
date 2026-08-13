package com.six.indexreview.validation;

/**
 * Severity assigned to validation findings.
 *
 * ERROR stops review execution, while WARNING allows the review to continue
 * with the affected security or input flagged for downstream handling.
 * @author Milos Davitkovic
 */
public enum ValidationSeverity {
    ERROR,
    WARNING
}

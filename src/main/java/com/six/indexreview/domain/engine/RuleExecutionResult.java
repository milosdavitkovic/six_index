package com.six.indexreview.domain.engine;

/**
 * Immutable data carrier for RuleExecutionResult.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record RuleExecutionResult(String ruleCode, int auditEventCount) {
}

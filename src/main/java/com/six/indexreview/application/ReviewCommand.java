package com.six.indexreview.application;

/**
 * Immutable data carrier for ReviewCommand.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public record ReviewCommand(String indexCode, String reviewPeriod) {
}

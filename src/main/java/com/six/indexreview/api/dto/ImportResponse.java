package com.six.indexreview.api.dto;

public record ImportResponse(String dataset, int inputRows, int storedRows, int deduplicatedRows) {
}

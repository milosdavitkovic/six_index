package com.six.indexreview.domain.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable data carrier for SecurityId.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public record SecurityId(int value) implements Comparable<SecurityId>, Serializable {

    public SecurityId {
        if (value <= 0) {
            throw new IllegalArgumentException("Security ID must be positive: " + value);
        }
    }

    public static SecurityId of(String value) {
        try {
            return new SecurityId(Integer.parseInt(Objects.requireNonNull(value, "value must not be null").trim()));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid security ID: " + value, exception);
        }
    }

    @Override
    public int compareTo(SecurityId other) {
        return Integer.compare(value, other.value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}

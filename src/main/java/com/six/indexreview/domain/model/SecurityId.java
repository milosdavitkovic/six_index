package com.six.indexreview.domain.model;

import java.io.Serializable;

public record SecurityId(int value) implements Comparable<SecurityId>, Serializable {

    public SecurityId {
        if (value <= 0) {
            throw new IllegalArgumentException("Security ID must be positive: " + value);
        }
    }

    public static SecurityId of(String value) {
        try {
            return new SecurityId(Integer.parseInt(value.trim()));
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

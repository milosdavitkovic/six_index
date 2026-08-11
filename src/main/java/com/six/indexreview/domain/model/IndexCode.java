package com.six.indexreview.domain.model;

import java.io.Serializable;
import java.util.Locale;

public record IndexCode(String value) implements Serializable {

    public IndexCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Index code must not be blank");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
    }

    public static IndexCode of(String value) {
        return new IndexCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

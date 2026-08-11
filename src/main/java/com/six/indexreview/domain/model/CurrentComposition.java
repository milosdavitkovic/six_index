package com.six.indexreview.domain.model;

import java.util.Set;

public record CurrentComposition(Set<SecurityId> members) {
    public CurrentComposition {
        members = members == null ? Set.of() : Set.copyOf(members);
    }
}

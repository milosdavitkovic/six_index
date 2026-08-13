package com.six.indexreview.domain.model;

import java.util.Set;

/**
 * Immutable data carrier for CurrentComposition.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record CurrentComposition(Set<SecurityId> members) {
    public CurrentComposition {
        members = members == null ? Set.of() : Set.copyOf(members);
    }
}

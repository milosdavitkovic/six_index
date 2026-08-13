package com.six.indexreview.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;

/**
 * Immutable data carrier for ReviewResult.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record ReviewResult(
        Long id,
        IndexCode indexCode,
        String reviewPeriod,
        LocalDate cutOffDate,
        LocalDate reviewDate,
        ReviewStatus status,
        Instant createdAt,
        int totalEligibleSecurities,
        int totalSelectedConstituents,
        Set<SecurityId> currentMembers,
        List<SelectedConstituent> constituents,
        List<ReviewDecision> decisions,
        List<RejectedSecurity> rejectedSecurities,
        List<AuditEvent> auditEvents,
        Map<SecurityId, String> validationWarnings) {

    public ReviewResult {
        Objects.requireNonNull(indexCode, "indexCode must not be null");
        Objects.requireNonNull(reviewPeriod, "reviewPeriod must not be null");
        Objects.requireNonNull(cutOffDate, "cutOffDate must not be null");
        Objects.requireNonNull(reviewDate, "reviewDate must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        currentMembers = currentMembers == null ? Set.of() : Set.copyOf(currentMembers);
        constituents = constituents == null ? List.of() : List.copyOf(constituents);
        decisions = decisions == null ? List.of() : List.copyOf(decisions);
        rejectedSecurities = rejectedSecurities == null ? List.of() : List.copyOf(rejectedSecurities);
        auditEvents = auditEvents == null ? List.of() : List.copyOf(auditEvents);
        validationWarnings = validationWarnings == null ? Map.of() : Map.copyOf(validationWarnings);
    }
}

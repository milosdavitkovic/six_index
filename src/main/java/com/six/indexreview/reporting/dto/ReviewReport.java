package com.six.indexreview.reporting.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Immutable data carrier for ReviewReport.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public record ReviewReport(
        Long reviewResultId,
        String indexCode,
        String reviewPeriod,
        LocalDate cutOffDate,
        LocalDate reviewDate,
        String status,
        Instant createdAt,
        int totalEligibleSecurities,
        int totalSelectedConstituents,
        List<Integer> currentMembers,
        List<ReportConstituent> constituents,
        List<ReportDecision> decisions,
        List<Integer> joiners,
        List<Integer> leavers,
        List<Integer> unchanged,
        List<Integer> notSelected,
        List<ReportRejectedSecurity> rejectedSecurities,
        List<ReportAuditEntry> auditEvents) {
}

package com.six.indexreview.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ReviewResponse(
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
        List<ConstituentResponse> constituents,
        List<DecisionResponse> decisions,
        List<Integer> joiners,
        List<Integer> leavers,
        List<Integer> unchanged,
        List<Integer> notSelected,
        List<RejectedSecurityResponse> rejectedSecurities,
        List<AuditEventResponse> auditEvents) {
}

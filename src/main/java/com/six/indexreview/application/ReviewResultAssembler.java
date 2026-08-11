package com.six.indexreview.application;

import com.six.indexreview.api.dto.*;
import com.six.indexreview.reporting.ReviewReportGenerator;
import com.six.indexreview.reporting.dto.ReviewReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Core ReviewResultAssembler component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
@Component
@RequiredArgsConstructor
public class ReviewResultAssembler {
    private final ReviewReportGenerator reportGenerator;

    public ReviewResponse toResponse(com.six.indexreview.domain.model.ReviewResult result) {
        ReviewReport report = reportGenerator.generate(result);
        return new ReviewResponse(report.reviewResultId(), report.indexCode(), report.reviewPeriod(), report.cutOffDate(),
                report.reviewDate(), report.status(), report.createdAt(), report.totalEligibleSecurities(),
                report.totalSelectedConstituents(), report.currentMembers(),
                report.constituents().stream().map(value -> new ConstituentResponse(value.securityId(), value.rank(), value.ffmcap(),
                        value.rawWeight(), value.finalWeight(), value.cappingFactor(), value.decisionType().name(),
                        value.decisionReason(), value.capped())).toList(),
                report.decisions().stream().map(value -> new DecisionResponse(value.securityId(), value.decisionType().name(), value.reason())).toList(),
                report.joiners(), report.leavers(), report.unchanged(), report.notSelected(),
                report.rejectedSecurities().stream().map(value -> new RejectedSecurityResponse(value.securityId(), value.reason())).toList(),
                report.auditEvents().stream().map(value -> new AuditEventResponse(value.timestamp(), value.ruleCode(), value.securityId(),
                        value.message(), value.inputValue(), value.outputValue())).toList());
    }
}

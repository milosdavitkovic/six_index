package com.six.indexreview.reporting;

import com.six.indexreview.domain.model.ReviewResult;
import com.six.indexreview.reporting.dto.*;
import org.springframework.stereotype.Component;

import java.util.Comparator;

/** Builds the review report DTO from the persisted review result. The
 * generator isolates reporting concerns so future export formats or SMIM
 * fields can be added with minimal impact on the review engine. */
/**
 * @author Milos Davitkovic
 */
@Component
public class ReviewReportGenerator {
    public ReviewReport generate(ReviewResult result) {
        // Keep exported data sorted so review diffs stay stable and easy to
        // compare across repeated runs.
        var decisions = result.decisions().stream()
                .sorted(Comparator.comparing(value -> value.securityId().value()))
                .map(value -> new ReportDecision(value.securityId().value(), value.decisionType(), value.reason()))
                .toList();
        return new ReviewReport(
                result.id(), result.indexCode().value(), result.reviewPeriod(), result.cutOffDate(), result.reviewDate(),
                result.status().name(), result.createdAt(), result.totalEligibleSecurities(), result.totalSelectedConstituents(),
                result.currentMembers().stream().map(com.six.indexreview.domain.model.SecurityId::value).sorted().toList(),
                result.constituents().stream().sorted(Comparator.comparingInt(com.six.indexreview.domain.model.SelectedConstituent::rank))
                        .map(value -> new ReportConstituent(value.securityId().value(), value.rank(), value.ffmcap(),
                                value.rawWeight(), value.finalWeight(), value.cappingFactor(), value.decisionType(),
                                value.decisionReason(), value.capped())).toList(),
                decisions,
                result.decisions().stream().filter(value -> value.decisionType().name().equals("JOINER"))
                        .map(value -> value.securityId().value()).sorted().toList(),
                result.decisions().stream().filter(value -> value.decisionType().name().equals("LEAVER"))
                        .map(value -> value.securityId().value()).sorted().toList(),
                result.decisions().stream().filter(value -> value.decisionType().name().equals("UNCHANGED"))
                        .map(value -> value.securityId().value()).sorted().toList(),
                result.decisions().stream().filter(value -> value.decisionType().name().equals("NOT_SELECTED"))
                        .map(value -> value.securityId().value()).sorted().toList(),
                result.rejectedSecurities().stream().sorted(Comparator.comparing(value -> value.securityId().value()))
                        .map(value -> new ReportRejectedSecurity(value.securityId().value(), value.reason())).toList(),
                result.auditEvents().stream().map(value -> new ReportAuditEntry(value.timestamp(), value.ruleCode(),
                        value.securityId() == null ? null : value.securityId().value(), value.message(),
                        value.inputValue(), value.outputValue())).toList());
    }
}

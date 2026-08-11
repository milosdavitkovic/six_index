package com.six.indexreview.infrastructure.persistence.mapper;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.model.*;
import com.six.indexreview.infrastructure.persistence.entity.AuditEventEntity;
import com.six.indexreview.infrastructure.persistence.entity.ReviewDecisionEntity;
import com.six.indexreview.infrastructure.persistence.entity.ReviewResultConstituentEntity;
import com.six.indexreview.infrastructure.persistence.entity.ReviewResultEntity;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ReviewResultMapper {

    public ReviewResultEntity toEntity(IndexReviewContext context) {
        ReviewStatus status = context.validationWarnings().isEmpty()
                ? ReviewStatus.COMPLETED : ReviewStatus.COMPLETED_WITH_WARNINGS;
        ReviewResultEntity entity = ReviewResultEntity.builder()
                .indexCode(context.definition().indexCode().value()).reviewPeriod(context.definition().reviewPeriod())
                .cutOffDate(context.definition().reviewDates().cutOffDate()).reviewDate(context.definition().reviewDates().reviewDate())
                .status(status.name()).createdAt(context.executionTimestamp())
                .totalEligible(context.eligibleSecurities().size()).totalSelected(context.selectedConstituents().size()).build();
        for (SelectedConstituent constituent : context.selectedConstituents()) {
            entity.addConstituent(ReviewResultConstituentEntity.builder()
                    .securityId(constituent.securityId().value()).rank(constituent.rank()).ffmcap(constituent.ffmcap())
                    .rawWeight(constituent.rawWeight()).finalWeight(constituent.finalWeight())
                    .cappingFactor(constituent.cappingFactor()).decisionType(constituent.decisionType().name())
                    .decisionReason(constituent.decisionReason()).capped(constituent.capped()).build());
        }
        for (ReviewDecision decision : context.decisions()) {
            entity.addDecision(new ReviewDecisionEntity(decision.securityId().value(), decision.decisionType().name(), decision.reason()));
        }
        int sequence = 1;
        for (AuditEvent event : context.auditEvents()) {
            entity.addAuditEvent(new AuditEventEntity(sequence++, event.timestamp(), event.ruleCode(),
                    event.securityId() == null ? null : event.securityId().value(), event.message(),
                    event.inputValue(), event.outputValue()));
        }
        return entity;
    }

    public ReviewResult toDomain(ReviewResultEntity entity) {
        List<SelectedConstituent> constituents = entity.getConstituents().stream()
                .sorted(Comparator.comparingInt(ReviewResultConstituentEntity::getRank))
                .map(value -> new SelectedConstituent(new SecurityId(value.getSecurityId()), value.getRank(), value.getFfmcap(),
                        findCurrent(entity, value.getSecurityId()), value.getRawWeight(), value.getFinalWeight(),
                        value.getCappingFactor(), DecisionType.valueOf(value.getDecisionType()), value.getDecisionReason(), value.isCapped()))
                .toList();
        List<ReviewDecision> decisions = entity.getDecisions().stream()
                .sorted(Comparator.comparing(ReviewDecisionEntity::getSecurityId))
                .map(value -> new ReviewDecision(new SecurityId(value.getSecurityId()),
                        DecisionType.valueOf(value.getDecisionType()), value.getReason()))
                .toList();
        Set<SecurityId> current = decisions.stream()
                .filter(value -> value.decisionType() == DecisionType.UNCHANGED || value.decisionType() == DecisionType.LEAVER)
                .map(ReviewDecision::securityId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        List<RejectedSecurity> rejected = decisions.stream()
                .filter(value -> value.decisionType() == DecisionType.REJECTED)
                .map(value -> new RejectedSecurity(value.securityId(), value.reason())).toList();
        List<AuditEvent> auditEvents = entity.getAuditEvents().stream()
                .sorted(Comparator.comparingInt(AuditEventEntity::getSequenceNumber))
                .map(value -> new AuditEvent(value.getTimestamp(), value.getRuleCode(),
                        value.getSecurityId() == null ? null : new SecurityId(value.getSecurityId()), value.getMessage(),
                        value.getInputValue(), value.getOutputValue()))
                .toList();
        return new ReviewResult(entity.getId(), new IndexCode(entity.getIndexCode()), entity.getReviewPeriod(),
                entity.getCutOffDate(), entity.getReviewDate(), ReviewStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(), entity.getTotalEligible(), entity.getTotalSelected(), current,
                constituents, decisions, rejected, auditEvents, Map.of());
    }

    private boolean findCurrent(ReviewResultEntity entity, Integer securityId) {
        return entity.getDecisions().stream().anyMatch(value -> value.getSecurityId().equals(securityId)
                && (value.getDecisionType().equals("UNCHANGED") || value.getDecisionType().equals("LEAVER")));
    }
}

package com.six.indexreview.domain.rule.impl;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.model.*;
import com.six.indexreview.domain.rule.JoinerLeaverRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class DefaultJoinerLeaverRule implements JoinerLeaverRule {
    private static final String RANK_PREFIX = "rank=";
    @Override
    public String code() {
        return "IDENTIFY_JOINERS_LEAVERS";
    }

    @Override
    public String description() {
        return "Compare selected constituents with the current composition";
    }

    @Override
    public IndexReviewContext apply(IndexReviewContext context) {
        log.info("Starting rule {}", code());
        Set<SecurityId> selectedIds = new HashSet<>();
        Map<SecurityId, RankedSecurity> rankedById = new HashMap<>();
        context.rankedSecurities().forEach(value -> rankedById.put(value.securityId(), value));
        List<SelectedConstituent> selected = new ArrayList<>();
        List<ReviewDecision> decisions = new ArrayList<>();

        for (SelectedConstituent constituent : context.selectedConstituents()) {
            selectedIds.add(constituent.securityId());
            DecisionType type = context.currentComposition().contains(constituent.securityId())
                    ? DecisionType.UNCHANGED : DecisionType.JOINER;
            String reason = selectionReason(context, constituent, type);
            SelectedConstituent updated = constituent.withDecision(type, reason);
            selected.add(updated);
            decisions.add(new ReviewDecision(updated.securityId(), type, reason));
            context.audit(code(), updated.securityId(), reason, RANK_PREFIX + updated.rank(), type.name());
        }

        for (SecurityId current : context.currentComposition()) {
            if (!selectedIds.contains(current)) {
                RankedSecurity ranked = rankedById.get(current);
                String reason = ranked == null
                        ? "Security was current constituent but is not present in the eligible universe."
                        : "Security was current constituent but ranked outside selected range and no buffer retained it.";
                decisions.add(new ReviewDecision(current, DecisionType.LEAVER, reason));
                context.audit(code(), current, reason, rankValue(ranked), "LEAVER");
            }
        }

        Set<SecurityId> decided = new HashSet<>(selectedIds);
        decided.addAll(context.currentComposition());
        for (RankedSecurity ranked : context.rankedSecurities()) {
            if (!selectedIds.contains(ranked.securityId()) && !context.currentComposition().contains(ranked.securityId())) {
                String reason = "Security not selected because rank is greater than constituent count.";
                decisions.add(new ReviewDecision(ranked.securityId(), DecisionType.NOT_SELECTED, reason));
                context.audit(code(), ranked.securityId(), reason, RANK_PREFIX + ranked.rank(), "NOT_SELECTED");
            }
        }
        for (var rejected : context.rejectedSecurities()) {
            if (!decided.contains(rejected.securityId())) {
                decisions.add(new ReviewDecision(rejected.securityId(), DecisionType.REJECTED, rejected.reason()));
                context.audit(code(), rejected.securityId(), rejected.reason(), "eligibility", "REJECTED");
            }
        }

        decisions.sort(Comparator.comparing(ReviewDecision::securityId));
        selected.sort(Comparator.comparingInt(SelectedConstituent::rank));
        context.replaceSelected(selected);
        context.replaceDecisions(decisions);
        long joiners = decisions.stream().filter(value -> value.decisionType() == DecisionType.JOINER).count();
        long leavers = decisions.stream().filter(value -> value.decisionType() == DecisionType.LEAVER).count();
        log.info("Completed rule {} joiners={} leavers={}", code(), joiners, leavers);
        return context;
    }

    private String selectionReason(IndexReviewContext context, SelectedConstituent constituent, DecisionType type) {
        if (type == DecisionType.JOINER) {
            return "Security ranked inside top " + context.definition().constituentCount() + " by FFMCAP.";
        }
        return constituent.rank() > context.definition().constituentCount()
                ? "Current constituent retained by buffer rule."
                : "Security selected by top-N rule.";
    }

    private String rankValue(RankedSecurity ranked) {
        return ranked == null ? "not-ranked" : RANK_PREFIX + ranked.rank();
    }
}

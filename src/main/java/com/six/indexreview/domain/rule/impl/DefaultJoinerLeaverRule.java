package com.six.indexreview.domain.rule.impl;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.model.*;
import com.six.indexreview.domain.rule.JoinerLeaverRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/** Classifies the selected universe against the prior composition. The rule
 * is separated from ranking and capping so the decision trail can be audited
 * independently and future methodology variants can swap only this
 * classification strategy. */
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
        Map<SecurityId, RankedSecurity> rankedById = rankedById(context);
        List<SelectedConstituent> selected = classifySelected(context);
        Set<SecurityId> selectedIds = selected.stream().map(SelectedConstituent::securityId).collect(java.util.stream.Collectors.toCollection(HashSet::new));
        List<ReviewDecision> decisions = new ArrayList<>(selected.stream()
                .map(value -> new ReviewDecision(value.securityId(), value.decisionType(), value.decisionReason()))
                .toList());

        decisions.addAll(classifyLeavers(context, selectedIds, rankedById));
        Set<SecurityId> decided = new HashSet<>(selectedIds);
        decided.addAll(context.currentComposition());
        decisions.addAll(classifyNotSelected(context, selectedIds));
        decisions.addAll(classifyRejected(context, decided));

        // Deterministic ordering keeps the decision feed stable for report
        // generation, audit lookup, and integration testing.
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

    private Map<SecurityId, RankedSecurity> rankedById(IndexReviewContext context) {
        Map<SecurityId, RankedSecurity> rankedById = new HashMap<>();
        context.rankedSecurities().forEach(value -> rankedById.put(value.securityId(), value));
        return rankedById;
    }

    private List<SelectedConstituent> classifySelected(IndexReviewContext context) {
        List<SelectedConstituent> selected = new ArrayList<>();
        for (SelectedConstituent constituent : context.selectedConstituents()) {
            DecisionType type = context.currentComposition().contains(constituent.securityId())
                    ? DecisionType.UNCHANGED : DecisionType.JOINER;
            String reason = selectionReason(context, constituent, type);
            SelectedConstituent updated = constituent.withDecision(type, reason);
            selected.add(updated);
            context.audit(code(), updated.securityId(), reason, RANK_PREFIX + updated.rank(), type.name());
        }
        return selected;
    }

    private List<ReviewDecision> classifyLeavers(IndexReviewContext context,
                                                 Set<SecurityId> selectedIds,
                                                 Map<SecurityId, RankedSecurity> rankedById) {
        List<ReviewDecision> leavers = new ArrayList<>();
        for (SecurityId current : context.currentComposition()) {
            if (!selectedIds.contains(current)) {
                RankedSecurity ranked = rankedById.get(current);
                String reason = ranked == null
                        ? "Security was current constituent but is not present in the eligible universe."
                        : "Security was current constituent but ranked outside selected range and no buffer retained it.";
                leavers.add(new ReviewDecision(current, DecisionType.LEAVER, reason));
                context.audit(code(), current, reason, rankValue(ranked), "LEAVER");
            }
        }
        return leavers;
    }

    private List<ReviewDecision> classifyNotSelected(IndexReviewContext context, Set<SecurityId> selectedIds) {
        List<ReviewDecision> notSelected = new ArrayList<>();
        for (RankedSecurity ranked : context.rankedSecurities()) {
            if (!selectedIds.contains(ranked.securityId()) && !context.currentComposition().contains(ranked.securityId())) {
                String reason = "Security not selected because rank is greater than constituent count.";
                notSelected.add(new ReviewDecision(ranked.securityId(), DecisionType.NOT_SELECTED, reason));
                context.audit(code(), ranked.securityId(), reason, RANK_PREFIX + ranked.rank(), "NOT_SELECTED");
            }
        }
        return notSelected;
    }

    private List<ReviewDecision> classifyRejected(IndexReviewContext context, Set<SecurityId> decided) {
        List<ReviewDecision> rejected = new ArrayList<>();
        for (var item : context.rejectedSecurities()) {
            if (!decided.contains(item.securityId())) {
                rejected.add(new ReviewDecision(item.securityId(), DecisionType.REJECTED, item.reason()));
                context.audit(code(), item.securityId(), item.reason(), "eligibility", "REJECTED");
            }
        }
        return rejected;
    }

    private String rankValue(RankedSecurity ranked) {
        return ranked == null ? "not-ranked" : RANK_PREFIX + ranked.rank();
    }
}

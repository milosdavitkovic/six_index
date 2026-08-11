package com.six.indexreview.domain.rule.impl;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.model.RankedSecurity;
import com.six.indexreview.domain.model.SelectedConstituent;
import com.six.indexreview.domain.rule.BufferRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Extension-point implementation for an index methodology that retains a
 * current member when it is within a configurable rank band.  SMI currently
 * selects NONE, but the engine can activate this rule without changing its
 * orchestration or persistence layers.
 */
@Slf4j
@Component("configurableBufferRule")
public class ConfigurableBufferRule implements BufferRule {
    private static final String RANK_PREFIX = "rank=";
    @Override
    public String code() {
        return "BUFFER_CONFIGURABLE";
    }

    @Override
    public String description() {
        return "Retain current constituents inside the configured rank buffer";
    }

    @Override
    public IndexReviewContext apply(IndexReviewContext context) {
        int rankLimit = context.definition().bufferRetentionRank() > 0
                ? context.definition().bufferRetentionRank()
                : context.definition().constituentCount() + 5;
        List<SelectedConstituent> selected = new ArrayList<>(context.selectedConstituents());
        Set<com.six.indexreview.domain.model.SecurityId> selectedIds = new HashSet<>(selected.stream()
                .map(SelectedConstituent::securityId).toList());
        List<RankedSecurity> candidates = context.rankedSecurities().stream()
                .filter(value -> value.currentConstituent() && !selectedIds.contains(value.securityId())
                        && value.rank() <= rankLimit)
                .toList();
        for (RankedSecurity candidate : candidates) {
            int replacementIndex = -1;
            for (int i = 0; i < selected.size(); i++) {
                if (!selected.get(i).currentConstituent()
                        && (replacementIndex < 0 || selected.get(i).rank() > selected.get(replacementIndex).rank())) {
                    replacementIndex = i;
                }
            }
            if (replacementIndex < 0) {
                continue;
            }
            SelectedConstituent replacement = selected.set(replacementIndex,
                    new SelectedConstituent(candidate.securityId(), candidate.rank(), candidate.ffmcap(), true));
            selectedIds.remove(replacement.securityId());
            selectedIds.add(candidate.securityId());
            context.audit(code(), candidate.securityId(), "Current constituent retained by configurable buffer rule.",
                    RANK_PREFIX + candidate.rank() + ",rankLimit=" + rankLimit,
                    "replaced=" + replacement.securityId());
        }
        selected.sort(Comparator.comparingInt(SelectedConstituent::rank));
        context.replaceSelected(selected);
        context.audit(code(), "Configurable buffer rule applied.", "rankLimit=" + rankLimit,
                "selected=" + selected.size());
        return context;
    }
}

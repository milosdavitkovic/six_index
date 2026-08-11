package com.six.indexreview.domain.service;

import com.six.indexreview.domain.model.EligibleSecurity;
import com.six.indexreview.domain.model.RankedSecurity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class DeterministicRanker {

    public List<RankedSecurity> rank(List<EligibleSecurity> securities, List<String> tieBreakers) {
        List<EligibleSecurity> sorted = new ArrayList<>(securities);
        Comparator<EligibleSecurity> comparator = Comparator.comparing(EligibleSecurity::ffmcap).reversed();
        if (tieBreakers != null && tieBreakers.stream().anyMatch("CURRENT_CONSTITUENT_FIRST"::equalsIgnoreCase)) {
            comparator = comparator.thenComparing(EligibleSecurity::currentConstituent, Comparator.reverseOrder());
        }
        // Security ID is always the final tie-breaker, which makes the result
        // deterministic even when a configuration omits optional tie-breakers.
        comparator = comparator.thenComparing(EligibleSecurity::securityId);
        sorted.sort(comparator); // TimSort: stable O(n log n), appropriate for a small/medium universe.

        List<RankedSecurity> ranked = new ArrayList<>(sorted.size());
        for (int i = 0; i < sorted.size(); i++) {
            EligibleSecurity security = sorted.get(i);
            ranked.add(new RankedSecurity(security.securityId(), i + 1, security.price(), security.shares(),
                    security.freeFloat(), security.ffmcap(), security.currentConstituent()));
        }
        log.debug("Ranked {} eligible securities", ranked.size());
        return ranked;
    }
}

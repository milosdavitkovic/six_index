package com.six.indexreview.domain.service;

import com.six.indexreview.domain.model.RankedSecurity;
import com.six.indexreview.domain.model.SelectedConstituent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Selects the highest-ranked securities for inclusion in the index.
 *
 * Selection is separated from ranking so future buffer or threshold logic can
 * be introduced without changing the ranking implementation.
 */
@Component
public class ConstituentSelector implements SelectionStrategy {

    public List<SelectedConstituent> select(List<RankedSecurity> ranked, int constituentCount) {
        // Top-N selection remains deterministic because ranking order is already
        // stable before this truncation step is applied.
        return ranked.stream()
                .limit(constituentCount)
                .map(value -> new SelectedConstituent(value.securityId(), value.rank(), value.ffmcap(),
                        value.currentConstituent()))
                .toList();
    }
}

package com.six.indexreview.domain.service;

import com.six.indexreview.domain.model.RankedSecurity;
import com.six.indexreview.domain.model.SelectedConstituent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConstituentSelector {

    public List<SelectedConstituent> select(List<RankedSecurity> ranked, int constituentCount) {
        return ranked.stream()
                .limit(constituentCount)
                .map(value -> new SelectedConstituent(value.securityId(), value.rank(), value.ffmcap(),
                        value.currentConstituent()))
                .toList();
    }
}

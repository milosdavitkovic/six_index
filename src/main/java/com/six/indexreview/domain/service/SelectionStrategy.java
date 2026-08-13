package com.six.indexreview.domain.service;

import com.six.indexreview.domain.model.RankedSecurity;
import com.six.indexreview.domain.model.SelectedConstituent;

import java.util.List;

/**
 * Strategy for selecting constituents from a ranked universe.
 *
 * Alternative methodologies can be implemented by providing new strategy
 * implementations without modifying workflow services.
 * @author Milos Davitkovic
 */
public interface SelectionStrategy {
    List<SelectedConstituent> select(List<RankedSecurity> ranked, int constituentCount);
}


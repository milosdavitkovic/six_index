package com.six.indexreview.domain.service;

import com.six.indexreview.domain.model.EligibleSecurity;
import com.six.indexreview.domain.model.RankedSecurity;

import java.util.List;

/**
 * Strategy for ranking eligible securities.
 *
 * Alternative methodologies can be implemented by providing new strategy
 * implementations without modifying workflow services.
 * @author Milos Davitkovic
 */
public interface RankingStrategy {
    List<RankedSecurity> rank(List<EligibleSecurity> securities, List<String> tieBreakers);
}


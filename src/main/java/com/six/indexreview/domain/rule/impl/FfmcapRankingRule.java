package com.six.indexreview.domain.rule.impl;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.model.EligibleSecurity;
import com.six.indexreview.domain.model.RankedSecurity;
import com.six.indexreview.domain.rule.RankingRule;
import com.six.indexreview.domain.service.DeterministicRanker;
import com.six.indexreview.domain.service.FreeFloatMarketCapCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates FFMCAP and produces the ranked eligible universe.
 *
 * The rule keeps methodology-specific ranking behavior isolated so tie-breaker
 * changes can be introduced through configuration or a new strategy.
 */
/**
 * @author Milos Davitkovic
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FfmcapRankingRule implements RankingRule {
    private static final String RANK_PREFIX = "rank=";
    private final FreeFloatMarketCapCalculator calculator;
    private final DeterministicRanker ranker;

    @Override
    public String code() {
        return "CALCULATE_AND_RANK_FFMCAP";
    }

    @Override
    public String description() {
        return "Calculate price(cut-off) x shares(review) x free-float(review) and rank descending";
    }

    @Override
    public IndexReviewContext apply(IndexReviewContext context) {
        log.info("Starting rule {} eligible={}", code(), context.eligibleSecurities().size());
        List<EligibleSecurity> calculated = new ArrayList<>();
        for (EligibleSecurity security : context.eligibleSecurities()) {
            // The ranking step depends on the exact FFMCAP values calculated
            // from the review-date snapshot, not on any persisted approximation.
            var ffmcap = calculator.calculate(security.price(), security.shares(), security.freeFloat());
            calculated.add(new EligibleSecurity(security.securityId(), security.price(), security.shares(),
                    security.freeFloat(), ffmcap, security.currentConstituent()));
            log.debug("FFMCAP securityId={} price={} shares={} freeFloat={} ffmcap={}", security.securityId(),
                    security.price(), security.shares(), security.freeFloat(), ffmcap);
            context.audit(code(), security.securityId(), "FFMCAP calculated from cut-off price and review-date shares/free float.",
                    "price=" + security.price() + ",shares=" + security.shares() + ",freeFloat=" + security.freeFloat(),
                    ffmcap.toPlainString());
        }
        context.replaceEligible(calculated);
        // Stable sorting plus a final security-ID tie-breaker guarantees
        // reproducible rankings across repeated runs.
        List<RankedSecurity> ranked = ranker.rank(calculated, context.definition().methodology().tieBreakers());
        context.replaceRanked(ranked);
        for (int i = 0; i < ranked.size(); i++) {
            RankedSecurity security = ranked.get(i);
            String tieMessage = i > 0 && ranked.get(i - 1).ffmcap().compareTo(security.ffmcap()) == 0
                    ? "Tie resolved by configured current-constituent preference and security ID ascending."
                    : "Security ranked by FFMCAP descending.";
            context.audit(code(), security.securityId(), tieMessage, security.ffmcap().toPlainString(),
                    RANK_PREFIX + security.rank());
        }
        log.info("Completed rule {} ranked={}", code(), ranked.size());
        return context;
    }
}

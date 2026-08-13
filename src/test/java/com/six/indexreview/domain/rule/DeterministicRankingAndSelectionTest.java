package com.six.indexreview.domain.rule;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.model.EligibleSecurity;
import com.six.indexreview.domain.model.RankedSecurity;
import com.six.indexreview.domain.model.SecurityId;
import com.six.indexreview.domain.rule.impl.FfmcapRankingRule;
import com.six.indexreview.domain.rule.impl.TopNSelectionRule;
import com.six.indexreview.domain.service.ConstituentSelector;
import com.six.indexreview.domain.service.DeterministicRanker;
import com.six.indexreview.domain.service.FreeFloatMarketCapCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Milos Davitkovic
 */
class DeterministicRankingAndSelectionTest {
    @Test
    void ranksByFfmcapThenCurrentConstituentThenSecurityId() {
        IndexReviewContext context = RuleTestFixtures.context(RuleTestFixtures.definition(2, new BigDecimal("1")),
                Set.of(new SecurityId(2)));
        context.replaceEligible(List.of(
                new EligibleSecurity(new SecurityId(3), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("10"), false),
                new EligibleSecurity(new SecurityId(2), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("10"), true),
                new EligibleSecurity(new SecurityId(1), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("10"), false)));

        new FfmcapRankingRule(new FreeFloatMarketCapCalculator(), new DeterministicRanker()).apply(context);

        assertThat(context.rankedSecurities().stream().map(RankedSecurity::securityId).toList())
                .containsExactly(new SecurityId(2), new SecurityId(1), new SecurityId(3));
        new TopNSelectionRule(new ConstituentSelector()).apply(context);
        assertThat(context.selectedConstituents().stream().map(value -> value.securityId()).toList())
                .containsExactly(new SecurityId(2), new SecurityId(1));
    }

    @Test
    void repeatedRankingIsIdentical() {
        DeterministicRanker ranker = new DeterministicRanker();
        List<EligibleSecurity> input = List.of(
                new EligibleSecurity(new SecurityId(2), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("5"), false),
                new EligibleSecurity(new SecurityId(1), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("5"), false));
        assertThat(ranker.rank(input, List.of("SECURITY_ID_ASC")))
                .containsExactly(ranker.rank(input, List.of("SECURITY_ID_ASC")).toArray(RankedSecurity[]::new));
    }

    @Test
    void usesSecurityIdAsDeterministicTieBreakerAtSelectionCutoff() {
        IndexReviewContext context = RuleTestFixtures.context(
                RuleTestFixtures.definition(2, BigDecimal.ONE), Set.of());
        context.replaceEligible(List.of(
                new EligibleSecurity(new SecurityId(3), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                        new BigDecimal("10"), false),
                new EligibleSecurity(new SecurityId(1), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                        new BigDecimal("10"), false),
                new EligibleSecurity(new SecurityId(2), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                        new BigDecimal("10"), false)));

        new FfmcapRankingRule(new FreeFloatMarketCapCalculator(), new DeterministicRanker()).apply(context);
        new TopNSelectionRule(new ConstituentSelector()).apply(context);

        assertThat(context.rankedSecurities().stream().map(RankedSecurity::securityId).toList())
                .containsExactly(new SecurityId(1), new SecurityId(2), new SecurityId(3));
        assertThat(context.selectedConstituents().stream().map(value -> value.securityId()).toList())
                .containsExactly(new SecurityId(1), new SecurityId(2));
    }
}

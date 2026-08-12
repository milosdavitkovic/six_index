package com.six.indexreview.domain.rule;

import com.six.indexreview.domain.model.DecisionType;
import com.six.indexreview.domain.model.RankedSecurity;
import com.six.indexreview.domain.model.RejectedSecurity;
import com.six.indexreview.domain.model.SecurityId;
import com.six.indexreview.domain.model.SelectedConstituent;
import com.six.indexreview.domain.rule.impl.DefaultJoinerLeaverRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JoinerLeaverRuleTest {
    @Test
    void identifiesJoinerLeaverUnchangedAndNotSelected() {
        var context = RuleTestFixtures.context(RuleTestFixtures.definition(2, BigDecimal.ONE), Set.of(new SecurityId(1), new SecurityId(3)));
        context.replaceRanked(List.of(
                new RankedSecurity(new SecurityId(1), 1, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.TEN, true),
                new RankedSecurity(new SecurityId(2), 2, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, false),
                new RankedSecurity(new SecurityId(4), 3, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, false)));
        context.replaceSelected(List.of(
                new SelectedConstituent(new SecurityId(1), 1, BigDecimal.TEN, true),
                new SelectedConstituent(new SecurityId(2), 2, BigDecimal.ONE, false)));

        new DefaultJoinerLeaverRule().apply(context);

        assertThat(context.decisions()).extracting(value -> value.decisionType())
                .contains(DecisionType.UNCHANGED, DecisionType.JOINER, DecisionType.LEAVER, DecisionType.NOT_SELECTED);
        assertThat(context.selectedConstituents()).allMatch(value -> value.decisionType() != null);
    }

    @Test
    void preservesRejectionEvidenceForAnIneligibleCurrentMember() {
        var currentRejected = new SecurityId(3);
        var context = RuleTestFixtures.context(RuleTestFixtures.definition(1, BigDecimal.ONE), Set.of(currentRejected));
        context.replaceRanked(List.of(
                new RankedSecurity(new SecurityId(1), 1, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.TEN, false)));
        context.replaceSelected(List.of(
                new SelectedConstituent(new SecurityId(1), 1, BigDecimal.TEN, false)));
        context.rejectedSecurities().add(new RejectedSecurity(currentRejected, "Missing review-date market data."));

        new DefaultJoinerLeaverRule().apply(context);

        assertThat(context.decisions()).filteredOn(value -> value.securityId().equals(currentRejected))
                .extracting(value -> value.decisionType())
                .containsExactlyInAnyOrder(DecisionType.LEAVER, DecisionType.REJECTED);
        assertThat(context.decisions()).filteredOn(value -> value.securityId().equals(currentRejected))
                .filteredOn(value -> value.decisionType() == DecisionType.REJECTED)
                .extracting(value -> value.reason())
                .containsExactly("Missing review-date market data.");
    }
}

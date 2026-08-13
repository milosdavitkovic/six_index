package com.six.indexreview.domain.rule;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.engine.ReviewDataSnapshot;
import com.six.indexreview.domain.model.MarketData;
import com.six.indexreview.domain.model.SecurityId;
import com.six.indexreview.domain.rule.impl.SpiUniverseEligibilityRule;
import com.six.indexreview.domain.service.FreeFloatMarketCapCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SpiUniverseEligibilityRuleTest {
    private static final LocalDate CUT_OFF = LocalDate.of(2026, 9, 10);
    private static final LocalDate REVIEW = LocalDate.of(2026, 9, 21);

    @Test
    void rejectsMissingCutOffPriceWithClearSecurityLevelReason() {
        SecurityId missing = new SecurityId(1);
        SecurityId valid = new SecurityId(2);
        IndexReviewContext context = context(Set.of(missing, valid),
                Map.of(missing, market(missing, CUT_OFF, null, "100", "0.5"),
                        valid, market(valid, CUT_OFF, "10", "100", "0.5")),
                Map.of(missing, market(missing, REVIEW, "999", "100", "0.5"),
                        valid, market(valid, REVIEW, "999", "100", "0.5")));

        new SpiUniverseEligibilityRule(new FreeFloatMarketCapCalculator()).apply(context);

        assertThat(context.eligibleSecurities()).extracting(value -> value.securityId())
                .containsExactly(valid);
        assertThat(context.rejectedSecurities()).singleElement()
                .satisfies(value -> assertThat(value.reason()).isEqualTo("Missing price on cut-off date."));
    }

    @Test
    void rejectsMissingReviewSharesAndFreeFloatWithClearReasons() {
        SecurityId missingShares = new SecurityId(1);
        SecurityId missingFloat = new SecurityId(2);
        SecurityId valid = new SecurityId(3);
        Set<SecurityId> universe = Set.of(missingShares, missingFloat, valid);
        IndexReviewContext context = context(universe,
                Map.of(missingShares, market(missingShares, CUT_OFF, "10", "100", "0.5"),
                        missingFloat, market(missingFloat, CUT_OFF, "10", "100", "0.5"),
                        valid, market(valid, CUT_OFF, "10", "100", "0.5")),
                Map.of(missingShares, market(missingShares, REVIEW, "999", null, "0.5"),
                        missingFloat, market(missingFloat, REVIEW, "999", "100", null),
                        valid, market(valid, REVIEW, "999", "100", "0.5")));

        new SpiUniverseEligibilityRule(new FreeFloatMarketCapCalculator()).apply(context);

        assertThat(context.rejectedSecurities()).extracting(value -> value.reason())
                .containsExactlyInAnyOrder("Missing shares on review date.", "Missing free float on review date.");
    }

    private IndexReviewContext context(Set<SecurityId> universe,
                                       Map<SecurityId, MarketData> cutOff,
                                       Map<SecurityId, MarketData> review) {
        return new IndexReviewContext(RuleTestFixtures.definition(1, BigDecimal.ONE),
                new ReviewDataSnapshot(universe, cutOff, review, Set.of()),
                RuleTestFixtures.precision(), Instant.EPOCH);
    }

    private MarketData market(SecurityId id, LocalDate date, String price, String shares, String freeFloat) {
        return new MarketData(id, date, decimal(price), decimal(shares), decimal(freeFloat));
    }

    private BigDecimal decimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}

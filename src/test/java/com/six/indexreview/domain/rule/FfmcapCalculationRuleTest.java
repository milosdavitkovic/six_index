package com.six.indexreview.domain.rule;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.engine.ReviewDataSnapshot;
import com.six.indexreview.domain.model.MarketData;
import com.six.indexreview.domain.model.SecurityId;
import com.six.indexreview.domain.rule.impl.FfmcapCalculationRule;
import com.six.indexreview.domain.rule.impl.SpiUniverseEligibilityRule;
import com.six.indexreview.domain.service.FreeFloatMarketCapCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FfmcapCalculationRuleTest {
    @Test
    void usesCutOffPriceAndReviewDateSharesAndFreeFloat() {
        SecurityId securityId = new SecurityId(42);
        LocalDate cutOffDate = LocalDate.of(2026, 9, 10);
        LocalDate reviewDate = LocalDate.of(2026, 9, 21);
        MarketData cutOff = new MarketData(securityId, cutOffDate,
                new BigDecimal("10"), new BigDecimal("999"), new BigDecimal("0.01"));
        MarketData review = new MarketData(securityId, reviewDate,
                new BigDecimal("999"), new BigDecimal("200"), new BigDecimal("0.25"));
        IndexReviewContext context = new IndexReviewContext(
                RuleTestFixtures.definition(1, BigDecimal.ONE),
                new ReviewDataSnapshot(Set.of(securityId), Map.of(securityId, cutOff),
                        Map.of(securityId, review), Set.of()),
                RuleTestFixtures.precision(), Instant.EPOCH);

        new SpiUniverseEligibilityRule(new FreeFloatMarketCapCalculator()).apply(context);
        new FfmcapCalculationRule(new FreeFloatMarketCapCalculator()).apply(context);

        assertThat(context.eligibleSecurities()).singleElement().satisfies(value -> {
            assertThat(value.price()).isEqualByComparingTo("10");
            assertThat(value.shares()).isEqualByComparingTo("200");
            assertThat(value.freeFloat()).isEqualByComparingTo("0.25");
            assertThat(value.ffmcap()).isEqualByComparingTo("500");
        });
    }
}

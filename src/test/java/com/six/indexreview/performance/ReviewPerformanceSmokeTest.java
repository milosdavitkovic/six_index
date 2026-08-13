package com.six.indexreview.performance;

import com.six.indexreview.domain.model.EligibleSecurity;
import com.six.indexreview.domain.model.SecurityId;
import com.six.indexreview.domain.service.ConstituentSelector;
import com.six.indexreview.domain.service.DeterministicRanker;
import com.six.indexreview.domain.service.FreeFloatMarketCapCalculator;
import com.six.indexreview.domain.service.WeightCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Milos Davitkovic
 */
class ReviewPerformanceSmokeTest {
    private final FreeFloatMarketCapCalculator calculator = new FreeFloatMarketCapCalculator();
    private final DeterministicRanker ranker = new DeterministicRanker();
    private final ConstituentSelector selector = new ConstituentSelector();
    private final WeightCalculator weightCalculator = new WeightCalculator(new com.six.indexreview.domain.service.PrecisionPolicy(16, 10, RoundingMode.HALF_UP));

    @Test
    void rankingSelectionAndWeightingRemainFastOnSyntheticUniverse() {
        List<EligibleSecurity> securities = syntheticUniverse(5000);
        Instant start = Instant.now();
        List<EligibleSecurity> calculated = securities.stream()
                .map(security -> new EligibleSecurity(security.securityId(), security.price(), security.shares(),
                        security.freeFloat(), calculator.calculate(security.price(), security.shares(), security.freeFloat()),
                        security.currentConstituent()))
                .toList();
        var ranked = ranker.rank(calculated, List.of("CURRENT_CONSTITUENT_FIRST", "SECURITY_ID_ASC"));
        var selected = selector.select(ranked, 20);
        var weighted = weightCalculator.calculate(selected);
        Duration duration = Duration.between(start, Instant.now());
        long approxBytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        assertThat(weighted).hasSize(20);
        assertThat(duration.toMillis()).isLessThan(2500);
        assertThat(approxBytes).isGreaterThan(0);
    }

    private List<EligibleSecurity> syntheticUniverse(int size) {
        List<EligibleSecurity> securities = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            securities.add(new EligibleSecurity(new SecurityId(i), new BigDecimal("10.0"), new BigDecimal("1000"),
                    new BigDecimal("0.5"), BigDecimal.ZERO, i % 3 == 0));
        }
        return securities;
    }
}



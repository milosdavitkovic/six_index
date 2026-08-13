package com.six.indexreview.domain.rule;

import com.six.indexreview.domain.model.SecurityId;
import com.six.indexreview.domain.model.SelectedConstituent;
import com.six.indexreview.domain.rule.impl.IterativeProportionalWeightCappingRule;
import com.six.indexreview.domain.service.PrecisionPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * @author Milos Davitkovic
 */
class IterativeProportionalWeightCappingRuleTest {
    private final IterativeProportionalWeightCappingRule capper =
            new IterativeProportionalWeightCappingRule(new PrecisionPolicy(16, 10, RoundingMode.HALF_UP));

    @Test
    void leavesWeightsBelowCapUntouchedApartFromOutputRounding() {
        List<SelectedConstituent> result = capper.cap(weights("0.10", "0.20", "0.70"), new BigDecimal("0.80"));
        assertThat(result.stream().map(SelectedConstituent::finalWeight).toList())
                .containsExactly(new BigDecimal("0.1000000000"), new BigDecimal("0.2000000000"), new BigDecimal("0.7000000000"));
    }

    @Test
    void redistributesOneOverCapProportionally() {
        List<SelectedConstituent> result = capper.cap(weights("0.70", "0.20", "0.10"), new BigDecimal("0.50"));
        assertThat(result.stream().map(SelectedConstituent::finalWeight).toList())
                .containsExactly(new BigDecimal("0.5000000000"), new BigDecimal("0.3333333333"), new BigDecimal("0.1666666667"));
        assertValid(result, new BigDecimal("0.50"));
    }

    @Test
    void handlesMultipleAndRepeatedRedistributionIterations() {
        List<SelectedConstituent> result = capper.cap(weights("0.80", "0.15", "0.05"), new BigDecimal("0.40"));
        assertThat(result.stream().map(SelectedConstituent::finalWeight).toList())
                .containsExactly(new BigDecimal("0.4000000000"), new BigDecimal("0.4000000000"), new BigDecimal("0.2000000000"));
        assertThat(result.stream().filter(SelectedConstituent::capped)).hasSize(2);
        assertValid(result, new BigDecimal("0.40"));
    }

    @Test
    void appliesDeterministicResidualAndRejectsImpossibleCap() {
        List<SelectedConstituent> result = capper.cap(weights("0.3333333333333333", "0.3333333333333333", "0.3333333333333334"), new BigDecimal("0.60"));
        assertThat(result.stream().map(SelectedConstituent::finalWeight).reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("1.0000000000");
        assertThatIllegalArgumentException().isThrownBy(() -> capper.cap(weights("0.5", "0.5"), new BigDecimal("0.49")));
    }

    private List<SelectedConstituent> weights(String... values) {
        var result = new java.util.ArrayList<SelectedConstituent>();
        for (int i = 0; i < values.length; i++) {
            result.add(new SelectedConstituent(new SecurityId(i + 1), i + 1, BigDecimal.ONE, false,
                    new BigDecimal(values[i]), null, null, null, null, false));
        }
        return result;
    }

    private void assertValid(List<SelectedConstituent> result, BigDecimal cap) {
        assertThat(result.stream().map(SelectedConstituent::finalWeight).reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("1.0000000000");
        assertThat(result).allMatch(value -> value.finalWeight().compareTo(cap.setScale(10)) <= 0);
    }
}

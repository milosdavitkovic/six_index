package com.six.indexreview.domain.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * @author Milos Davitkovic
 */
class FreeFloatMarketCapCalculatorTest {
    private final FreeFloatMarketCapCalculator calculator = new FreeFloatMarketCapCalculator();

    @Test
    void calculatesUsingBigDecimalOnly() {
        assertThat(calculator.calculate(new BigDecimal("12.50"), new BigDecimal("1000"), new BigDecimal("0.25")))
                .isEqualByComparingTo("3125.000");
    }

    @Test
    void supportsZeroAndFullFreeFloat() {
        assertThat(calculator.calculate(new BigDecimal("10"), new BigDecimal("100"), BigDecimal.ZERO))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(calculator.calculate(new BigDecimal("10"), new BigDecimal("100"), BigDecimal.ONE))
                .isEqualByComparingTo("1000");
    }

    @Test
    void rejectsInvalidInputs() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                calculator.calculate(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE));
        assertThatIllegalArgumentException().isThrownBy(() ->
                calculator.calculate(BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("1.01")));
        assertThatIllegalArgumentException().isThrownBy(() ->
                calculator.calculate(null, BigDecimal.ONE, BigDecimal.ONE));
    }
}

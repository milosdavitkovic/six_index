package com.six.indexreview.domain.service;

import com.six.indexreview.domain.model.SelectedConstituent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class WeightCalculator {
    private final PrecisionPolicy precisionPolicy;

    public WeightCalculator() {
        this(new PrecisionPolicy(16, 10, java.math.RoundingMode.HALF_UP));
    }

    @Autowired
    public WeightCalculator(PrecisionPolicy precisionPolicy) {
        this.precisionPolicy = precisionPolicy;
    }

    public List<SelectedConstituent> calculate(List<SelectedConstituent> constituents) {
        BigDecimal total = constituents.stream()
                .map(SelectedConstituent::ffmcap)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Selected constituents must have positive total FFMCAP");
        }
        List<SelectedConstituent> result = new ArrayList<>(constituents.size());
        for (SelectedConstituent constituent : constituents) {
            if (constituent.ffmcap().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Selected constituent has non-positive FFMCAP: " + constituent.securityId());
            }
            result.add(constituent.withRawWeight(precisionPolicy.divide(constituent.ffmcap(), total)));
        }
        return result;
    }
}

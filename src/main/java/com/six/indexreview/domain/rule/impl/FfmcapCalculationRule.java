package com.six.indexreview.domain.rule.impl;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.model.EligibleSecurity;
import com.six.indexreview.domain.rule.ReviewRule;
import com.six.indexreview.domain.service.FreeFloatMarketCapCalculator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

/** Standalone FFMCAP calculation rule; the SMI pipeline combines it with ranking for one pass. */
@Slf4j
public class FfmcapCalculationRule implements ReviewRule {
    private final FreeFloatMarketCapCalculator calculator;

    public FfmcapCalculationRule(FreeFloatMarketCapCalculator calculator) {
        this.calculator = calculator;
    }

    @Override
    public String code() {
        return "CALCULATE_FFMCAP";
    }

    @Override
    public String description() {
        return "Calculate free-float market capitalisation for every eligible security";
    }

    @Override
    public IndexReviewContext apply(IndexReviewContext context) {
        log.info("Starting rule {} eligible={}", code(), context.eligibleSecurities().size());
        var calculated = new ArrayList<EligibleSecurity>();
        for (EligibleSecurity value : context.eligibleSecurities()) {
            var ffmcap = calculator.calculate(value.price(), value.shares(), value.freeFloat());
            calculated.add(new EligibleSecurity(value.securityId(), value.price(), value.shares(), value.freeFloat(),
                    ffmcap, value.currentConstituent()));
            context.audit(code(), value.securityId(), "FFMCAP calculated.",
                    "price=" + value.price() + ",shares=" + value.shares() + ",freeFloat=" + value.freeFloat(),
                    ffmcap.toPlainString());
        }
        context.replaceEligible(calculated);
        log.info("Completed rule {} calculated={}", code(), calculated.size());
        return context;
    }
}

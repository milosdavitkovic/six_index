package com.six.indexreview.domain.rule;

import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.engine.ReviewDataSnapshot;
import com.six.indexreview.domain.model.IndexCode;
import com.six.indexreview.domain.model.IndexDefinition;
import com.six.indexreview.domain.model.ReviewDates;
import com.six.indexreview.domain.model.SecurityId;
import com.six.indexreview.domain.service.PrecisionPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RuleTestFixtures {
    private RuleTestFixtures() {
    }

    public static PrecisionPolicy precision() {
        return new PrecisionPolicy(16, 10, RoundingMode.HALF_UP);
    }

    public static IndexDefinition definition(int count, BigDecimal cap) {
        return new IndexDefinition(new IndexCode("SMI"), "Test index", count, cap, "FFMCAP", "TOP_N",
                "NONE", "Q3-2026", new ReviewDates(LocalDate.of(2026, Month.SEPTEMBER, 10), LocalDate.of(2026, Month.SEPTEMBER, 21)),
                List.of("CURRENT_CONSTITUENT_FIRST", "SECURITY_ID_ASC"), true, 0);
    }

    public static IndexReviewContext context(IndexDefinition definition, Set<SecurityId> current) {
        return new IndexReviewContext(definition,
                new ReviewDataSnapshot(Set.of(), Map.of(), Map.of(), current), precision(), Instant.EPOCH);
    }
}

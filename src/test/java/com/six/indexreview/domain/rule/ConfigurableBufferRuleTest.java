package com.six.indexreview.domain.rule;

import com.six.indexreview.domain.model.*;
import com.six.indexreview.domain.rule.impl.ConfigurableBufferRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurableBufferRuleTest {
    @Test
    void retainsCurrentMembersWithinConfiguredRankBand() {
        IndexDefinition definition = new IndexDefinition(
                new IndexCode("SMI"), "Test index", 2, BigDecimal.ONE,
                "FFMCAP", "TOP_N", "CONFIGURABLE", "Q3-2026",
                new ReviewDates(LocalDate.of(2026, Month.SEPTEMBER, 10), LocalDate.of(2026, Month.SEPTEMBER, 21)),
                List.of("CURRENT_CONSTITUENT_FIRST", "SECURITY_ID_ASC"), true, 4);
        var context = RuleTestFixtures.context(definition, Set.of(new SecurityId(1), new SecurityId(2)));
        context.replaceRanked(List.of(
                ranked(3, 1, false),
                ranked(4, 2, false),
                ranked(1, 3, true),
                ranked(2, 4, true)));
        context.replaceSelected(List.of(
                new SelectedConstituent(new SecurityId(3), 1, BigDecimal.valueOf(4), false),
                new SelectedConstituent(new SecurityId(4), 2, BigDecimal.valueOf(3), false)));

        new ConfigurableBufferRule().apply(context);

        assertThat(context.selectedConstituents())
                .extracting(SelectedConstituent::securityId)
                .containsExactly(new SecurityId(1), new SecurityId(2));
        assertThat(context.auditEvents())
                .anyMatch(event -> event.message().contains("retained by configurable buffer rule"));
    }

    private RankedSecurity ranked(int id, int rank, boolean current) {
        return new RankedSecurity(new SecurityId(id), rank, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.valueOf(10L - rank), current);
    }
}

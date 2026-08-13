package com.six.indexreview.domain.rule;

import com.six.indexreview.domain.model.SecurityId;
import com.six.indexreview.domain.model.SelectedConstituent;
import com.six.indexreview.domain.rule.impl.NoOpBufferRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Milos Davitkovic
 */
class NoOpBufferRuleTest {
    @Test
    void leavesPureTopNSelectionUnchangedAndAuditsConfiguration() {
        var context = RuleTestFixtures.context(RuleTestFixtures.definition(1, BigDecimal.ONE), java.util.Set.of());
        context.replaceSelected(List.of(new SelectedConstituent(new SecurityId(1), 1, BigDecimal.TEN, false)));
        new NoOpBufferRule().apply(context);
        assertThat(context.selectedConstituents()).hasSize(1);
        assertThat(context.auditEvents()).anyMatch(value -> value.message().contains("Pure top-N selection applied"));
    }
}

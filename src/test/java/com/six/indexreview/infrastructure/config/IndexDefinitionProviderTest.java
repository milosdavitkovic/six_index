package com.six.indexreview.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IndexDefinitionProviderTest {
    @Test
    void unsupportedRankingRuleFailsDuringConfigurationResolution() {
        IndexConfiguration configuration = new IndexConfiguration();
        configuration.setIndexCode("SMI");
        configuration.setName("Swiss Market Index");
        configuration.setConstituentCount(20);
        configuration.setMaxWeight(new BigDecimal("0.18"));
        configuration.setRankingRule("UNKNOWN");
        configuration.setReviewPeriod("Q3-2026");
        configuration.setCutOffDate(LocalDate.of(2026, 9, 10));
        configuration.setReviewDate(LocalDate.of(2026, 9, 21));

        IndexReviewProperties properties = new IndexReviewProperties();
        properties.setIndices(new LinkedHashMap<>());
        properties.getIndices().put("SMI", configuration);

        assertThatThrownBy(() -> new IndexDefinitionProvider(properties).get("smi", "q3-2026"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported ranking rule: UNKNOWN");
    }
}

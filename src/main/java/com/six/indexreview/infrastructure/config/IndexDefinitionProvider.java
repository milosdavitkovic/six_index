package com.six.indexreview.infrastructure.config;

import com.six.indexreview.domain.model.IndexCode;
import com.six.indexreview.domain.model.IndexDefinition;
import com.six.indexreview.domain.model.ReviewDates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Core IndexDefinitionProvider component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IndexDefinitionProvider {
    private final IndexReviewProperties properties;

    public IndexDefinition defaultDefinition() {
        return properties.getIndices().values().stream()
                .filter(IndexConfiguration::isEnabled)
                .findFirst()
                .map(configuration -> get(configuration.getIndexCode() == null ? configuration.getName() : configuration.getIndexCode(),
                        configuration.getReviewPeriod()))
                .orElseThrow(() -> new IllegalArgumentException("No enabled index configuration found"));
    }

    public IndexDefinition get(String indexCode, String reviewPeriod) {
        IndexConfiguration configuration = properties.getIndices().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(indexCode)
                        || (entry.getValue().getIndexCode() != null
                        && entry.getValue().getIndexCode().equalsIgnoreCase(indexCode)))
                .map(java.util.Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No index configuration found for " + indexCode));
        if (!configuration.isEnabled()) {
            throw new IllegalArgumentException("Index is disabled: " + indexCode);
        }
        if (configuration.getReviewPeriod() == null || !configuration.getReviewPeriod().equalsIgnoreCase(reviewPeriod)) {
            throw new IllegalArgumentException("No configuration for review period " + reviewPeriod + " and index " + indexCode);
        }
        IndexDefinition definition = new IndexDefinition(
                new IndexCode(configuration.getIndexCode() == null ? indexCode : configuration.getIndexCode()),
                configuration.getName(),
                configuration.getConstituentCount(),
                configuration.getMaxWeight(),
                upper(configuration.getRankingRule()),
                upper(configuration.getSelectionRule()),
                upper(configuration.getBufferRule()),
                configuration.getReviewPeriod(),
                new ReviewDates(configuration.getCutOffDate(), configuration.getReviewDate()),
                configuration.getTieBreakers(),
                configuration.isEnabled(),
                configuration.getBufferRetentionRank());
        log.info("Loaded index configuration indexCode={} reviewPeriod={} constituentCount={} maxWeight={}",
                definition.indexCode(), definition.reviewPeriod(), definition.methodology().constituentCount(), definition.methodology().maxWeight());
        return definition;
    }

    private String upper(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }
}

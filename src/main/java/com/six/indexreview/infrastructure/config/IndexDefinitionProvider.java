package com.six.indexreview.infrastructure.config;

import com.six.indexreview.domain.model.IndexCode;
import com.six.indexreview.domain.model.IndexDefinition;
import com.six.indexreview.domain.model.ReviewDates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

/**
 * Core IndexDefinitionProvider component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
/**
 * @author Milos Davitkovic
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
        String rankingRule = upper(configuration.getRankingRule(), "ranking rule", indexCode);
        String selectionRule = upper(configuration.getSelectionRule(), "selection rule", indexCode);
        String bufferRule = upper(configuration.getBufferRule(), "buffer rule", indexCode);
        if (!"FFMCAP".equals(rankingRule)) {
            throw new IllegalArgumentException("Unsupported ranking rule: " + configuration.getRankingRule());
        }
        if (!"TOP_N".equals(selectionRule) && !"TOP_N_WITH_BUFFER".equals(selectionRule)) {
            throw new IllegalArgumentException("Unsupported selection rule: " + configuration.getSelectionRule());
        }
        if (!Set.of("NONE", "NOOP", "NO_OP", "CONFIGURABLE", "TOP_N_WITH_BUFFER").contains(bufferRule)) {
            throw new IllegalArgumentException("Unsupported buffer rule: " + configuration.getBufferRule());
        }
        IndexDefinition definition = new IndexDefinition(
                new IndexCode(configuration.getIndexCode() == null ? indexCode : configuration.getIndexCode()),
                configuration.getName(),
                configuration.getConstituentCount(),
                configuration.getMaxWeight(),
                rankingRule,
                selectionRule,
                bufferRule,
                configuration.getReviewPeriod().trim().toUpperCase(Locale.ROOT),
                new ReviewDates(configuration.getCutOffDate(), configuration.getReviewDate()),
                configuration.getTieBreakers(),
                configuration.isEnabled(),
                configuration.getBufferRetentionRank());
        log.info("Loaded index configuration indexCode={} reviewPeriod={} constituentCount={} maxWeight={}",
                definition.indexCode(), definition.reviewPeriod(), definition.methodology().constituentCount(), definition.methodology().maxWeight());
        return definition;
    }

    private String upper(String value, String field, String indexCode) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing " + field + " for index " + indexCode);
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}

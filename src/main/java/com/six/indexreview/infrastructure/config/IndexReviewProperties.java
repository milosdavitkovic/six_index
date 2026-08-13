package com.six.indexreview.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Core IndexReviewProperties component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
/**
 * @author Milos Davitkovic
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "index-review")
public class IndexReviewProperties {
    private PrecisionProperties precision = new PrecisionProperties();
    private Map<String, IndexConfiguration> indices = new LinkedHashMap<>();
}

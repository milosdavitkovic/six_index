package com.six.indexreview.infrastructure.config;

import com.six.indexreview.domain.service.PrecisionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Core IndexReviewConfiguration component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
/**
 * @author Milos Davitkovic
 */
@Configuration
public class IndexReviewConfiguration {

    @Bean
    PrecisionPolicy precisionPolicy(IndexReviewProperties properties) {
        PrecisionProperties precision = properties.getPrecision();
        return new PrecisionPolicy(precision.getInternalScale(), precision.getOutputScale(), precision.getRoundingMode());
    }

    @Bean
    Clock reviewClock() {
        return Clock.systemUTC();
    }
}

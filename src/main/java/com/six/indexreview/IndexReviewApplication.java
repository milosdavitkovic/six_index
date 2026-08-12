package com.six.indexreview;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Named application entry point for the index-review service.  The original
 * repository entry point is retained for compatibility with the generated
 * project and delegates to the same Spring Boot configuration.
 */
/**
 * Core IndexReviewApplication component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
@SpringBootApplication(scanBasePackages = "com.six.indexreview")
@ConfigurationPropertiesScan("com.six.indexreview")
@EntityScan("com.six.indexreview.infrastructure.persistence.entity")
@EnableJpaRepositories("com.six.indexreview.infrastructure.persistence.repository")
public class IndexReviewApplication {

    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(IndexReviewApplication.class, args);
    }
}

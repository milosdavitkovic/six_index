package com.six.indexreview;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

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
/**
 * @author Milos Davitkovic
 */
@SpringBootApplication(scanBasePackages = "com.six.indexreview")
@ConfigurationPropertiesScan("com.six.indexreview")
public class IndexReviewApplication {

    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(IndexReviewApplication.class, args);
    }
}

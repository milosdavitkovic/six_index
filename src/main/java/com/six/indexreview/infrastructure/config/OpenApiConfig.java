package com.six.indexreview.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Provides the service metadata shown by the generated OpenAPI document. */
@Configuration
public class OpenApiConfig {
    /** Creates the OpenAPI metadata for the index review API. */
    @Bean
    public OpenAPI indexReviewOpenApi() {
        return new OpenAPI().info(new Info().title("SIX Index Review API")
                .version("v1").description("Deterministic SIX index review and reporting service."));
    }
}

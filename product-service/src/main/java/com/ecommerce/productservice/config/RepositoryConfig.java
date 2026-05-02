package com.ecommerce.productservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Separate JPA and Elasticsearch repositories to prevent Spring Data
 * from trying to create JPA implementations for ES repositories and vice-versa.
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.ecommerce.productservice.repository",
    excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        value = com.ecommerce.productservice.repository.ProductSearchRepository.class
    )
)
@EnableElasticsearchRepositories(
    basePackages = "com.ecommerce.productservice.repository",
    includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        value = com.ecommerce.productservice.repository.ProductSearchRepository.class
    )
)
public class RepositoryConfig {
}

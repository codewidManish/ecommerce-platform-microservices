package com.ecommerce.cartservice.config;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.Optional;

@FeignClient(name = "product-service", fallback = ProductServiceClient.ProductServiceFallback.class)
public interface ProductServiceClient {

    @GetMapping("/api/v1/products/{id}")
    ProductInfo getProduct(@PathVariable Long id);

    record ProductInfo(
        Long id,
        String name,
        String sku,
        BigDecimal price,
        BigDecimal originalPrice,
        String imageUrl,
        String status
    ) {}

    @Component
    @Slf4j
    class ProductServiceFallback implements ProductServiceClient {
        @Override
        public ProductInfo getProduct(Long id) {
            log.warn("Product service fallback triggered for productId: {}", id);
            return null;
        }
    }
}

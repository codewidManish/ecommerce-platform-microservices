package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.*;
import com.ecommerce.productservice.entity.*;
import com.ecommerce.productservice.exception.*;
import com.ecommerce.productservice.mapper.ProductMapper;
import com.ecommerce.productservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ElasticsearchOperations elasticsearchOperations;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ──────────────── CREATE ────────────────

    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new ProductAlreadyExistsException("SKU already exists: " + request.getSku());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + request.getCategoryId()));

        Product product = productMapper.toEntity(request);
        product.setCategory(category);

        if (request.getOriginalPrice() != null && request.getPrice() != null) {
            int discount = request.getOriginalPrice().subtract(request.getPrice())
                    .divide(request.getOriginalPrice(), 2, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).intValue();
            product.setDiscountPercent(discount);
        }

        Product saved = productRepository.save(product);
        indexProductInElasticsearch(saved);

        kafkaTemplate.send("product-events", saved.getId().toString(), Map.of(
                "eventType", "PRODUCT_CREATED",
                "productId", saved.getId(),
                "sku", saved.getSku()
        ));

        log.info("Product created: {} [SKU: {}]", saved.getId(), saved.getSku());
        return productMapper.toResponse(saved);
    }

    // ──────────────── READ ────────────────

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException("Product not found for SKU: " + sku));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(ProductFilterRequest filter, Pageable pageable) {
        if (filter.hasSearchQuery()) {
            return searchProducts(filter.getQuery(), pageable);
        }
        return productRepository
                .findWithFilters(filter.getMinPrice(), filter.getMaxPrice(), filter.getBrand(), filter.getCategoryId(), pageable)
                .map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + categoryId));
        return productRepository
                .findByCategoryIdAndStatus(categoryId, Product.ProductStatus.ACTIVE, pageable)
                .map(productMapper::toResponse);
    }

    // ──────────────── ELASTICSEARCH SEARCH ────────────────

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String queryText, Pageable pageable) {
        Query multiMatchQuery = Query.of(q -> q.multiMatch(mm -> mm
                .query(queryText)
                .fields("name^3", "description^1", "brand^2", "tags^1", "categoryName^1")
                .fuzziness("AUTO")
        ));

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(multiMatchQuery)
                .withPageable(pageable)
                .build();

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(searchQuery, ProductDocument.class);

        List<ProductResponse> responses = searchHits.getSearchHits().stream()
                .map(hit -> productMapper.documentToResponse(hit.getContent()))
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, searchHits.getTotalHits());
    }

    // ──────────────── UPDATE ────────────────

    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        if (request.getBrand() != null) product.setBrand(request.getBrand());
        if (request.getTags() != null) product.setTags(request.getTags());
        if (request.getCategoryId() != null) {
            Category cat = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + request.getCategoryId()));
            product.setCategory(cat);
        }

        Product updated = productRepository.save(product);
        indexProductInElasticsearch(updated);

        log.info("Product updated: {}", id);
        return productMapper.toResponse(updated);
    }

    public void updateProductStatus(Long id, Product.ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        product.setStatus(status);
        productRepository.save(product);

        // Update Elasticsearch status
        ProductDocument doc = productSearchRepository.findById(id.toString()).orElse(null);
        if (doc != null) {
            doc.setStatus(status.name());
            productSearchRepository.save(doc);
        }
        log.info("Product {} status set to {}", id, status);
    }

    // ──────────────── DELETE ────────────────

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        product.setStatus(Product.ProductStatus.DISCONTINUED);
        productRepository.save(product);
        productSearchRepository.deleteByProductId(id);
        log.info("Product soft-deleted: {}", id);
    }

    // ──────────────── RATING UPDATE ────────────────

    public void updateProductRating(Long productId, BigDecimal newAvgRating, int totalReviews) {
        productRepository.findById(productId).ifPresent(p -> {
            p.setAverageRating(newAvgRating);
            p.setTotalReviews(totalReviews);
            productRepository.save(p);
            indexProductInElasticsearch(p);
        });
    }

    // ──────────────── PRIVATE HELPERS ────────────────

    private void indexProductInElasticsearch(Product product) {
        try {
            ProductDocument doc = ProductDocument.builder()
                    .id(product.getId().toString())
                    .productId(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .sku(product.getSku())
                    .price(product.getPrice())
                    .categoryId(product.getCategory().getId())
                    .categoryName(product.getCategory().getName())
                    .brand(product.getBrand())
                    .tags(product.getTags())
                    .averageRating(product.getAverageRating())
                    .status(product.getStatus().name())
                    .imageUrl(product.getImageUrl())
                    .createdAt(product.getCreatedAt())
                    .build();
            productSearchRepository.save(doc);
        } catch (Exception e) {
            log.error("Failed to index product {} in Elasticsearch: {}", product.getId(), e.getMessage());
        }
    }
}

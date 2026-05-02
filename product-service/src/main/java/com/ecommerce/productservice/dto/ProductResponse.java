package com.ecommerce.productservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer discountPercent;
    private Long categoryId;
    private String categoryName;
    private String brand;
    private String imageUrl;
    private List<String> additionalImages;
    private List<String> tags;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

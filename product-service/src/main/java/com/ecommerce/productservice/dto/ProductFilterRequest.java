package com.ecommerce.productservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {

    private String query;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String brand;
    private Long categoryId;

    public boolean hasSearchQuery() {
        return query != null && !query.isBlank();
    }
}

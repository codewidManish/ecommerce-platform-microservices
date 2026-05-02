package com.ecommerce.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @Size(max = 200)
    private String name;

    private String description;

    @DecimalMin("0.01")
    private BigDecimal price;

    private Long categoryId;

    @Size(max = 100)
    private String brand;

    private String imageUrl;
    private List<String> tags;
}

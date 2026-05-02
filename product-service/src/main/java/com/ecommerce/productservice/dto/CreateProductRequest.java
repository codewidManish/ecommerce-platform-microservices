package com.ecommerce.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    private String description;

    @NotBlank
    @Size(max = 50)
    private String sku;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotNull
    private Long categoryId;

    @Size(max = 100)
    private String brand;

    private String imageUrl;
    private List<String> additionalImages;
    private List<String> tags;
    private Integer weightGrams;
    private Long vendorId;
}

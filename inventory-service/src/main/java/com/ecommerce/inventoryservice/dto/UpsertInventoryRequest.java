package com.ecommerce.inventoryservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpsertInventoryRequest {
    @NotNull  private Long productId;
    @NotBlank private String productSku;
    @Min(0)   private int quantity;
    private Integer lowStockThreshold;
    private String warehouseLocation;
}

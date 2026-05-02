package com.ecommerce.inventoryservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryResponse {
    private Long id;
    private Long productId;
    private String productSku;
    private int quantity;
    private int reservedQuantity;
    private int availableQuantity;
    private boolean lowStock;
    private int lowStockThreshold;
    private String warehouseLocation;
    private LocalDateTime updatedAt;
}

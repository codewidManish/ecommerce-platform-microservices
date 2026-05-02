package com.ecommerce.cartservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AddCartItemRequest {
    @NotNull(message = "Product ID is required") private Long productId;
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100") private int quantity;
}

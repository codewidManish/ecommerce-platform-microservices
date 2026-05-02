package com.ecommerce.cartservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponse {
    private String cartId;
    private Long userId;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
    private int totalItems;
    private LocalDateTime updatedAt;
}

package com.ecommerce.cartservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CartItemResponse {
    private Long productId;
    private String productName;
    private String productSku;
    private String imageUrl;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal originalPrice;
    private BigDecimal subtotal;
}

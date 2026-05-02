package com.ecommerce.orderservice.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderCreatedEvent {
    private String eventType = "ORDER_CREATED";
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private List<Map<String, Object>> items;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp = LocalDateTime.now();
}

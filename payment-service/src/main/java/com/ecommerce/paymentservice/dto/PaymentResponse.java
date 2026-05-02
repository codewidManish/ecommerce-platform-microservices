package com.ecommerce.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {
    private Long id;
    private String idempotencyKey;
    private Long orderId;
    private String orderNumber;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String transactionId;
    private String failureReason;
    private String refundId;
    private BigDecimal refundedAmount;
    private LocalDateTime createdAt;
}

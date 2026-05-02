package com.ecommerce.paymentservice.dto;

import com.ecommerce.paymentservice.entity.Payment;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePaymentRequest {
    @NotNull  private Long orderId;
    @NotBlank private String orderNumber;
    @NotNull  private Long userId;
    @NotNull  @DecimalMin("0.01") private BigDecimal amount;
    @NotNull  private Payment.PaymentMethod paymentMethod;
}

package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.*;
import com.ecommerce.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing and refunds")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Initiate payment for an order")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            idempotencyKey = UUID.randomUUID().toString();
        }
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.initiatePayment(request, idempotencyKey), "Payment processed"));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment details by order ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getPaymentByOrderId(orderId), "Payment retrieved"));
    }

    @PostMapping("/order/{orderId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Refund a payment [ADMIN]")
    public ResponseEntity<ApiResponse<PaymentResponse>> refund(
            @PathVariable Long orderId,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.refundPayment(orderId, amount), "Refund processed"));
    }
}

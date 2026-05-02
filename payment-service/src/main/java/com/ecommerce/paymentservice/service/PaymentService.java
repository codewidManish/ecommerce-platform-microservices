package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.*;
import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.exception.*;
import com.ecommerce.paymentservice.gateway.MockPaymentGateway;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MockPaymentGateway gateway;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ─── Listen for PROCESS_PAYMENT commands from Order Saga ───

    @KafkaListener(topics = "payment-commands", groupId = "payment-service-group")
    @Transactional
    public void handlePaymentCommand(Map<String, Object> command) {
        String commandType = (String) command.get("commandType");
        if (!"PROCESS_PAYMENT".equals(commandType)) return;

        Long orderId = Long.valueOf(command.get("orderId").toString());
        String orderNumber = (String) command.get("orderNumber");
        Long userId = Long.valueOf(command.get("userId").toString());
        BigDecimal amount = new BigDecimal(command.get("amount").toString());

        log.info("[PAYMENT] Received PROCESS_PAYMENT command for orderId: {}", orderId);
        processPaymentInternal(orderId, orderNumber, userId, amount, "CARD");
    }

    // ─── Direct API payment initiation (for REST-based flow) ───

    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request, String idempotencyKey) {
        // Idempotency check - prevent duplicate payments
        return paymentRepository.findByIdempotencyKey(idempotencyKey)
                .map(existing -> {
                    log.info("[PAYMENT] Returning cached response for idempotency key: {}", idempotencyKey);
                    return mapToResponse(existing);
                })
                .orElseGet(() -> processPaymentInternal(
                        request.getOrderId(), request.getOrderNumber(),
                        request.getUserId(), request.getAmount(),
                        request.getPaymentMethod().name()
                ));
    }

    // ─── Core payment processing ───

    private PaymentResponse processPaymentInternal(Long orderId, String orderNumber,
                                                    Long userId, BigDecimal amount,
                                                    String method) {
        // Check no successful payment already exists
        paymentRepository.findByOrderId(orderId).ifPresent(existing -> {
            if (existing.getStatus() == Payment.PaymentStatus.COMPLETED) {
                throw new PaymentAlreadyProcessedException("Payment already completed for order: " + orderId);
            }
        });

        String idempotencyKey = "PAY-" + orderId + "-" + UUID.randomUUID().toString().substring(0, 8);

        Payment payment = Payment.builder()
                .idempotencyKey(idempotencyKey)
                .orderId(orderId)
                .orderNumber(orderNumber)
                .userId(userId)
                .amount(amount)
                .paymentMethod(Payment.PaymentMethod.valueOf(method))
                .status(Payment.PaymentStatus.PROCESSING)
                .build();

        payment = paymentRepository.save(payment);

        // Call mock gateway
        MockPaymentGateway.GatewayResponse gwResponse = gateway.processPayment(
                new MockPaymentGateway.GatewayRequest(orderId, orderNumber, userId, amount, "INR", method)
        );

        payment.setGatewayResponse(gwResponse.rawResponse());

        if (gwResponse.success()) {
            payment.setTransactionId(gwResponse.transactionId());
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            // Notify saga: payment success
            kafkaTemplate.send("payment-events", orderId.toString(), Map.of(
                    "eventType", "PAYMENT_COMPLETED",
                    "orderId", orderId,
                    "paymentId", payment.getId().toString(),
                    "transactionId", gwResponse.transactionId()
            ));
            log.info("[PAYMENT] Payment COMPLETED for orderId: {}, txnId: {}", orderId, gwResponse.transactionId());
        } else {
            payment.setFailureReason(gwResponse.errorReason());
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);

            // Notify saga: payment failed
            kafkaTemplate.send("payment-events", orderId.toString(), Map.of(
                    "eventType", "PAYMENT_FAILED",
                    "orderId", orderId,
                    "reason", gwResponse.errorReason()
            ));
            log.warn("[PAYMENT] Payment FAILED for orderId: {}. Reason: {}", orderId, gwResponse.errorReason());
        }

        return mapToResponse(payment);
    }

    // ─── Refund ───

    @Transactional
    public PaymentResponse refundPayment(Long orderId, BigDecimal refundAmount) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + orderId));

        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new PaymentRefundException("Cannot refund payment in status: " + payment.getStatus());
        }
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new PaymentRefundException("Refund amount exceeds payment amount");
        }

        MockPaymentGateway.GatewayResponse refundResponse = gateway.processRefund(
                payment.getTransactionId(), refundAmount);

        if (refundResponse.success()) {
            payment.setRefundId(refundResponse.transactionId());
            payment.setRefundedAmount(refundAmount);
            payment.setStatus(refundAmount.compareTo(payment.getAmount()) == 0
                    ? Payment.PaymentStatus.REFUNDED
                    : Payment.PaymentStatus.PARTIALLY_REFUNDED);
            paymentRepository.save(payment);

            kafkaTemplate.send("notification-events", orderId.toString(), Map.of(
                    "eventType", "PAYMENT_REFUNDED",
                    "orderId", orderId,
                    "refundAmount", refundAmount,
                    "userId", payment.getUserId()
            ));
            log.info("[PAYMENT] Refund of {} processed for orderId: {}", refundAmount, orderId);
        }

        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + orderId));
    }

    private PaymentResponse mapToResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .idempotencyKey(p.getIdempotencyKey())
                .orderId(p.getOrderId())
                .orderNumber(p.getOrderNumber())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus().name())
                .paymentMethod(p.getPaymentMethod() != null ? p.getPaymentMethod().name() : null)
                .transactionId(p.getTransactionId())
                .failureReason(p.getFailureReason())
                .refundId(p.getRefundId())
                .refundedAmount(p.getRefundedAmount())
                .createdAt(p.getCreatedAt())
                .build();
    }
}

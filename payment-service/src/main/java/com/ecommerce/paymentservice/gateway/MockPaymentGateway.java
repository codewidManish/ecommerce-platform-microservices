package com.ecommerce.paymentservice.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * Mock payment gateway simulating Razorpay / Stripe behavior.
 * In production, replace this with actual SDK integration.
 */
@Component
@Slf4j
public class MockPaymentGateway {

    private static final Random RANDOM = new Random();

    public GatewayResponse processPayment(GatewayRequest request) {
        log.info("[GATEWAY] Processing payment for orderId: {}, amount: {} {}",
                request.orderId(), request.amount(), request.currency());

        // Simulate network latency
        simulateLatency(200, 800);

        // Simulate 90% success rate (realistic for mocking)
        boolean success = RANDOM.nextInt(100) < 90;

        if (success) {
            String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
            log.info("[GATEWAY] Payment SUCCESS. TxnId: {}", txnId);
            return new GatewayResponse(true, txnId, "CAPTURED", null,
                    """
                    {"status":"captured","transaction_id":"%s","gateway":"mock-razorpay","mode":"test"}
                    """.formatted(txnId));
        } else {
            String reason = pickFailureReason();
            log.warn("[GATEWAY] Payment FAILED. Reason: {}", reason);
            return new GatewayResponse(false, null, "FAILED", reason,
                    """
                    {"status":"failed","error_code":"PAYMENT_DECLINED","description":"%s"}
                    """.formatted(reason));
        }
    }

    public GatewayResponse processRefund(String transactionId, BigDecimal amount) {
        log.info("[GATEWAY] Processing refund for txnId: {}, amount: {}", transactionId, amount);
        simulateLatency(100, 500);

        String refundId = "RFD-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        return new GatewayResponse(true, refundId, "REFUNDED", null,
                """
                {"status":"refunded","refund_id":"%s","amount":"%s"}
                """.formatted(refundId, amount));
    }

    private void simulateLatency(int minMs, int maxMs) {
        try {
            Thread.sleep(minMs + RANDOM.nextInt(maxMs - minMs));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String pickFailureReason() {
        String[] reasons = {
            "Insufficient funds",
            "Card declined by bank",
            "Transaction limit exceeded",
            "Invalid card details",
            "Bank server timeout"
        };
        return reasons[RANDOM.nextInt(reasons.length)];
    }

    public record GatewayRequest(Long orderId, String orderNumber, Long userId,
                                  BigDecimal amount, String currency, String paymentMethod) {}

    public record GatewayResponse(boolean success, String transactionId, String status,
                                   String errorReason, String rawResponse) {}
}

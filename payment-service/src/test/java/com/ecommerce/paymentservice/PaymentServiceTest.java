package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.exception.PaymentAlreadyProcessedException;
import com.ecommerce.paymentservice.gateway.MockPaymentGateway;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private MockPaymentGateway gateway;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;
    @InjectMocks private PaymentService paymentService;

    @Test
    @DisplayName("Should process successful payment and publish PAYMENT_COMPLETED")
    void shouldProcessSuccessfulPayment() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(gateway.processPayment(any())).thenReturn(
                new MockPaymentGateway.GatewayResponse(true, "TXN-ABC123", "CAPTURED", null,
                        "{\"status\":\"captured\"}")
        );
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(null);

        var req = new com.ecommerce.paymentservice.dto.InitiatePaymentRequest();
        req.setOrderId(1L); req.setOrderNumber("ORD-001");
        req.setUserId(1L);  req.setAmount(new BigDecimal("9999.00"));
        req.setPaymentMethod(Payment.PaymentMethod.CARD);

        var resp = paymentService.initiatePayment(req, "idem-key-001");

        assertThat(resp.getStatus()).isEqualTo("COMPLETED");
        assertThat(resp.getTransactionId()).isEqualTo("TXN-ABC123");
        verify(kafkaTemplate).send(eq("payment-events"), eq("1"), any());
    }

    @Test
    @DisplayName("Should publish PAYMENT_FAILED on gateway failure")
    void shouldHandlePaymentFailure() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0); p.setId(1L); return p;
        });
        when(gateway.processPayment(any())).thenReturn(
                new MockPaymentGateway.GatewayResponse(false, null, "FAILED", "Insufficient funds",
                        "{\"status\":\"failed\"}")
        );
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(null);

        var req = new com.ecommerce.paymentservice.dto.InitiatePaymentRequest();
        req.setOrderId(1L); req.setOrderNumber("ORD-001");
        req.setUserId(1L);  req.setAmount(new BigDecimal("9999.00"));
        req.setPaymentMethod(Payment.PaymentMethod.CARD);

        var resp = paymentService.initiatePayment(req, "idem-key-002");

        assertThat(resp.getStatus()).isEqualTo("FAILED");
        assertThat(resp.getFailureReason()).isEqualTo("Insufficient funds");
        verify(kafkaTemplate).send(eq("payment-events"), eq("1"), any());
    }

    @Test
    @DisplayName("Should return cached response for duplicate idempotency key")
    void shouldReturnCachedForDuplicateKey() {
        Payment existing = Payment.builder()
                .id(1L).idempotencyKey("idem-key-003")
                .orderId(1L).orderNumber("ORD-001")
                .status(Payment.PaymentStatus.COMPLETED)
                .amount(new BigDecimal("9999.00")).currency("INR")
                .build();

        when(paymentRepository.findByIdempotencyKey("idem-key-003"))
                .thenReturn(Optional.of(existing));

        var req = new com.ecommerce.paymentservice.dto.InitiatePaymentRequest();
        req.setOrderId(1L); req.setOrderNumber("ORD-001");
        req.setUserId(1L);  req.setAmount(new BigDecimal("9999.00"));
        req.setPaymentMethod(Payment.PaymentMethod.CARD);

        var resp = paymentService.initiatePayment(req, "idem-key-003");

        assertThat(resp.getIdempotencyKey()).isEqualTo("idem-key-003");
        verify(gateway, never()).processPayment(any());  // gateway NOT called again
    }
}

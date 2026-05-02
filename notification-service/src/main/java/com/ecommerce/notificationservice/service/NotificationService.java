package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.entity.NotificationLog;
import com.ecommerce.notificationservice.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationLogRepository logRepository;
    private final EmailSimulator emailSimulator;
    private final SmsSimulator smsSimulator;

    public void sendWelcomeEmail(Map<String, Object> event) {
        Long userId = Long.valueOf(event.get("userId").toString());
        String email = (String) event.get("email");
        String firstName = (String) event.get("firstName");

        String subject = "Welcome to ShopEasy, " + firstName + "!";
        String body = buildWelcomeEmailBody(firstName);

        emailSimulator.send(email, subject, body);
        saveLog(userId, "EMAIL", "WELCOME", email, subject, "SENT");
        log.info("[NOTIFICATION] Welcome email sent to userId: {}", userId);
    }

    public void sendOrderConfirmation(Map<String, Object> event) {
        Long orderId = Long.valueOf(event.get("orderId").toString());
        Long userId = Long.valueOf(event.get("userId").toString());
        String orderNumber = (String) event.get("orderNumber");
        Object totalAmount = event.get("totalAmount");

        String subject = "Order Confirmed: " + orderNumber;
        String body = """
            Your order %s has been confirmed!
            Total: ₹%s
            We'll notify you when it ships.
            """.formatted(orderNumber, totalAmount);

        // In production, look up user email from User Service
        String mockEmail = "user" + userId + "@example.com";
        emailSimulator.send(mockEmail, subject, body);
        smsSimulator.send("+91-9000000000", "Order " + orderNumber + " confirmed. Total: ₹" + totalAmount);
        saveLog(userId, "EMAIL", "ORDER_CONFIRMED", mockEmail, subject, "SENT");
        log.info("[NOTIFICATION] Order confirmation sent for orderId: {}", orderId);
    }

    public void sendOrderCancellation(Map<String, Object> event) {
        Long userId = Long.valueOf(event.get("userId").toString());
        String orderNumber = (String) event.get("orderNumber");
        String reason = (String) event.getOrDefault("reason", "Requested by user");

        String subject = "Order Cancelled: " + orderNumber;
        String body = "Your order %s has been cancelled.\nReason: %s".formatted(orderNumber, reason);

        String mockEmail = "user" + userId + "@example.com";
        emailSimulator.send(mockEmail, subject, body);
        saveLog(userId, "EMAIL", "ORDER_CANCELLED", mockEmail, subject, "SENT");
        log.info("[NOTIFICATION] Cancellation email sent for order: {}", orderNumber);
    }

    public void sendRefundConfirmation(Map<String, Object> event) {
        Long userId = Long.valueOf(event.get("userId").toString());
        Long orderId = Long.valueOf(event.get("orderId").toString());
        Object refundAmount = event.get("refundAmount");

        String subject = "Refund Processed";
        String body = "A refund of ₹%s has been processed for order #%s. It will reflect in 3-5 business days.".formatted(refundAmount, orderId);

        String mockEmail = "user" + userId + "@example.com";
        emailSimulator.send(mockEmail, subject, body);
        saveLog(userId, "EMAIL", "REFUND_PROCESSED", mockEmail, subject, "SENT");
        log.info("[NOTIFICATION] Refund notification sent for userId: {}", userId);
    }

    public void sendShippingUpdate(Map<String, Object> event) {
        Long userId = Long.valueOf(event.get("userId").toString());
        String orderNumber = (String) event.get("orderNumber");
        String trackingId = (String) event.getOrDefault("trackingId", "N/A");

        String subject = "Order Shipped: " + orderNumber;
        String body = "Your order %s has been shipped! Tracking ID: %s".formatted(orderNumber, trackingId);

        String mockEmail = "user" + userId + "@example.com";
        emailSimulator.send(mockEmail, subject, body);
        smsSimulator.send("+91-9000000000", "Order " + orderNumber + " shipped. Track: " + trackingId);
        saveLog(userId, "EMAIL", "ORDER_SHIPPED", mockEmail, subject, "SENT");
    }

    public void sendDeliveryConfirmation(Map<String, Object> event) {
        Long userId = Long.valueOf(event.get("userId").toString());
        String orderNumber = (String) event.get("orderNumber");

        String subject = "Order Delivered: " + orderNumber;
        String body = "Your order %s has been delivered! Enjoy your purchase.".formatted(orderNumber);

        String mockEmail = "user" + userId + "@example.com";
        emailSimulator.send(mockEmail, subject, body);
        saveLog(userId, "EMAIL", "ORDER_DELIVERED", mockEmail, subject, "SENT");
    }

    public void sendLowStockAlert(Map<String, Object> event) {
        Long productId = Long.valueOf(event.get("productId").toString());
        int available = Integer.parseInt(event.get("availableQty").toString());

        String adminEmail = "admin@shopeasy.com";
        String subject = "⚠️ Low Stock Alert: Product #" + productId;
        String body = "Product ID %d has only %d units left in stock.".formatted(productId, available);

        emailSimulator.send(adminEmail, subject, body);
        saveLog(0L, "EMAIL", "LOW_STOCK_ALERT", adminEmail, subject, "SENT");
        log.warn("[NOTIFICATION] Low stock alert sent for productId: {}", productId);
    }

    private String buildWelcomeEmailBody(String firstName) {
        return """
            Hi %s,
            
            Welcome to ShopEasy! 🎉
            
            Your account has been created successfully.
            Start exploring thousands of products at the best prices.
            
            Happy Shopping!
            The ShopEasy Team
            """.formatted(firstName);
    }

    private void saveLog(Long userId, String channel, String type, String recipient, String subject, String status) {
        try {
            logRepository.save(NotificationLog.builder()
                    .userId(userId)
                    .channel(channel)
                    .notificationType(type)
                    .recipient(recipient)
                    .subject(subject)
                    .status(status)
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to save notification log: {}", e.getMessage());
        }
    }
}

package com.ecommerce.notificationservice.consumer;

import com.ecommerce.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "notification-events", groupId = "notification-service-group")
    public void handleNotificationEvent(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        log.info("[NOTIFICATION] Received event: {}", eventType);

        switch (eventType) {
            case "ORDER_CONFIRMED"   -> notificationService.sendOrderConfirmation(event);
            case "ORDER_CANCELLED"  -> notificationService.sendOrderCancellation(event);
            case "PAYMENT_REFUNDED" -> notificationService.sendRefundConfirmation(event);
            case "ORDER_SHIPPED"    -> notificationService.sendShippingUpdate(event);
            case "ORDER_DELIVERED"  -> notificationService.sendDeliveryConfirmation(event);
            default -> log.warn("[NOTIFICATION] Unknown event type: {}", eventType);
        }
    }

    @KafkaListener(topics = "user-events", groupId = "notification-service-group")
    public void handleUserEvent(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        if ("USER_REGISTERED".equals(eventType)) {
            notificationService.sendWelcomeEmail(event);
        }
    }

    @KafkaListener(topics = "inventory-alerts", groupId = "notification-service-group")
    public void handleInventoryAlert(Map<String, Object> event) {
        if ("LOW_STOCK_ALERT".equals(event.get("eventType"))) {
            notificationService.sendLowStockAlert(event);
        }
    }
}

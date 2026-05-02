package com.ecommerce.orderservice.saga;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.event.*;
import com.ecommerce.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Choreography-based Saga for Order lifecycle:
 *
 *  [OrderService] --ORDER_CREATED--> [InventoryService]
 *  [InventoryService] --INVENTORY_RESERVED--> [PaymentService]
 *  [PaymentService] --PAYMENT_COMPLETED--> [OrderService]  → CONFIRMED
 *
 *  Compensating transactions on failure:
 *  [PaymentService] --PAYMENT_FAILED--> [InventoryService] (release stock)
 *                                    --> [OrderService]     → CANCELLED
 *  [InventoryService] --INVENTORY_FAILED--> [OrderService]  → CANCELLED
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaOrchestrator {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderRepository orderRepository;

    // ─── Step 1: Order created → request inventory reservation ───

    public void startOrderSaga(Order order) {
        log.info("[SAGA] Starting saga for order: {}", order.getOrderNumber());

        var event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .items(order.getItems().stream().map(item -> Map.of(
                        "productId", item.getProductId(),
                        "quantity", item.getQuantity(),
                        "unitPrice", item.getUnitPrice()
                )).toList())
                .totalAmount(order.getTotalAmount())
                .build();

        kafkaTemplate.send("order-events", order.getId().toString(), event);
        log.info("[SAGA] Sent ORDER_CREATED event for orderId: {}", order.getId());
    }

    // ─── Step 2a: Inventory reserved → initiate payment ───

    @KafkaListener(topics = "inventory-events", groupId = "order-saga-group")
    @Transactional
    public void handleInventoryEvent(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        Long orderId = Long.valueOf(event.get("orderId").toString());

        if ("INVENTORY_RESERVED".equals(eventType)) {
            log.info("[SAGA] Inventory reserved for orderId: {}. Initiating payment.", orderId);

            orderRepository.findById(orderId).ifPresent(order -> {
                order.addStatusHistory(Order.OrderStatus.PROCESSING, "Inventory reserved, initiating payment");
                orderRepository.save(order);

                kafkaTemplate.send("payment-commands", orderId.toString(), Map.of(
                        "commandType", "PROCESS_PAYMENT",
                        "orderId", orderId,
                        "orderNumber", order.getOrderNumber(),
                        "userId", order.getUserId(),
                        "amount", order.getTotalAmount()
                ));
            });

        } else if ("INVENTORY_RESERVATION_FAILED".equals(eventType)) {
            log.warn("[SAGA] Inventory reservation FAILED for orderId: {}. Cancelling order.", orderId);
            cancelOrder(orderId, "Inventory reservation failed: " + event.get("reason"));
        }
    }

    // ─── Step 2b: Payment result → confirm or cancel order ───

    @KafkaListener(topics = "payment-events", groupId = "order-saga-group")
    @Transactional
    public void handlePaymentEvent(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        Long orderId = Long.valueOf(event.get("orderId").toString());

        if ("PAYMENT_COMPLETED".equals(eventType)) {
            log.info("[SAGA] Payment completed for orderId: {}. Confirming order.", orderId);

            orderRepository.findById(orderId).ifPresent(order -> {
                order.setPaymentId((String) event.get("paymentId"));
                order.setPaymentStatus(Order.PaymentStatus.PAID);
                order.addStatusHistory(Order.OrderStatus.CONFIRMED, "Payment successful. Order confirmed.");
                orderRepository.save(order);

                // Notify notification service
                kafkaTemplate.send("notification-events", orderId.toString(), Map.of(
                        "eventType", "ORDER_CONFIRMED",
                        "orderId", orderId,
                        "orderNumber", order.getOrderNumber(),
                        "userId", order.getUserId(),
                        "totalAmount", order.getTotalAmount()
                ));
                log.info("[SAGA] Order {} confirmed successfully.", order.getOrderNumber());
            });

        } else if ("PAYMENT_FAILED".equals(eventType)) {
            log.warn("[SAGA] Payment FAILED for orderId: {}. Rolling back inventory & cancelling.", orderId);

            orderRepository.findById(orderId).ifPresent(order -> {
                order.setPaymentStatus(Order.PaymentStatus.FAILED);
                orderRepository.save(order);
            });

            // Compensating transaction: release inventory
            kafkaTemplate.send("inventory-commands", orderId.toString(), Map.of(
                    "commandType", "RELEASE_INVENTORY",
                    "orderId", orderId,
                    "reason", event.get("reason")
            ));

            cancelOrder(orderId, "Payment failed: " + event.get("reason"));
        }
    }

    // ─── Compensation: Cancel order ───

    private void cancelOrder(Long orderId, String reason) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.addStatusHistory(Order.OrderStatus.CANCELLED, reason);
            orderRepository.save(order);

            kafkaTemplate.send("notification-events", orderId.toString(), Map.of(
                    "eventType", "ORDER_CANCELLED",
                    "orderId", orderId,
                    "orderNumber", order.getOrderNumber(),
                    "userId", order.getUserId(),
                    "reason", reason
            ));
            log.info("[SAGA] Order {} cancelled. Reason: {}", order.getOrderNumber(), reason);
        });
    }
}

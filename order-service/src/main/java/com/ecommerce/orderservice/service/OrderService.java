package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.entity.*;
import com.ecommerce.orderservice.exception.*;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.saga.OrderSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderSagaOrchestrator sagaOrchestrator;
    private static final AtomicLong orderSeq = new AtomicLong(1000);

    // ──────────────── PLACE ORDER ────────────────

    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        // Build order items
        BigDecimal subtotal = request.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax       = subtotal.multiply(new BigDecimal("0.18"));  // 18% GST
        BigDecimal shipping  = subtotal.compareTo(new BigDecimal("500")) >= 0 ? BigDecimal.ZERO : new BigDecimal("50");
        BigDecimal total     = subtotal.add(tax).add(shipping);

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(userId)
                .shippingAddress(mapAddress(request.getShippingAddress()))
                .subtotal(subtotal)
                .taxAmount(tax)
                .shippingAmount(shipping)
                .totalAmount(total)
                .notes(request.getNotes())
                .build();

        // Add items
        request.getItems().forEach(itemReq -> {
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .productId(itemReq.getProductId())
                    .productName(itemReq.getProductName())
                    .productSku(itemReq.getProductSku())
                    .imageUrl(itemReq.getImageUrl())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .subtotal(itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())))
                    .build();
            order.getItems().add(item);
        });

        order.addStatusHistory(Order.OrderStatus.PENDING, "Order placed");
        Order saved = orderRepository.save(order);

        // Kick off distributed saga
        sagaOrchestrator.startOrderSaga(saved);

        log.info("Order placed: {} for user {}", saved.getOrderNumber(), userId);
        return mapToResponse(saved);
    }

    // ──────────────── GET ORDER ────────────────

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserIdWithDetails(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
        return mapToResponse(order);
    }

    // ──────────────── LIST ORDERS ────────────────

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Long userId, Order.OrderStatus status, Pageable pageable) {
        Page<Order> page = status != null
                ? orderRepository.findByUserIdAndStatus(userId, status, pageable)
                : orderRepository.findByUserId(userId, pageable);
        return page.map(this::mapToResponse);
    }

    // ──────────────── CANCEL ORDER ────────────────

    public OrderResponse cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findByIdAndUserIdWithDetails(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (!isCancellable(order.getStatus())) {
            throw new OrderNotCancellableException("Order cannot be cancelled in status: " + order.getStatus());
        }

        order.addStatusHistory(Order.OrderStatus.CANCELLED, reason != null ? reason : "Cancelled by user");
        Order saved = orderRepository.save(order);
        log.info("Order {} cancelled by user {}", order.getOrderNumber(), userId);
        return mapToResponse(saved);
    }

    // ──────────────── ADMIN: UPDATE STATUS ────────────────

    public OrderResponse updateOrderStatus(Long orderId, Order.OrderStatus newStatus, String comment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        order.addStatusHistory(newStatus, comment);
        Order saved = orderRepository.save(order);
        log.info("Order {} status updated to {} by admin", order.getOrderNumber(), newStatus);
        return mapToResponse(saved);
    }

    // ──────────────── HELPERS ────────────────

    private boolean isCancellable(Order.OrderStatus status) {
        return status == Order.OrderStatus.PENDING || status == Order.OrderStatus.CONFIRMED;
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "ORD-" + date + "-" + orderSeq.incrementAndGet();
    }

    private ShippingAddress mapAddress(ShippingAddressRequest req) {
        return ShippingAddress.builder()
                .fullName(req.getFullName())
                .streetAddress(req.getStreetAddress())
                .city(req.getCity())
                .state(req.getState())
                .postalCode(req.getPostalCode())
                .country(req.getCountry())
                .phone(req.getPhone())
                .build();
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .shippingAmount(order.getShippingAmount())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream().map(i -> OrderItemResponse.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .productSku(i.getProductSku())
                        .imageUrl(i.getImageUrl())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .subtotal(i.getSubtotal())
                        .build()).toList())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}

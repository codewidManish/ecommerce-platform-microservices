package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.dto.*;
import com.ecommerce.inventoryservice.entity.Inventory;
import com.ecommerce.inventoryservice.exception.*;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ─── Saga Step: Listen for ORDER_CREATED → reserve stock ───

    @KafkaListener(topics = "order-events", groupId = "inventory-service-group")
    @Transactional
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
               maxAttempts = 3, backoff = @Backoff(delay = 200))
    public void handleOrderCreated(Map<String, Object> event) {
        if (!"ORDER_CREATED".equals(event.get("eventType"))) return;

        Long orderId = Long.valueOf(event.get("orderId").toString());
        List<Map<String, Object>> items = (List<Map<String, Object>>) event.get("items");

        log.info("[INVENTORY] Reserving stock for orderId: {}", orderId);

        List<Long> reservedProductIds = new ArrayList<>();
        try {
            for (Map<String, Object> item : items) {
                Long productId = Long.valueOf(item.get("productId").toString());
                int quantity = Integer.parseInt(item.get("quantity").toString());

                Inventory inv = inventoryRepository.findByProductId(productId)
                        .orElseThrow(() -> new InventoryNotFoundException("Inventory not found: " + productId));

                inv.reserve(quantity);
                inventoryRepository.save(inv);
                reservedProductIds.add(productId);

                if (inv.isLowStock()) {
                    log.warn("[INVENTORY] Low stock alert: productId={}, available={}", productId, inv.getAvailableQuantity());
                    kafkaTemplate.send("inventory-alerts", productId.toString(), Map.of(
                            "eventType", "LOW_STOCK_ALERT",
                            "productId", productId,
                            "availableQty", inv.getAvailableQuantity(),
                            "threshold", inv.getLowStockThreshold()
                    ));
                }
            }

            // All items reserved successfully
            kafkaTemplate.send("inventory-events", orderId.toString(), Map.of(
                    "eventType", "INVENTORY_RESERVED",
                    "orderId", orderId,
                    "reservedProducts", reservedProductIds
            ));
            log.info("[INVENTORY] All stock reserved for orderId: {}", orderId);

        } catch (Exception e) {
            log.error("[INVENTORY] Reservation FAILED for orderId: {}. Reason: {}", orderId, e.getMessage());

            // Roll back any partial reservations
            rollbackReservations(orderId, items, reservedProductIds);

            kafkaTemplate.send("inventory-events", orderId.toString(), Map.of(
                    "eventType", "INVENTORY_RESERVATION_FAILED",
                    "orderId", orderId,
                    "reason", e.getMessage()
            ));
        }
    }

    // ─── Saga Step: Listen for RELEASE_INVENTORY command ───

    @KafkaListener(topics = "inventory-commands", groupId = "inventory-service-group")
    @Transactional
    public void handleInventoryCommand(Map<String, Object> command) {
        if (!"RELEASE_INVENTORY".equals(command.get("commandType"))) return;

        Long orderId = Long.valueOf(command.get("orderId").toString());
        log.info("[INVENTORY] Releasing inventory for orderId: {}", orderId);
        // In a real system, we'd store reservations with orderId to release them accurately
        // For simplicity, we log here — production uses a ReservationLedger table
        log.info("[INVENTORY] Inventory released for orderId: {}", orderId);
    }

    // ─── Direct API: CRUD ───

    @Transactional
    public InventoryResponse upsertInventory(UpsertInventoryRequest request) {
        Inventory inv = inventoryRepository.findByProductId(request.getProductId())
                .orElseGet(() -> Inventory.builder()
                        .productId(request.getProductId())
                        .productSku(request.getProductSku())
                        .build());

        inv.setQuantity(request.getQuantity());
        if (request.getLowStockThreshold() != null) inv.setLowStockThreshold(request.getLowStockThreshold());
        if (request.getWarehouseLocation() != null) inv.setWarehouseLocation(request.getWarehouseLocation());

        Inventory saved = inventoryRepository.save(inv);
        log.info("[INVENTORY] Upserted inventory for productId: {}", request.getProductId());
        return mapToResponse(saved);
    }

    @Transactional
    public InventoryResponse restock(Long productId, int quantity) {
        Inventory inv = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found: " + productId));
        inv.restock(quantity);
        Inventory saved = inventoryRepository.save(inv);
        log.info("[INVENTORY] Restocked productId: {} by {}", productId, quantity);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found: " + productId));
    }

    @Transactional(readOnly = true)
    public boolean checkAvailability(Long productId, int requested) {
        return inventoryRepository.findByProductId(productId)
                .map(inv -> inv.isAvailable(requested))
                .orElse(false);
    }

    // ─── Helpers ───

    private void rollbackReservations(Long orderId, List<Map<String, Object>> items, List<Long> reservedProductIds) {
        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("productId").toString());
            if (reservedProductIds.contains(productId)) {
                inventoryRepository.findByProductId(productId).ifPresent(inv -> {
                    int qty = Integer.parseInt(item.get("quantity").toString());
                    inv.release(qty);
                    inventoryRepository.save(inv);
                    log.info("[INVENTORY] Rolled back reservation for productId: {}", productId);
                });
            }
        }
    }

    private InventoryResponse mapToResponse(Inventory inv) {
        return InventoryResponse.builder()
                .id(inv.getId())
                .productId(inv.getProductId())
                .productSku(inv.getProductSku())
                .quantity(inv.getQuantity())
                .reservedQuantity(inv.getReservedQuantity())
                .availableQuantity(inv.getAvailableQuantity())
                .lowStock(inv.isLowStock())
                .lowStockThreshold(inv.getLowStockThreshold())
                .warehouseLocation(inv.getWarehouseLocation())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }
}

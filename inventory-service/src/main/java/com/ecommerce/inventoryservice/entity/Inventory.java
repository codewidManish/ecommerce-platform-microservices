package com.ecommerce.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory", indexes = {
    @Index(name = "idx_inventory_product", columnList = "product_id", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(name = "product_sku", nullable = false, length = 50)
    private String productSku;

    /** Total available quantity */
    @Column(nullable = false)
    @Builder.Default
    private int quantity = 0;

    /** Quantity locked (reserved) for pending orders */
    @Column(name = "reserved_quantity")
    @Builder.Default
    private int reservedQuantity = 0;

    /** Low-stock alert threshold */
    @Column(name = "low_stock_threshold")
    @Builder.Default
    private int lowStockThreshold = 10;

    @Column(name = "warehouse_location", length = 100)
    private String warehouseLocation;

    @Version
    private Long version;   // Optimistic locking for concurrent stock updates

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    public boolean isAvailable(int requested) {
        return getAvailableQuantity() >= requested;
    }

    public boolean isLowStock() {
        return getAvailableQuantity() <= lowStockThreshold;
    }

    /** Reserve stock for an order - throws if insufficient */
    public void reserve(int qty) {
        if (!isAvailable(qty)) {
            throw new IllegalStateException(
                "Insufficient stock for productId=" + productId + ". Available=" + getAvailableQuantity() + ", Requested=" + qty);
        }
        this.reservedQuantity += qty;
    }

    /** Release reservation (on cancel/failure) */
    public void release(int qty) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - qty);
    }

    /** Confirm reservation → deduct from physical stock */
    public void confirmReservation(int qty) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - qty);
        this.quantity = Math.max(0, this.quantity - qty);
    }

    /** Restock */
    public void restock(int qty) {
        this.quantity += qty;
    }
}

package com.ecommerce.cartservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RedisHash("carts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Cart implements Serializable {

    @Id
    private String id;              // cartId = "cart:{userId}"

    @Indexed
    private Long userId;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TimeToLive(unit = TimeUnit.SECONDS)
    @Builder.Default
    private long ttl = 604800L;    // 7 days TTL

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public void addItem(CartItem newItem) {
        Optional<CartItem> existing = items.stream()
                .filter(i -> i.getProductId().equals(newItem.getProductId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + newItem.getQuantity());
        } else {
            items.add(newItem);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void removeItem(Long productId) {
        items.removeIf(i -> i.getProductId().equals(productId));
        this.updatedAt = LocalDateTime.now();
    }

    public void updateItemQuantity(Long productId, int quantity) {
        items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    if (quantity <= 0) {
                        items.remove(item);
                    } else {
                        item.setQuantity(quantity);
                    }
                });
        this.updatedAt = LocalDateTime.now();
    }

    public void clear() {
        items.clear();
        this.updatedAt = LocalDateTime.now();
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CartItem implements Serializable {
        private Long productId;
        private String productName;
        private String productSku;
        private String imageUrl;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal originalPrice;

        public BigDecimal getSubtotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}

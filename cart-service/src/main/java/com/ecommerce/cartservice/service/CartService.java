package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.config.ProductServiceClient;
import com.ecommerce.cartservice.dto.*;
import com.ecommerce.cartservice.entity.Cart;
import com.ecommerce.cartservice.exception.*;
import com.ecommerce.cartservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;

    private static final String CART_KEY_PREFIX = "cart:";

    // ──────────────── GET CART ────────────────

    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return mapToResponse(cart);
    }

    // ──────────────── ADD ITEM ────────────────

    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        // Validate product exists and is active
        ProductServiceClient.ProductInfo product = productServiceClient.getProduct(request.getProductId());
        if (product == null) {
            throw new ProductNotFoundException("Product not found: " + request.getProductId());
        }
        if (!"ACTIVE".equals(product.status())) {
            throw new ProductNotAvailableException("Product is not available: " + request.getProductId());
        }

        Cart cart = getOrCreateCart(userId);

        Cart.CartItem newItem = Cart.CartItem.builder()
                .productId(product.id())
                .productName(product.name())
                .productSku(product.sku())
                .imageUrl(product.imageUrl())
                .quantity(request.getQuantity())
                .unitPrice(product.price())
                .originalPrice(product.originalPrice() != null ? product.originalPrice() : product.price())
                .build();

        cart.addItem(newItem);
        Cart saved = cartRepository.save(cart);
        log.info("Item added to cart for user {}: product {}", userId, request.getProductId());
        return mapToResponse(saved);
    }

    // ──────────────── REMOVE ITEM ────────────────

    public CartResponse removeItem(Long userId, Long productId) {
        Cart cart = getCartOrThrow(userId);
        cart.removeItem(productId);
        Cart saved = cartRepository.save(cart);
        log.info("Item removed from cart for user {}: product {}", userId, productId);
        return mapToResponse(saved);
    }

    // ──────────────── UPDATE ITEM QUANTITY ────────────────

    public CartResponse updateItemQuantity(Long userId, Long productId, int quantity) {
        if (quantity < 0) throw new InvalidCartOperationException("Quantity cannot be negative");

        Cart cart = getCartOrThrow(userId);
        boolean itemExists = cart.getItems().stream().anyMatch(i -> i.getProductId().equals(productId));
        if (!itemExists) throw new CartItemNotFoundException("Item not in cart: " + productId);

        cart.updateItemQuantity(productId, quantity);
        Cart saved = cartRepository.save(cart);
        log.info("Cart item quantity updated for user {}: product {}, qty {}", userId, productId, quantity);
        return mapToResponse(saved);
    }

    // ──────────────── CLEAR CART ────────────────

    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.clear();
        cartRepository.save(cart);
        log.info("Cart cleared for user {}", userId);
    }

    // ──────────────── DELETE CART ────────────────

    public void deleteCart(Long userId) {
        String cartId = CART_KEY_PREFIX + userId;
        cartRepository.deleteById(cartId);
        log.info("Cart deleted for user {}", userId);
    }

    // ──────────────── PRIVATE HELPERS ────────────────

    private Cart getOrCreateCart(Long userId) {
        String cartId = CART_KEY_PREFIX + userId;
        return cartRepository.findById(cartId).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .id(cartId)
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return cartRepository.save(newCart);
        });
    }

    private Cart getCartOrThrow(Long userId) {
        String cartId = CART_KEY_PREFIX + userId;
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
    }

    private CartResponse mapToResponse(Cart cart) {
        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUserId())
                .items(cart.getItems().stream().map(item -> CartItemResponse.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productSku(item.getProductSku())
                        .imageUrl(item.getImageUrl())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .originalPrice(item.getOriginalPrice())
                        .subtotal(item.getSubtotal())
                        .build()).toList())
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
